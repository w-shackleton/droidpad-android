/*  This file is part of DroidPad.
 *
 *  DroidPad is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DroidPad is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DroidPad.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.digitalsquid.droidpad;

import java.net.InetAddress;
import java.util.UUID;

import org.spongycastle.crypto.tls.TlsPSKIdentity;

import uk.digitalsquid.droidpad.Pairing.DevicePair;
import uk.digitalsquid.droidpad.buttons.Layout;
import uk.digitalsquid.droidpad.buttons.ModeSpec;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Backend service that runs the service that communicates with the computer.
 * @author william
 *
 */
public class BGService extends Service implements ConnectionCallbacks, LogTag {
	
	public static final String MODE_SPEC = "uk.digitalsquid.droidpad.BGService.ModeSpec";
	
	public static final String INTENT_STATUSUPDATE = "uk.digitalsquid.droidpad.BGService.Status";
	public static final String INTENT_ALERT = "uk.digitalsquid.droidpad.BGService.Alert";
	public static final String INTENT_EXTRA_STATE = "uk.digitalsquid.droidpad.BGService.Status.State";
	public static final String INTENT_EXTRA_IP = "uk.digitalsquid.droidpad.BGService.Status.Ip";
	public static final String INTENT_EXTRA_ALERT_TYPE = "uk.digitalsquid.droidpad.BGService.Alert.Type";
	public static final int STATE_CONNECTED = 1;
	public static final int STATE_WAITING = 2;
	public static final int STATE_CONNECTION_LOST = 3;
	
	public static final class Calibration {
		public final float x, y;
		public Calibration(float x, float y) {
			this.x = x;
			this.y = y;
		}
		public Calibration(SharedPreferences prefs) {
			// FIXME: Use proper calibration rather than this?
			x = prefs.getFloat("calibX", 0);
			y = prefs.getFloat("calibY", 0);
		}
		
		public void save(SharedPreferences prefs) {
			Editor prefEd = prefs.edit();
			prefEd.putFloat("calibX", x);
			prefEd.putFloat("calibY", y);
			prefEd.commit();
		}
	}
	
	private App app;
	private SharedPreferences prefs;
	private SensorManager sm;
	
	private WifiLock wifiLock;
	private MulticastLock multicastLock;
	private InetAddress wifiAddr;
	private MDNSBroadcaster mdns;
	
	private int state = 0;
	private String connectedPc = "Computer"; // Temp placeholder text
	private int port;
	
	private ModeSpec spec = new ModeSpec();
	private Calibration calibration;
	
	private Connection connection;
	private SecureConnection secureConnection;
	
	private final Vec3 accelerometer = new Vec3();
	/**
	 * Integrated from the gyroscope
	 */
	private final Vec3 rotation = new Vec3();
	private final Vec3 rotationalVelocity = new Vec3();
	/**
	 * Rotation about the world's z axis, rather than the phone's.
	 */
	private float worldRotation;
	/**
	 * Data which the user has inputed onscreen.
	 */
	private Layout screenData;
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = (App) getApplication();
		
		// Load preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		calibration = new Calibration(prefs);
		try {
			port = Integer.valueOf(prefs.getString("portnumber", "3141"));
		} catch (ClassCastException e) {
			Log.e(TAG, "ERROR: Invalid preference", e);
			Toast.makeText(this, "Invalid port number", Toast.LENGTH_LONG).show();
			port = 3141;
		} catch (NumberFormatException e) {
			Log.e(TAG, "ERROR: Invalid preference", e);
			Toast.makeText(this, "Invalid port number", Toast.LENGTH_LONG).show();
			port = 3141;
		}
		
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		// Acquire wifi and multicast locks
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wm.getConnectionInfo();
		if(wifiInfo != null) wifiAddr = Buttons.intToInetAddress(wifiInfo.getIpAddress());
		wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
		multicastLock = wm.createMulticastLock(TAG);
			
		// Start mDNS broadcaster
		String deviceName = prefs.getString("devicename", Build.MODEL);
		// In case field is set but blank
		if(deviceName.equals("")) deviceName = Build.MODEL;
		deviceName = "secure:" + deviceName;
		mdns = new MDNSBroadcaster(wifiAddr,
				deviceName.substring(0, Math.min(deviceName.length(), 40)),
				port);
		mdns.start();
	}
	
	/**
	 * Call this method when the user has chosen a mode to use. The mode that
	 * then should be displayed will then be returned; most of the time the
	 * mode the user chose will be returned, but in some cases the service
	 * will override the user's choice (for example if a session is running
	 * already).
	 * @param spec
	 * @return
	 */
	public synchronized ModeSpec onModeChosen(ModeSpec spec) {
		boolean closed = true;
		if(connection != null) closed &= connection.attemptClose();
		if(secureConnection != null) closed &= secureConnection.attemptClose();
		if(closed) { // Closed idle connection successfully
			this.spec = createNewConnection(spec);
			return spec;
		} else { // Connection running, user can't change spec.
			return this.spec;
		}
	}
	
	@SuppressLint("NewApi")
	private synchronized ModeSpec createNewConnection(ModeSpec newSpec) {
		wifiLock.acquire();
		multicastLock.acquire();
		
		// Load basic preferences (MINIMAL)
		int interval = 20;
		boolean landscape = false;
		try {
			interval = Integer.valueOf(prefs.getString("updateinterval", "20")); // Using string as for some reason Android won't
			landscape = prefs.getBoolean("orientation", false);
		} catch (ClassCastException e) {
			Log.e(TAG, "ERROR: Invalid preference", e);
			Toast.makeText(this, "Incorrect preferences set, please check", Toast.LENGTH_LONG).show();
		} catch (NumberFormatException e) {
			Log.e(TAG, "ERROR: Invalid preference", e);
			Toast.makeText(this, "Incorrect preferences set, please check", Toast.LENGTH_LONG).show();
		}
		
		newSpec.setLandscape(landscape);
		
		final boolean needsAccel = true;
		final boolean needsGyro = newSpec.getLayout().getExtraDetail() == Layout.EXTRA_MOUSE_ABSOLUTE;
        // Special cases which need extra detail
		
		if(needsAccel) {
			Sensor downwardsDirection = null;
			downwardsDirection = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
			if(downwardsDirection == null) // Use accel
				downwardsDirection = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			
			if(downwardsDirection != null)
				sm.registerListener(sensorEvents, downwardsDirection, SensorManager.SENSOR_DELAY_GAME);
			else
				Toast.makeText(this, "Accelerometer not found on this device", Toast.LENGTH_SHORT).show();
		}
        if(needsGyro) {
        	Sensor gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        	if(gyro != null)
				sm.registerListener(sensorEvents, gyro, SensorManager.SENSOR_DELAY_GAME);
        	else
				Toast.makeText(this, "Gyroscope not found on this device", Toast.LENGTH_SHORT).show();
        }
		
		ConnectionInfo connectionInfo = new ConnectionInfo();
		connectionInfo.callbacks = this;
		connectionInfo.port = port;
		connectionInfo.securePort = port + 1; // For the time being using p+1, until I can figure out how to
											  // receive mDNS properties using the C mDNS library in the PC app
		connectionInfo.spec = newSpec;
		connectionInfo.interval = (float)interval / 1000f;
		connectionInfo.reverseX = prefs.getBoolean("reverse-x", false);
		connectionInfo.reverseY = prefs.getBoolean("reverse-y", false);
		connectionInfo.identity = pskAuthenticator;
		
		// Set up normal connection
		if(!prefs.getBoolean("onlysecureconnection", false)) {
			connection = new Connection();
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, connectionInfo);
			else
				connection.execute(connectionInfo);
		}
		// Set up secure connection
		secureConnection = new SecureConnection();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			secureConnection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, connectionInfo);
		else
			secureConnection.execute(connectionInfo);
		
		
		Log.i(TAG, "Starting new connection");
		
		return newSpec;
	}

	@Override
	public synchronized void onConnectionFinished() {
		Log.i(TAG, "Connection finishing");
		// Kill off any remaining threads first.
		if(connection != null) connection.cancel(true);
		if(secureConnection != null) secureConnection.cancel(true);
		connection = null; secureConnection = null;
		if(app.isServiceRequired()) {
			// Launch again with old spec
			Log.i(TAG, "Still required, launching new connection");
			createNewConnection(spec);
		} else {
			Log.i(TAG, "Not required, stopping service");
			this.stopSelf();
		}
		wifiLock.release();
		multicastLock.release();
		
		sm.unregisterListener(sensorEvents);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(connection != null) connection.cancel(true);
		if(secureConnection != null) secureConnection.cancel(true);
		mdns.stopRunning();
		Log.i(TAG, "Service stopped");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
    public class LocalBinder extends Binder {
        BGService getService() {
            return BGService.this;
        }
    }
    private final IBinder binder = new LocalBinder();
    
    long gyroIntegrationTime;
    boolean gyroAvailable = false;
    
    private SensorEventListener sensorEvents = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			switch(event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
			case Sensor.TYPE_GRAVITY:
				if(spec.isLandscape()) {
					accelerometer.set(
							-event.values[1],
							-event.values[0],
							event.values[2]);
				} else {
					accelerometer.set(event.values);
				}
				accelerometer.minusLocal(calibration.x, calibration.y, 0);
				break;
			case Sensor.TYPE_GYROSCOPE:
				gyroAvailable = true;
				if(spec.isLandscape()) {
					rotationalVelocity.set(
							-event.values[1],
							-event.values[0],
							event.values[2]);
				} else {
					rotationalVelocity.set(event.values);
				}
				float timeDiff = (float)(gyroIntegrationTime - System.nanoTime()) / 1000f / 1000f / 1000f;
				if(gyroIntegrationTime != 0) {
					// TODO: Use trapezium rule?
					Vec3 rotationDelta = rotationalVelocity.mul(timeDiff);
					rotation.addLocal(rotationDelta);
					
					/*
					 * The dot product here selects the components of the rotations which
					 * are important for rotating about the world z-axis (not the phone's one)
					 */
					Vec3 gravityUnit = accelerometer.getUnitVector();
					float normalisedRotation = rotationalVelocity.dot(gravityUnit);
					worldRotation += normalisedRotation * timeDiff;
				}
				
				gyroIntegrationTime = System.nanoTime();
			}
		}
    };

	@Override
	public Vec3 getAccelerometerValues() {
		return accelerometer;
	}

	@Override
	public Vec3 getGyroscopeValues() {
		return rotation;
	}

	@Override
	public float getWorldRotation() {
		return worldRotation;
	}
    
    public void broadcastState() {
    	Intent intent = new Intent(INTENT_STATUSUPDATE);
    	intent.putExtra(INTENT_EXTRA_STATE, state);
    	intent.putExtra(INTENT_EXTRA_IP, connectedPc);
    	sendBroadcast(intent);
    }
    
    /**
     * Sends out a broadcast intent with information about whether DroidPad is connected.
     * @param state
     * @param connectedPc
     */
    @Override
    public void broadcastState(int state, String connectedPc) {
    	this.state = state;
    	this.connectedPc = connectedPc;
    	broadcastState();
    }

	@Override
	public void broadcastAlert(int type) {
		Intent intent = new Intent(INTENT_ALERT);
		intent.putExtra(INTENT_EXTRA_ALERT_TYPE, type);
		sendBroadcast(intent);
	}
    
    public int getState() {
    	return state;
    }
    
    public void calibrate() {
    	if(accelerometer != null) {
    		calibration = new Calibration(accelerometer.x, accelerometer.y);
    	}
    }

    @Override
	public Layout getScreenData() {
		return screenData;
	}

	public void setScreenData(Layout screenData) {
		this.screenData = screenData;
	}
	
	// TLS authenticator
	private TlsPSKIdentity pskAuthenticator =  new TlsPSKIdentity() {
		@Override
		public void skipIdentityHint() {
			Log.e(TAG, "skipIdentityHint called!!!11!!");
		}
		
		private UUID computerId;
		
		private DevicePair credentials;
		
		private void retrieveCredentials() {
			Pairing pairing = app.getPairingEngine();
			credentials = pairing.findDevicePair(computerId);
		}
		
		@Override
		public void notifyIdentityHint(byte[] psk_identity_hint) {
			String uuid = new String(psk_identity_hint);
			try {
				computerId = UUID.fromString(uuid);
			} catch(NullPointerException e) {
				Log.e(TAG, "No PSK identity given?!?");
			} catch(IllegalArgumentException e) {
				Log.w(TAG, "Incorrectly formatted UUID");
			}
		}
		
		@Override
		public byte[] getPSKIdentity() {
			retrieveCredentials();
			if(credentials == null) return "NOCREDS".getBytes();
			return credentials.getDeviceId().toString().getBytes();
		}
		
		@Override
		public byte[] getPSK() {
			retrieveCredentials();
			if(credentials == null) return new byte[] {};
			return credentials.getPsk();
		}
	};
}

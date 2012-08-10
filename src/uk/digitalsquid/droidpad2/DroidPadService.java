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

package uk.digitalsquid.droidpad2;

import java.net.InetAddress;

import uk.digitalsquid.droidpad2.buttons.Item;
import uk.digitalsquid.droidpad2.buttons.Layout;
import uk.digitalsquid.droidpad2.buttons.ModeSpec;
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
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class DroidPadService extends Service implements LogTag {
	
	public static final String MODE_SPEC = "uk.digitalsquid.droidpad2.DroidPadService.ModeSpec";
	
	public static final String INTENT_STATUSUPDATE = "uk.digitalsquid.droidpad2.DroidPadService.Status";
	public static final String INTENT_EXTRA_STATE = "uk.digitalsquid.droidpad2.DroidPadService.Status.State";
	public static final String INTENT_EXTRA_IP = "uk.digitalsquid.droidpad2.DroidPadService.Status.Ip";
	public static final int STATE_CONNECTED = 1;
	public static final int STATE_WAITING = 2;
	public static final int STATE_CONNECTION_LOST = 3;
	
	private Boolean setup = false;
	private Connection connection;
	private Thread th;
	public int port;
	public int interval;
	
	public static final int PURPOSE_SETUP = 1;
	public static final int PURPOSE_CALIBRATE = 2;
	
	ModeSpec mode;
	
	public boolean invX, invY;
	public Layout buttons;
	
	private SensorManager sm;
	float x, y, z;
	float gyroX, gyroY, gyroZ, gyroAccumulator;
	float calibX = 0, calibY = 0;
	
	protected boolean landscape = false;
	
	InetAddress wifiAddr;
	
	private WifiLock wifiLock;
	private MulticastLock multicastLock;
	
	private SharedPreferences prefs;
	
	private MDNSBroadcaster mdns;
	
	// NORMAL SERVICE
	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		try {
			interval = Integer.valueOf(prefs.getString("updateinterval", "20")); // Using string as for some reason Android won't
			port = Integer.valueOf(prefs.getString("portnumber", "3141")); // do conversion in some cases.
			landscape = prefs.getBoolean("orientation", false);
		} catch (ClassCastException e) {
			Log.e(TAG, "ERROR: Invalid preference", e);
			Toast.makeText(this, "Incorrect preferences set, please check", Toast.LENGTH_LONG).show();
			stopSelf();
			return;
		} catch (NumberFormatException e) {
			Log.e(TAG, "ERROR: Invalid preference", e);
			Toast.makeText(this, "Incorrect preferences set, please check", Toast.LENGTH_LONG).show();
			stopSelf();
			return;
		}
		
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		Sensor downwardsDirection = null;
		downwardsDirection = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
		if(downwardsDirection == null) // Use accel
			downwardsDirection = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if(downwardsDirection == null) {
			Toast.makeText(this, "Accelerometer not found on this device", Toast.LENGTH_SHORT).show();
			// TODO: Stop here?
		}
		sm.registerListener(sensorEvents, downwardsDirection, SensorManager.SENSOR_DELAY_GAME);
		
		// FIXME: Use proper calibration rather than this?
		calibX = prefs.getFloat("calibX", 0);
		calibY = prefs.getFloat("calibY", 0);
		Log.v(TAG, "DPS: Calibration " + String.valueOf(calibX) + ", " + String.valueOf(calibY) + " found.");
		
		WifiManager wm = null;
        
		try {
			wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		} catch(Exception e) {
			Log.w(TAG, "DPS: Could not get wifi manager");
		}
		if(wm != null) {
			WifiInfo wifiInfo = wm.getConnectionInfo();
			if(wifiInfo != null)
				wifiAddr = Buttons.intToInetAddress(wifiInfo.getIpAddress());
			try {
				wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
				multicastLock = wm.createMulticastLock(TAG);
			} catch (Exception e) {
				Log.w(TAG, "DPS: Could not create wifi lock or multicast lock");
			}
		}
        
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "DPS: Service Started");
		int purpose = -1;
		try {
			purpose = intent.getExtras().getInt("purpose");
		} catch(NullPointerException e) { // Reported thru market?!?
			Log.e(TAG, "Failed to get purpose!", e);
			return START_STICKY_COMPATIBILITY;
		}
		switch(purpose) {
		case PURPOSE_SETUP:
			if(!setup)
			{
				//Toast.makeText(this, "Service started with setup", Toast.LENGTH_SHORT).show();
				setup = true;
				
				if(wifiLock != null) {
					wifiLock.acquire();
					multicastLock.acquire();
					Log.d(TAG, "DPS: Wifi Locked & multicasting enabled");
				}

		        mode = (ModeSpec) intent.getSerializableExtra(MODE_SPEC);
		        
		        // Special cases which need extra detail
		        if(mode.getLayout().getExtraDetail() == Layout.EXTRA_MOUSE_ABSOLUTE) { // Needs gyro
		        	Sensor gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		        	if(gyro != null) {
						sm.registerListener(sensorEvents, gyro, SensorManager.SENSOR_DELAY_GAME);
		        	} else {
						Toast.makeText(this, "Gyroscope not found on this device", Toast.LENGTH_SHORT).show();
		        	}
		        }
		        
		        connection = new Connection(this, port, interval, mode, prefs.getBoolean("reverse-x", false), prefs.getBoolean("reverse-y", false));
		        th = new Thread(connection);
		        th.start();
				Log.v(TAG, "DPS: DroidPad connection thread started!");
				
				Log.v(TAG, "DPS: Starting mDNS broadcaster");
				final String defaultName = Build.MODEL;
				String deviceName =
						PreferenceManager.getDefaultSharedPreferences(
								getBaseContext()).getString("devicename",
										defaultName);
				// In case field is set but blank
				if(deviceName.equals("")) deviceName = defaultName;
				mdns = new MDNSBroadcaster(wifiAddr,
						deviceName.substring(0, Math.min(deviceName.length(), 40)),
						port);
				mdns.start();
			}
			else
			{
				Log.w(TAG, "DPS: Service started but already set up (this shouldn't happen)");
			}
			break;
		case 0:
			Log.i(TAG, "DPS: Unknown purpose, killing...");
			stopSelf();
			break;
		}
		return START_STICKY_COMPATIBILITY;
	}
	@Override
	public void onDestroy() {
		// Making sure here to check each object, as service instance may try to destroy after killing and remaking process, and other weird stuff.
		if(sensorEvents != null && sm != null)
			sm.unregisterListener(sensorEvents);
		Log.v(TAG, "DPS: Stopping DPC thread...");
		connection.killThread();
		try {
			th.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mdns.stopRunning();
		// Let it die out by itself.
		Log.v(TAG, "DPS: DPC thread down!");
		if(wifiLock != null) wifiLock.release();
		if(multicastLock != null) multicastLock.release();
		Log.d(TAG, "DPS: Wifi unlocked & multicasting released.");
		Editor prefEd = PreferenceManager.getDefaultSharedPreferences(this).edit();
		prefEd.putFloat("calibX", calibX);
		prefEd.putFloat("calibY", calibY);
		prefEd.commit();
        //Toast.makeText(this, "Unlocking Wifi.", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "DPS: Service destroyed & prefs saved.");
		super.onDestroy();
	}
	// END NORMAL SERVICE
	
	// BIND
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
	
    public class LocalBinder extends Binder {
        DroidPadService getService() {
            return DroidPadService.this;
        }
    }
    private final IBinder binder = new LocalBinder();
    // END BIND
    
    long gyroIntegrationTime;
    boolean gyroAvailable = false;
    
    private SensorEventListener sensorEvents = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			switch(event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
			case Sensor.TYPE_GRAVITY:
				if(landscape) {
					x = -event.values[1];
					y = -event.values[0];
					z = event.values[2];
				} else {
					x = event.values[0];
					y = event.values[1];
					z = event.values[2];
				}
				break;
			case Sensor.TYPE_GYROSCOPE:
				gyroAvailable = true;
				float rx, ry, rz;
				if(landscape) {
					rx = -event.values[1];
					ry = -event.values[0];
					rz = event.values[2];
				} else {
					rx = event.values[0];
					ry = event.values[1];
					rz = event.values[2];
				}
				float timeDiff = (float)(gyroIntegrationTime - System.nanoTime()) / 1000f / 1000f / 1000f;
				if(gyroIntegrationTime != 0) {
					// TODO: Use trapezium rule?
					gyroX += rx * timeDiff;
					gyroY += ry * timeDiff;
					gyroZ += rz * timeDiff;
					
					/*
					 * The dot product here selects the components of the rotations which
					 * are important for rotating about the world z-axis (not the phone's one)
					 */
					Vec3 gravityUnit = new Vec3(x, y, z).getUnitVector();
					float normalisedRotation = new Vec3(rx, ry, rz).dot(gravityUnit);
					gyroAccumulator += normalisedRotation * timeDiff;
				}
				
				gyroIntegrationTime = System.nanoTime();
			}
		}
    };
    
    /**
     * Returns the analogue JS values from the accelerometer reports.
     * @return
     */
    public float[] getAccelValues() {
    	return new float[] {x - calibX, y - calibY, z};
    }
    
    public float[] getGyroValues() {
    	if(!gyroAvailable) return null;
    	return new float[] { gyroX, gyroY, gyroZ, gyroAccumulator };
    }
    
    public Layout getButtons() {
    	return buttons;
    }
    
    /**
     * Sends out a broadcast intent with information about whether DroidPad is connected.
     * @param state
     * @param connectedPc
     */
    void broadcastState(int state, String connectedPc) {
    	Intent intent = new Intent(INTENT_STATUSUPDATE);
    	intent.putExtra(INTENT_EXTRA_STATE, state);
    	intent.putExtra(INTENT_EXTRA_IP, connectedPc);
    	sendBroadcast(intent);
    }
    
    void calibrate() {
    	calibX = x;
    	calibY = y;
    	Toast.makeText(this, "Saved calibration", Toast.LENGTH_LONG).show();
    }
}

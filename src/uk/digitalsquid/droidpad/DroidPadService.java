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

import uk.digitalsquid.droidpad.buttons.Layout;
import uk.digitalsquid.droidpad.buttons.ModeSpec;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class DroidPadService extends Service implements LogTag {
	
	public static final String MODE_SPEC = "uk.digitalsquid.droidpad.DroidPadService.ModeSpec";
	
	public static final String INTENT_STATUSUPDATE = "uk.digitalsquid.droidpad.DroidPadService.Status";
	public static final String INTENT_EXTRA_STATE = "uk.digitalsquid.droidpad.DroidPadService.Status.State";
	public static final String INTENT_EXTRA_IP = "uk.digitalsquid.droidpad.DroidPadService.Status.Ip";
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
	public float x = 0,y = 0,z = 0;
	public float calibX = 0, calibY = 0;
	
	protected boolean landscape = false;
	
	private WifiLock wL;
	
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
		try {
			sm.registerListener(sensorEvents, sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);
		} catch(IndexOutOfBoundsException e) {
			Toast.makeText(this, "ERROR: Accelerometer not found!", Toast.LENGTH_SHORT).show();
		}
		calibX = prefs.getFloat("calibX", 0);
		calibY = prefs.getFloat("calibY", 0);
		Log.v(TAG, "DPS: Calibration " + String.valueOf(calibX) + ", " + String.valueOf(calibY) + " found.");
		
		WifiManager wm = null;
        
		try{ wm = (WifiManager) getSystemService(Context.WIFI_SERVICE); }
		catch(Exception e)
		{
			Log.w(TAG, "DPS: Could not get wifi manager");
		}
		if(wm != null)
		{
			try{ wL = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG); }
			catch (Exception e)
			{
				Log.w(TAG, "DPS: Could not create wifi lock");
			}
		}
		Log.v(TAG, "DPS: Wifi lock sorted...");
        
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "DPS: Service Started");
		switch(intent.getExtras().getInt("purpose"))
		{
		case PURPOSE_SETUP:
			if(!setup)
			{
				//Toast.makeText(this, "Service started with setup", Toast.LENGTH_SHORT).show();
				setup = true;
				
				if(wL != null) {
					wL.acquire();
					Log.d(TAG, "DPS: Wifi Locked");
				}

		        mode = (ModeSpec) intent.getSerializableExtra(MODE_SPEC);
		        connection = new Connection(this, port, interval, mode, prefs.getBoolean("reverse-x", false), prefs.getBoolean("reverse-y", false));
		        th = new Thread(connection);
		        th.start();
				Log.v(TAG, "DPS: DroidPad connection thread started!");
				
				Log.v(TAG, "DPS: Starting mDNS broadcaster");
				mdns = new MDNSBroadcaster("DEVICE NAME", port);
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
		if(wL != null) wL.release();
		Log.d(TAG, "DPS: Wifi unlocked");
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
    
    private SensorEventListener sensorEvents = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if(landscape)
			{
				x = -event.values[1];
				y = -event.values[0];
				z = event.values[2];
			}
			else
			{
				x = event.values[0];
				y = event.values[1];
				z = event.values[2];
			}
		}
    };
    
    /**
     * Returns the analogue JS values from the accelerometer reports.
     * @return
     */
    public float[] getAVals()
    {
    	return new float[] {x - calibX, y - calibY, z};
    }
    
    public synchronized Layout getButtons()
    {
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
}

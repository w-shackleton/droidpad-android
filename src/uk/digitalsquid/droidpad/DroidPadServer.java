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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

public class DroidPadServer extends Service {
	private NotificationManager NM;
	private Notification notification;
	private PendingIntent contentIntent;
	private Boolean setup = false;
	private DroidPadConn apc;
	private DroidPad win;
	private Thread th;
	public int port;
	public int interval;
	
	public String mode = "1";
	public boolean invX, invY;
	public Layout buttons;
	
	private SensorManager sm;
	public float x = 0,y = 0,z = 0;
	public float calibX = 0, calibY = 0;
	
	protected boolean landscape = false;
	
	private WifiLock wL;
	
	private SharedPreferences prefs;
	
	// NORMAL SERVICE
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("DroidPad", "DPS: Service Created");
        NM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		mode = prefs.getString("layout", "1s4");
		
		landscape = prefs.getBoolean("orientation", false);

		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if(mode != "slide")
		{
			try
			{
				sm.registerListener(asl, sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);
			}
			catch(IndexOutOfBoundsException e)
			{
				Toast.makeText(this, "ERROR: Accelerometer not found!", Toast.LENGTH_SHORT).show();
			}
			
			Log.v("DroidPad", "DPS: Sensors initiated.");
		}
		calibX = prefs.getFloat("calibX", 0);
		calibY = prefs.getFloat("calibY", 0);
		Log.v("DroidPad", "DPS: Calibration " + String.valueOf(calibX) + ", " + String.valueOf(calibY) + " found.");
		
		WifiManager wm = null;
        
		try{ wm = (WifiManager) getSystemService(Context.WIFI_SERVICE); }
		catch(Exception e)
		{
			Log.w("DroidPad", "DPS: Could not get wifi manager");
		}
		if(wm != null)
		{
			try{ wL = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "DroidPad"); }
			catch (Exception e)
			{
				Log.w("DroidPad", "DPS: Could not create wifi lock");
			}
		}
		Log.v("DroidPad", "DPS: Wifi lock sorted...");
        
	}
	private void showNotification() {
		notification = new Notification(R.drawable.icon, getText(R.string.NotificationTitle2),
                System.currentTimeMillis());
		contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DroidPad.class), 0);
		notification.setLatestEventInfo(this, getText(R.string.NotificationTitle),
				getText(R.string.NotificationText), contentIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		NM.notify(R.string.NotificationTitle, notification);
		Log.v("DroidPad", "DPS: Notification");
	}
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		Log.d("DroidPad", "DPS: Service Started");
		switch(intent.getExtras().getInt("purpose"))
		{
		case DroidPad.PURPOSE_SETUP:
			if(!setup)
			{
				//Toast.makeText(this, "Service started with setup", Toast.LENGTH_SHORT).show();
				setup = true;
				
				if(wL != null)
				{
					wL.acquire();
					Log.d("DroidPad", "DPS: Wifi Locked");
				}
		        //Toast.makeText(this, "Locking Wifi.", Toast.LENGTH_SHORT).show();
				interval = intent.getExtras().getInt("interval", 20);
				port = intent.getExtras().getInt("port", 3141);

		        apc = new DroidPadConn(this, port, interval, mode, prefs.getBoolean("reverse-x", false), prefs.getBoolean("reverse-y", false));
				Log.v("DroidPad", "DPS: DroidPad connection initiated");
		        th = new Thread(apc);
				Log.v("DroidPad", "DPS: DroidPad connection thread initiated");
		        th.start();
				Log.v("DroidPad", "DPS: DroidPad connection thread started!");
			}
			else
			{
				Log.w("DroidPad", "DPS: Service started but already set up (this shouldn't happen)");
			}
			break;
		case DroidPad.PURPOSE_CALIBRATE:
			calibX = x;
			calibY = y;
			Log.v("DroidPad", "DPS: Calibrated");
			break;
		case 0:
			Log.i("DroidPad", "DPS: Unknown purpose, killing...");
			stopSelf();
			break;
		}
	}
	@Override
	public void onDestroy() {
		NM.cancel(R.string.NotificationTitle);
		if(asl != null)
			sm.unregisterListener(asl);
		Log.v("DroidPad", "DPS: Stopping DPC thread...");
		apc.killThread();
		try {
			th.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.v("DroidPad", "DPS: DPC thread down!");
		wL.release();
		Log.d("DroidPad", "DPS: Wifi unlocked");
		Editor prefEd = PreferenceManager.getDefaultSharedPreferences(this).edit();
		prefEd.putFloat("calibX", calibX);
		prefEd.putFloat("calibY", calibY);
		prefEd.commit();
        //Toast.makeText(this, "Unlocking Wifi.", Toast.LENGTH_SHORT).show();
		Log.d("DroidPad", "DPS: Service destroyed & prefs saved.");
		super.onDestroy();
	}
	// END NORMAL SERVICE
	
	// BIND
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
    public class LocalBinder extends Binder {
        DroidPadServer getService() {
            return DroidPadServer.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();
    // END BIND
    
    //PARENT CALLBACK
    public void setParentObject(DroidPad a)
    {
    	setWin(a);
    }
    //END PARENT CALLBACK
    private SensorEventListener asl = new SensorEventListener() {
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
    
    public synchronized float[] getAVals()
    {
    	return new float[] {x - calibX, y - calibY, z};
    }
    
    public synchronized Layout getButtons()
    {
    	return buttons;
    }
    
	private void setWin(DroidPad win) {
		this.win = win;
		apc.notifyOfParent();
	}
	
	public DroidPad getWin() {
		return win;
	}
	
	public boolean isWinAvailable() {
		return win != null;
	}
}

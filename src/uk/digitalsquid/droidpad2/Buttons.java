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
import java.net.UnknownHostException;

import uk.digitalsquid.droidpad2.buttons.Layout;
import uk.digitalsquid.droidpad2.buttons.ModeSpec;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Buttons extends Activity implements LogTag
{
	private DroidPadService boundService = null;
	private Intent serviceIntent;
	WakeLock wakelock;
	
	WifiManager wm;
	WifiLock wL;
	
	public static final String MODE_SPEC = "uk.digitalsquid.droidpad2.Buttons.ModeSpec";
	
	LinearLayout connectionContainer;
	ProgressBar connectionStatusProgress;
	TextView connectionStatus, connectionIp;
	
	Animation fadeIn, fadeOut;
	
	ButtonView bview;
	
	/**
	 * The mode, as specified by the user on the previous screen.
	 */
	ModeSpec mode;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN); */
        
        setContentView(R.layout.buttons);
        
        mode = (ModeSpec) getIntent().getSerializableExtra(MODE_SPEC);
        if(mode == null) {
        	Log.w(TAG, "Activity not started with mode specification");
        	finish();
        	return;
        }
        
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout_delay);
        
        // Set up broadcast listening
        statusFilter = new IntentFilter();
        statusFilter.addAction(DroidPadService.INTENT_STATUSUPDATE);
        
        bview = (ButtonView) findViewById(R.id.buttonView);
        bview.setModeSpec(this, mode);
        
        connectionContainer = (LinearLayout) findViewById(R.id.connectionContainer);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        connectionIp = (TextView) findViewById(R.id.connectionIp);
        connectionStatusProgress = (ProgressBar) findViewById(R.id.connectionStatusProgress);
        
        connectionStatus.setText(R.string.connectWaiting);
        
        // Wifi management etc.
        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wL = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "DroidPad");
		wL.acquire();
		
        wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        wifiFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        
        serviceIntent = new Intent(Buttons.this,DroidPadService.class);
        wakelock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,TAG);
        wakelock.acquire();
        
        // We will now start the service to get DroidPad running.
        
		serviceIntent.putExtra("purpose", DroidPadService.PURPOSE_SETUP);
		serviceIntent.putExtra(DroidPadService.MODE_SPEC, mode);
		Log.v(TAG, "Starting DroidPad service");
		startService(serviceIntent);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            boundService = ((DroidPadService.LocalBinder)service).getService();
            sendEvent(bview.getLayout());
        }

        public void onServiceDisconnected(ComponentName className) {
            boundService = null;
        }
    };
    
	private IntentFilter statusFilter;
	private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch(intent.getIntExtra(DroidPadService.INTENT_EXTRA_STATE, DroidPadService.STATE_WAITING)) {
			case DroidPadService.STATE_WAITING:
				if(connectionContainer.getVisibility() != View.VISIBLE) { // Anim in
					connectionContainer.startAnimation(fadeIn);
				}
				connectionStatusProgress.setVisibility(View.VISIBLE);
				connectionContainer.setVisibility(View.VISIBLE);
				
				connectionStatus.setText(R.string.connectWaiting);
				break;
			case DroidPadService.STATE_CONNECTION_LOST:
				if(connectionContainer.getVisibility() != View.VISIBLE) { // Anim in
					connectionContainer.startAnimation(fadeIn);
				}
				connectionStatusProgress.setVisibility(View.VISIBLE);
				connectionContainer.setVisibility(View.VISIBLE);
				
				connectionStatus.setText(R.string.connectFailed);
				break;
			case DroidPadService.STATE_CONNECTED:
				connectionStatusProgress.setVisibility(View.GONE);
				connectionContainer.setVisibility(View.GONE); // But is being animated
				connectionContainer.startAnimation(fadeOut);
				
				connectionStatus.setText(R.string.connected);
				break;
			}
		}
	};
	
	private IntentFilter wifiFilter;
	private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			connectionIp.setText(getWifiStateText());
		}
	};
	
	/**
	 * Returns the resource ID of the text to display as the wifi state.
	 * @return
	 */
    private String getWifiStateText() {
    	int stringId = -1;
        switch (wm.getWifiState())
        {
        case WifiManager.WIFI_STATE_UNKNOWN:
        case WifiManager.WIFI_STATE_DISABLED:
        	stringId = R.string.wifi_disabled;
        	break;
        case WifiManager.WIFI_STATE_DISABLING:
        	stringId = R.string.wifi_disabling;
        	break;
        case WifiManager.WIFI_STATE_ENABLED:
        	// Ask the supplicant how the connection is
	        switch (wm.getConnectionInfo().getSupplicantState())
	        {
	        case ASSOCIATED:
	        case ASSOCIATING:
	        case FOUR_WAY_HANDSHAKE:
	        case GROUP_HANDSHAKE:
	        	stringId = R.string.wifi_connecting;
	        	break;
	        case COMPLETED:
	            // All authentication completed. 
	            // Trying to associate with a BSS/SSID.
	        	int ip = wm.getConnectionInfo().getIpAddress();
	        	return getResources().getString(R.string.wifi_connectToPhone, intToInetAddress(ip).getHostAddress());
	        case DISCONNECTED:
	        case DORMANT:
	        case INACTIVE:
	        case INVALID:
	        case UNINITIALIZED:
	        	stringId = R.string.wifi_disconnected;
	        	break;
	        case SCANNING:
	        	stringId = R.string.wifi_scanning;
	        	break;
			default:
				break;
	        }
	        break;
        case WifiManager.WIFI_STATE_ENABLING:
        	stringId = R.string.wifi_enabling;
        	break;
        }
        // If got through, return string resource
        if(stringId == -1) return "";
        return getResources().getString(stringId);
    }

	public static InetAddress intToInetAddress(int hostAddress) {
	    byte[] addressBytes = { (byte)(0xff & hostAddress),
	                            (byte)(0xff & (hostAddress >> 8)),
	                            (byte)(0xff & (hostAddress >> 16)),
	                            (byte)(0xff & (hostAddress >> 24)) };
	
	    try {
	       return InetAddress.getByAddress(addressBytes);
	    } catch(UnknownHostException e) {
	       return null;
	    }
	}

    /**
     * Binds to the DroidPad service, to communicate with it.
     */
    private void bind() {
        bindService(serviceIntent, serviceConnection,0);
    }
    private void unbind() {
    	if(boundService != null) {
    		unbindService(serviceConnection);
    	}
    }
    
    @Override
    public void onResume() {
    	super.onResume();
		registerReceiver(statusReceiver, statusFilter);
        registerReceiver(wifiReceiver, wifiFilter);
    	bind();
    	getWifiStateText();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
		unregisterReceiver(wifiReceiver);
		unregisterReceiver(statusReceiver);
    	unbind();
    }
    
    @Override
    public void onDestroy() {
    	stopService(serviceIntent);
        wakelock.release();
    	super.onDestroy();
    }
    
    /**
     * Sends the layout info to the service, if it is connected.
     * @param layout
     */
    public void sendEvent(Layout layout) {
    	if(boundService == null) return;
    	boundService.buttons = layout;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.buttons_menu, menu);
		return true;
    	
    }
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	if (!item.hasSubMenu()) {
    		switch (item.getItemId()) {
    		case R.id.calibrate:
    			if(boundService != null) {
    				boundService.calibrate();
    			}
    			break;
    		}
    	}
		return true;
    }
}

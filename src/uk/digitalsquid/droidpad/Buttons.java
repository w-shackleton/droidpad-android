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
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class Buttons extends Activity implements LogTag
{
	private DroidPadService boundService = null;
	private Intent serviceIntent;
	WakeLock wakelock;
	
	public static final String MODE_SPEC = "uk.digitalsquid.droidpad.Buttons.ModeSpec";
	
	ButtonView bview;
	
	/**
	 * The mode, as specified by the user on the previous screen.
	 */
	ModeSpec mode;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        mode = (ModeSpec) getIntent().getSerializableExtra(MODE_SPEC);
        if(mode == null) {
        	Log.w(TAG, "Activity not started with mode specification");
        	finish();
        	return;
        }
        
        bview = new ButtonView(Buttons.this, mode);
        setContentView(bview);
        
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
        }

        public void onServiceDisconnected(ComponentName className) {
            boundService = null;
        }
    };

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
    	bind();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
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
}

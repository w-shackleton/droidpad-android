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

import java.util.List;

import uk.digitalsquid.droidpad.buttons.Layout;
import android.app.Activity;
import android.app.ActivityManager;
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
import android.widget.ToggleButton;

public class DroidPadButtons extends Activity
{
	private DroidPadServer apsbound = null;
	private Intent i;
	private ActivityManager am;
	WakeLock wakelock;
	ToggleButton tbutton;
	
	ButtonView bview;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Replaced by manual text-rotate, easier to code
        /* if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("orientation", false))
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); */
        i = new Intent(DroidPadButtons.this,DroidPadServer.class);
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        wakelock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"DroidPad");
        wakelock.acquire();
        
        if(!isRunning("DroidPadServer"))
        {
        	finish();
        }
        else
        {
        	bind();
        }
    }
    private boolean isRunning(String Activity)
    {
        List<ActivityManager.RunningServiceInfo> sl = am.getRunningServices(Integer.MAX_VALUE);
        for(int i = 0;i < sl.size();i++)
        {
        	if(sl.get(i).service.getClassName().equals("uk.digitalsquid.droidpad." + Activity))
        	{
        		return true;
        	}
        }
        return false;
    }

    private ServiceConnection mConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            apsbound = ((DroidPadServer.LocalBinder)service).getService();
            
            bview = new ButtonView(DroidPadButtons.this, apsbound.mode);
            Log.v("DroidPad", "DPB: Using mode " + apsbound.mode);
            DroidPadButtons.this.setContentView(bview);
        }

        public void onServiceDisconnected(ComponentName className) {
            apsbound = null;
        }
    };

    private void bind()
    {
		Intent j = i;
        bindService(j,mConnection,0);
    }
    private void unbind()
    {
    	if(apsbound != null)
    	{
    		unbindService(mConnection);
    	}
    }
    @Override
    public void onDestroy()
    {
        if(isRunning("DroidPadServer"))
        {
        	unbind();
        }
        wakelock.release();
    	super.onDestroy();
    }
    
    public void sendEvent(Layout layout)
    {
    	apsbound.buttons = layout;
    }
}

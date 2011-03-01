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


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DroidPad extends Activity /* implements SharedPreferences.OnSharedPreferenceChangeListener */ {
	private Button startit, startb, enablewifi, disablewifi, changePref;
	private Intent i, j;
	/*private ImageView pmStat, mmStat, bmStat, ppStat;*/
	private TextView ontext, context, iptext, ipNWtext, localiptext, localwifitext;
	private DroidPadServer apsbound = null;
	private ActivityManager am;
	private boolean running;
	
	private String ipT;
	
	private WifiLock wL;
	private SharedPreferences prefs;
	private Menu mainMenu;
	WifiManager wm;
	
	private static final int DIALOG_ID_NO_ACCEL = 1;
	
	public static final int PURPOSE_SETUP = 1;
	public static final int PURPOSE_CALIBRATE = 2;
	
	// ACTIVITY
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("DroidPad", "DP: Hello!");
        setContentView(R.layout.main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
			copyfiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//prefs.edit().putInt("firstRun", 1).commit();
		if(prefs.getInt("firstRun", 1) == 1)
		{
			prefs.edit().putInt("firstRun", 0).commit();
			Log.v("DroidPad", "DP: First time, go to manual page");
			Log.v("DroidPad", "DP: ");
			startActivity(new Intent(this, DroidPadIntro.class));
		}
        
        startit = (Button)findViewById(R.id.Startit);
        startit.setOnClickListener(cl);
        startb = (Button)findViewById(R.id.LaunchButtons);
        startb.setOnClickListener(cl);
        enablewifi = (Button)findViewById(R.id.EnableWifi);
        enablewifi.setOnClickListener(cl);
        disablewifi = (Button)findViewById(R.id.DisableWifi);
        disablewifi.setOnClickListener(cl);
        changePref = (Button)findViewById(R.id.PrefButton);
        changePref.setOnClickListener(cl);
        
        /* pmStat = (ImageView)findViewById(R.id.PointerModeStat);
        mmStat = (ImageView)findViewById(R.id.MouseModeStat);
        bmStat = (ImageView)findViewById(R.id.BackwardsModeStat);
        ppStat = (ImageView)findViewById(R.id.PresenterStat); */
        
        ontext = (TextView)findViewById(R.id.Ontext);
        context = (TextView)findViewById(R.id.Context);
        
        

        iptext = (TextView)findViewById(R.id.IPText);
        ipNWtext = (TextView)findViewById(R.id.NWifiIPText);
        localiptext = (TextView)findViewById(R.id.LocalIPText);
        localwifitext = (TextView)findViewById(R.id.LocalWifiText);
        
        i = new Intent(DroidPad.this,DroidPadServer.class);
        j = new Intent(DroidPad.this,DroidPadButtons.class);
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        
		startb.setEnabled(false);
        
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sm.getSensorList(Sensor.TYPE_ACCELEROMETER).isEmpty())
        {
        	showDialog(DIALOG_ID_NO_ACCEL);
        	startit.setEnabled(false);
			Log.i("DroidPad", "DP: No accelerometer!");
        }
        if(isRunning("DroidPadServer"))
        {
			Log.v("DroidPad", "DP: DPS already running");
        	startit.setText("Stop");
	        ontext.setText("Status: On");
			startb.setEnabled(true);
	        bind();
	        running = true;
        }
        else
        {
        	startit.setText("Start");
	        running = false;
        }

        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wL = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "DroidPad");
		wL.acquire();
		//Toast.makeText(getBaseContext(), "Locking Wifi", Toast.LENGTH_SHORT).show();
		
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        wifiFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiListener, wifiFilter);
		
        wifiState();
        wifiConn();
        
        if(isRunning("DroidPadButtons"))
        {
        	startActivity(j);
        }
        
        /* if(prefs.getBoolean("pointermode", false))
        	pmStat.setImageResource(R.drawable.green);
        if(prefs.getBoolean("mousemode", false))
        	mmStat.setImageResource(R.drawable.green);
        if(prefs.getBoolean("backwardsmode", false))
        	bmStat.setImageResource(R.drawable.green);
        if(prefs.getBoolean("presenter", false))
        	ppStat.setImageResource(R.drawable.green);

        prefs.registerOnSharedPreferenceChangeListener(this); */
    }
    // END ACTIVITY
    
    // SCREEN CLICKS
    public OnClickListener cl = new OnClickListener()
    {
		@Override
		public void onClick(View v) {
			switch(v.getId())
			{
			case R.id.Startit:
				if(!running)
				{
					Log.i("DroidPad", "DP: Starting DroidPad");
					i = new Intent(DroidPad.this, DroidPadServer.class);
					i.putExtra("purpose", PURPOSE_SETUP);
					try
					{
						i.putExtra("interval", Integer.parseInt(prefs.getString("updateinterval", "20")));
					}
					catch (NumberFormatException e)
					{
						Toast.makeText(getBaseContext(), "In Preferences, \"Update Interval\" is not a number!", Toast.LENGTH_LONG).show();
						Log.w("DroidPad", "DP: Number format error");
						return;
					}
					
					try
					{
						i.putExtra("port", Integer.parseInt(prefs.getString("portnumber", "3141")));
					}
					catch (NumberFormatException e)
					{
						Toast.makeText(getBaseContext(), "In Preferences, \"Port\" is not a number!", Toast.LENGTH_LONG).show();
						Log.w("DroidPad", "DP: Number format error");
						return;
					}
					Log.v("DroidPad", "DP: Starting DPS service");
					startService(i);
					Log.v("DroidPad", "DP: DPS service started! (will start when loop is free)");
					startit.setText("Stop");
					startb.setEnabled(true);
					ontext.setText("Status: On");
					bind();
					Log.v("DroidPad", "DP: Bound to service");
					running = true;
					if(mainMenu != null)
					{
						mainMenu.getItem(1).setEnabled(false);
			    		mainMenu.getItem(2).setEnabled(true);
					}
				}
		        else
		        {
					Log.i("DroidPad", "DP: Stopping DroidPad");
		        	sendParentKill();
		        	unbind();
		        	i = new Intent(DroidPad.this, DroidPadServer.class);
		        	stopService(i);
					Log.v("DroidPad", "DP: Sucessfully killed DPS");
		        	startit.setText("Start");
					startb.setEnabled(false);
		        	ontext.setText("Status: Off");
		        	context.setText("Not Connected");
		        	iptext.setText("");
		        	ipNWtext.setText("");
		        	ipT = "";
		        	//sendParentKill();
					running = false;
					if(mainMenu != null)
					{
						mainMenu.getItem(1).setEnabled(true);
						mainMenu.getItem(2).setEnabled(false);
					}
		        }
				break;
			case R.id.LaunchButtons:
				startActivity(j);
				
				break;
			case R.id.EnableWifi:
				if(wm != null)
					wm.setWifiEnabled(true);
				break;
			case R.id.DisableWifi:
				if(wm != null)
					wm.setWifiEnabled(false);
				break;
			case R.id.PrefButton:
    			i = new Intent(DroidPad.this,SettingsMenu.class);
    			startActivity(i);
				break;
			}
		}
    };
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	menu.getItem(0).setIcon(R.drawable.wifiicon);
    	menu.getItem(1).setIcon(android.R.drawable.ic_menu_preferences);
    	menu.getItem(2).setIcon(R.drawable.options);
    	menu.getItem(2).setEnabled(running);
    	menu.getItem(3).setIcon(android.R.drawable.ic_menu_info_details);
    	menu.getItem(4).setIcon(android.R.drawable.ic_menu_help);
    	mainMenu = menu;
		return true;
    	
    }
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	if (!item.hasSubMenu())
    	{
    		Intent i;
    		switch (item.getItemId())
    		{
    		case R.id.wifi:
    			i = new Intent(Settings.ACTION_WIFI_SETTINGS);
    			try
    			{
    				startActivity(i);
    			}
    			catch (ActivityNotFoundException a)
    			{
    				Toast.makeText(getBaseContext(), "Could not launch Wifi settings.", Toast.LENGTH_SHORT).show();
    			}
    			break;
    		case R.id.website:
    			i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://digitalsquid.co.uk/droidpad/"));
    			try
    			{
    				startActivity(i);
    			}
    			catch (ActivityNotFoundException a)
    			{
    				Toast.makeText(getBaseContext(), "Could not launch Browser.", Toast.LENGTH_SHORT).show();
    			}
    			break;
    		case R.id.settings:
    			i = new Intent(DroidPad.this,SettingsMenu.class);
    			startActivity(i);
    			break;
    		case R.id.about:
    			i = new Intent(DroidPad.this, DroidPadAbout.class);
    			startActivity(i);
    			break;
    		case R.id.calibrate:
    			i = new Intent(DroidPad.this, DroidPadServer.class);
				i.putExtra("purpose", PURPOSE_CALIBRATE);
				startService(i);
				break;
    		}
    	}
		return true;
    }
    @Override 
    protected Dialog onCreateDialog(int id) { 
        if(id == DIALOG_ID_NO_ACCEL){
            return new AlertDialog.Builder(DroidPad.this)
            .setTitle("ERROR: Accelerometer not found!")
            .setPositiveButton("OK",new DialogInterface.OnClickListener()
            { 
                public void onClick(DialogInterface dialog, int which)
                {
					Log.v("DroidPad", "DP: Bye bye!");
                	finish();
                }
            }
            ) 
            .create(); 
        }
        return null; 
      } 
    // END SCREEN CLICKS
    
    /* // PREFERENCE CHANGES
	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key)
	{
		int red = R.drawable.red, green = R.drawable.green;
		
		if(prefs.getBoolean("pointermode", false))
       		pmStat.setImageResource(green);
		else
			pmStat.setImageResource(red);
		
		if(prefs.getBoolean("mousemode", false))
       		mmStat.setImageResource(green);
		else
			mmStat.setImageResource(red);
		
		if(prefs.getBoolean("backwardsmode", false))
       		bmStat.setImageResource(green);
		else
			bmStat.setImageResource(red);
		
		if(prefs.getBoolean("presenter", false))
       		ppStat.setImageResource(green);
		else
			ppStat.setImageResource(red);
	}
    // END PREFERENCE CHANGES */
    
	// WIFI - 'borrowed' from tricorder app.
    private BroadcastReceiver wifiListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i)
        {
        final String action = i.getAction();
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
            wifiConn();
        else if (action.equals(WifiManager.RSSI_CHANGED_ACTION))
            wifiConn();
        else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
            wifiConn();
        else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
            wifiConn();
        else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
        	wifiState();
        }
    };
    private void wifiState()
    {
        switch (wm.getWifiState())
        {
        case WifiManager.WIFI_STATE_DISABLED:
            localwifitext.setText("Disabled");
            
            enablewifi.setEnabled(true);
            enablewifi.setVisibility(View.VISIBLE);
            
            disablewifi.setEnabled(false);
            disablewifi.setVisibility(View.INVISIBLE);
            localiptext.setVisibility(View.INVISIBLE);
            break;
        case WifiManager.WIFI_STATE_DISABLING:
            localwifitext.setText("Disabling");
            
            enablewifi.setEnabled(false);
            enablewifi.setVisibility(View.VISIBLE);
            
            disablewifi.setEnabled(false);
            disablewifi.setVisibility(View.INVISIBLE);
            localiptext.setVisibility(View.INVISIBLE);
            updateIPT(false);
            break;
        case WifiManager.WIFI_STATE_ENABLED:
            localwifitext.setText("Enabled");
            
            enablewifi.setEnabled(false);
            enablewifi.setVisibility(View.INVISIBLE);
            
            disablewifi.setEnabled(true);
            disablewifi.setVisibility(View.VISIBLE);
            localiptext.setVisibility(View.VISIBLE);
            updateIPT(true);
            break;
        case WifiManager.WIFI_STATE_ENABLING:
            localwifitext.setText("Enabling");
            
            enablewifi.setEnabled(false);
            enablewifi.setVisibility(View.VISIBLE);
            
            disablewifi.setEnabled(false);
            disablewifi.setVisibility(View.INVISIBLE);
            localiptext.setVisibility(View.INVISIBLE);
            break;
        case WifiManager.WIFI_STATE_UNKNOWN:
            localwifitext.setText("not supported!");
            break;
        }
        localwifitext.setText("Wifi: " + localwifitext.getText());
    }
    private void wifiConn()
    {
        switch (wm.getConnectionInfo().getSupplicantState())
        {
        case ASSOCIATED:
            // Association completed.
            localiptext.setText("Connecting");
            break;
        case ASSOCIATING:
            // Trying to associate with a BSS/SSID. 
            localiptext.setText("Connecting");
            break;
        case COMPLETED:
            // All authentication completed. 
            // Trying to associate with a BSS/SSID.
        	int ip = wm.getConnectionInfo().getIpAddress();
        	
        	if(ip != 0)
        		
        		localiptext.setText("IP: " + ipTextFromByte(ipFromInt(ip)));
        	else
        		localiptext.setText("Connecting");
            
            break;
        case DISCONNECTED:
            // This state indicates that client is not associated, but is
            // likely to start looking for an access point.
            localiptext.setText("");
            break;
        case DORMANT:
            // An Android-added state that is reported when a client issues an explicit DISCONNECT command.
            localiptext.setText("");
            break;
        case FOUR_WAY_HANDSHAKE:
        case GROUP_HANDSHAKE:
            // WPA 4-Way Key Handshake in progress.
            // WPA Group Key Handshake in progress.
            localiptext.setText("Connecting");
            break;
        case INACTIVE:
            // Inactive state (wpa_supplicant disabled).
            localiptext.setText("");
            break;
        case INVALID:
            // A pseudo-state that should normally never be seen.
            localiptext.setText("");
            break;
        case SCANNING:
            // Scanning for a network.
            localiptext.setText("Scanning");
            break;
        case UNINITIALIZED:
            // No connection to wpa_supplicant.
            localiptext.setText("");
            break;
        }
    }
	//END WIFI
	
    // PRIVATE COMMANDS
    private void bind()
    {
    	i = new Intent(DroidPad.this, DroidPadServer.class);
        bindService(i,mConnection,0);
    }
    private void unbind()
    {
    	if(apsbound != null)
    	{
    		unbindService(mConnection);
    	}
    }
    private void sendParent()
    {
    	//bind();

    	if(apsbound != null)
    	{
    		apsbound.setParentObject(this);
    	}
    	//unbind();
    }
    private void sendParentKill()
    {
    	//bind();
    	if(apsbound != null)
    	{
    		apsbound.setParentObject(null);
    	}
    	//unbind();
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
    // END PRIVATE COMMANDS
    
    // BINDER
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            apsbound = ((DroidPadServer.LocalBinder)service).getService();
            sendParent();
        }

        public void onServiceDisconnected(ComponentName className) {
            apsbound = null;
        }
    };
    public Handler isConnectedCallback = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
    		super.handleMessage(msg);
    		if(msg.arg1 == 1)
    		{
    			context.setText("Connected");
	        	if(msg.getData().getString("ip").startsWith("127"))
	        	{
	        		ipT = "Connected via USB";
	        		updateIPT(wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED);
	        	}
	        	else if(msg.getData().getString("ip") == "0.0.0.0")
	        	{
	        		ipT = "";
	        		updateIPT(wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED);
	        	}
	        	else
	        	{
	        		ipT = "Remote IP: " + msg.getData().getString("ip");
	        		updateIPT(wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED);
	        	}
	        	startActivity(new Intent(DroidPad.this, DroidPadButtons.class));
    		}
    		else
    		{
    			context.setText("Not Connected");
		        ipT = "";
        		updateIPT(wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED);
		        
    		}
			Log.v("DroidPad", "DP: IP text set");
    	}
    };
    //END BINDER
    @Override
    public void onDestroy()
    {
        unregisterReceiver(wifiListener);
        
        wL.release();
		//Toast.makeText(getBaseContext(), "Unlocking Wifi", Toast.LENGTH_SHORT).show();
		Log.i("DroidPad", "DP: Bye bye!");
    	super.onDestroy();
    }
    public static final int[] ipFromInt(int value)
    {
    	int[] b = new int[4];
    	b[0] = (value & 0xFF000000) >> 24;
    	b[1] = (value & 0x00FF0000) >> 16;
    	b[2] = (value & 0x0000FF00) >> 8;
    	b[3] = (value & 0x000000FF);
        return b;
    }
    public static final String ipTextFromByte(int[] ip)
    {
    	return String.valueOf(ip[3]) + "." + String.valueOf(ip[2]) + "." + String.valueOf(ip[1]) + "." + String.valueOf(ip[0]);
    }
    private void copyfiles() throws IOException
    {
    	String dataDir = getFileStreamPath("").getAbsolutePath() + "/";
    	int curVer = 0;
    	try {
			curVer = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	if(prefs.getInt("currentHelpVer", 0) < curVer)
    	{
    		//Toast.makeText(this, "Help out of date", Toast.LENGTH_LONG).show();
    		prefs.edit().putInt("currentHelpVer", curVer).commit();
    		Log.i("DroidPad", "DP: First time (or newer), so copying help files");
    	}
    	else return;
		copyfile(dataDir + "iconlarge.png", R.raw.iconlarge);
		copyfile(dataDir + "index.html", R.raw.index);
		copyfile(dataDir + "more.html", R.raw.more);
		copyfile(dataDir + "instinst.html", R.raw.instinst);
    }
    private void copyfile(String fullPath, int RRaw) throws IOException
    {
    	InputStream is = getResources().openRawResource(RRaw);
    	byte[] buffer = new byte[1024];
        int read;
        OutputStream os = null;
        os = new FileOutputStream(fullPath);
	    while ((read = is.read(buffer)) > 0) {
	      os.write(buffer, 0, read);
	    }
	    os.close();
	    is.close();
		Log.v("DroidPad", "DP: Copied " + fullPath);
    }
    
    private void updateIPT(boolean wifiOn)
    {
    	if(wifiOn)
    	{
    		iptext.setText(ipT);
    		ipNWtext.setText("");
    	}
    	else
    	{
    		ipNWtext.setText(ipT);
    		iptext.setText("");
    	}
    }
}

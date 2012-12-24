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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingsMenu extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settingsmenu);
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.settings_menu, menu);
		return true;
    	
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	if (!item.hasSubMenu()) {
    		Intent i;
    		switch (item.getItemId()) {
    		case R.id.enableDevMode:
    			try {
	    			i = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
	    			startActivity(i);
	    			Toast.makeText(this, R.string.enableUsbDebugRequest, Toast.LENGTH_LONG).show();
    			} catch(ActivityNotFoundException e) {
    				Log.w("Couldn't open dev settings activity", e);
    				Toast.makeText(this, "Couldn't open development settings", Toast.LENGTH_LONG).show();
    			}
    			break;
    		}
    	}
		return true;
    }
}

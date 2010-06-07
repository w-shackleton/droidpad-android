package uk.digitalsquid.droidpad;

import uk.digitalsquid.droidpad.R;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsMenu extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settingsmenu);
		SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		try
		{
			sm.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
		}
		catch(IndexOutOfBoundsException e)
		{
			findPreference("pointermode").setEnabled(false);
		}
    }
}

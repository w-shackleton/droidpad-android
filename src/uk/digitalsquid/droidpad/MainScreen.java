package uk.digitalsquid.droidpad;

import java.util.List;

import uk.digitalsquid.droidpad.buttons.Layout;
import uk.digitalsquid.droidpad.buttons.ModeSpec;
import android.app.TabActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the new, much improved main UI for DroidPad.
 * @author william
 *
 */
public class MainScreen extends TabActivity implements OnClickListener, OnItemClickListener /*, LogTag */ {
	
	private static final String TAG = "DroidPad"; // Until we import trunk
	
	TabHost tabHost;
	
	// Global state, for getting layouts. In the future, these could also be user defined.
	App app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (App) getApplication();
		setContentView(R.layout.main2);
		// tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost = getTabHost();
		
		// set up tabs
		TabSpec spec;
		spec = tabHost.newTabSpec("jsTab");
		spec.setIndicator(
				getResources().getString(R.string.js),
				getResources().getDrawable(R.drawable.ic_tab_js));
		spec.setContent(R.id.jsTab);
		tabHost.addTab(spec);
		
		// Also create the list adapter for each tab.
		jsList = (ListView) findViewById(R.id.jsList);
		jsModes = new ModeListAdapter(app.getLayouts(ModeSpec.LAYOUTS_JS));
		jsList.setAdapter(jsModes);
		jsList.setOnItemClickListener(this);
		
		spec = tabHost.newTabSpec("mouseTab");
		spec.setIndicator(
				getResources().getString(R.string.mouse),
				getResources().getDrawable(R.drawable.ic_tab_mouse));
		spec.setContent(R.id.mouseTab);
		tabHost.addTab(spec);
		
		mouseList = (ListView) findViewById(R.id.mouseList);
		mouseModes = new ModeListAdapter(app.getLayouts(ModeSpec.LAYOUTS_MOUSE));
		mouseList.setAdapter(mouseModes);
		mouseList.setOnItemClickListener(this);
		
		spec = tabHost.newTabSpec("slideTab");
		spec.setIndicator(
				getResources().getString(R.string.slideshow),
				getResources().getDrawable(R.drawable.ic_tab_slide));
		spec.setContent(R.id.slideTab);
		tabHost.addTab(spec);
		
		slideList = (ListView) findViewById(R.id.slideList);
		slideModes = new ModeListAdapter(app.getLayouts(ModeSpec.LAYOUTS_SLIDE));
		slideList.setAdapter(slideModes);
		slideList.setOnItemClickListener(this);
	}
	
	@Override
	public void onClick(View arg0) {
	}
	
	private ModeListAdapter jsModes, mouseModes, slideModes;
	private ListView jsList, slideList, mouseList;
	
	private class ModeListAdapter extends BaseAdapter {
		private final LayoutInflater inflater;
		
		private List<Layout> modes;
		
		public ModeListAdapter(List<Layout> modes) {
			this.modes = modes;
			inflater = LayoutInflater.from(MainScreen.this);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = inflater.inflate(R.layout.layoutitem, null);
	        }
	        
	        TextView title = (TextView) convertView.findViewById(R.id.title);
	        TextView description = (TextView) convertView.findViewById(R.id.description);
	        
        	title.setText(modes.get(position).getTitle());
        	description.setText(modes.get(position).getDescription());

            return convertView;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public Layout getItem(int position) {
			if(modes == null) return null;
			return modes.get(position);
		}
		
		@Override
		public int getCount() {
			if(modes == null) return 0;
			return modes.size();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		int type = -1;
		Layout layout = (Layout) parent.getItemAtPosition(position);
		if(parent.equals(jsList))
			type = ModeSpec.LAYOUTS_JS;
		else if(parent.equals(mouseList))
			type = ModeSpec.LAYOUTS_MOUSE;
		else if(parent.equals(slideList))
			type = ModeSpec.LAYOUTS_SLIDE;
		Log.v(TAG, "Using layout type " + type + ", \"" + layout.getTitle() + "\".");
		
		Intent intent = new Intent(this, Buttons.class);
		ModeSpec spec = new ModeSpec();
		spec.setLayout(layout);
		spec.setMode(type);
		intent.putExtra(Buttons.MODE_SPEC, spec);
		startActivity(intent);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
		return true;
    	
    }
    
    @Override
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
    			i = new Intent(this, SettingsMenu.class);
    			startActivity(i);
    			break;
    		case R.id.about:
    			i = new Intent(this, AboutActivity.class);
    			startActivity(i);
    			break;
    		}
    	}
		return true;
    }
}

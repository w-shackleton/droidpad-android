package uk.digitalsquid.droidpad;

import java.util.List;

import uk.digitalsquid.droidpad.buttons.Layout;
import android.app.TabActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
		spec.setIndicator("JS (TODO: Pic)");
		spec.setContent(R.id.jsTab);
		tabHost.addTab(spec);
		
		// Also create the list adapter for each tab.
		jsList = (ListView) findViewById(R.id.jsList);
		jsModes = new ModeListAdapter(app.getLayouts(App.LAYOUTS_JS));
		jsList.setAdapter(jsModes);
		jsList.setOnItemClickListener(this);
		
		spec = tabHost.newTabSpec("mouseTab");
		spec.setIndicator("Mouse");
		spec.setContent(R.id.mouseTab);
		tabHost.addTab(spec);
		
		mouseList = (ListView) findViewById(R.id.mouseList);
		mouseModes = new ModeListAdapter(app.getLayouts(App.LAYOUTS_MOUSE));
		mouseList.setAdapter(mouseModes);
		mouseList.setOnItemClickListener(this);
		
		spec = tabHost.newTabSpec("slideTab");
		spec.setIndicator("Slideshow");
		spec.setContent(R.id.slideTab);
		tabHost.addTab(spec);
		
		slideList = (ListView) findViewById(R.id.slideList);
		slideModes = new ModeListAdapter(app.getLayouts(App.LAYOUTS_SLIDE));
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
			type = App.LAYOUTS_JS;
		else if(parent.equals(mouseList))
			type = App.LAYOUTS_MOUSE;
		else if(parent.equals(slideList))
			type = App.LAYOUTS_SLIDE;
		Log.v(TAG, "Using layout type " + type + ", \"" + layout.getTitle() + "\".");
	}
}

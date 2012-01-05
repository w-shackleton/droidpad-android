package uk.digitalsquid.droidpad;

import java.util.List;

import uk.digitalsquid.droidpad.buttons.Layout;
import android.app.TabActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
public class MainScreen extends TabActivity implements OnClickListener {
	
	TabHost tabHost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main2);
		// tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost = getTabHost();
		
		// set up tabs
		TabSpec spec;
		spec = tabHost.newTabSpec("jsTab");
		spec.setIndicator("JS (TODO: Pic)");
		spec.setContent(R.id.jsTab);
		tabHost.addTab(spec);
		
		ListView jsList = (ListView) findViewById(R.id.jsList);
		jsModes = new ModeListAdapter(null); // TODO: Add!
		jsList.setAdapter(jsModes);
		
		spec = tabHost.newTabSpec("mouseTab");
		spec.setIndicator("Mouse");
		spec.setContent(R.id.mouseTab);
		tabHost.addTab(spec);
		
		ListView mouseList = (ListView) findViewById(R.id.mouseList);
		mouseModes = new ModeListAdapter(null); // TODO: Add!
		mouseList.setAdapter(mouseModes);
		
		spec = tabHost.newTabSpec("slideTab");
		spec.setIndicator("Slideshow");
		spec.setContent(R.id.slideTab);
		tabHost.addTab(spec);
		
		ListView slideList = (ListView) findViewById(R.id.slideList);
		slideModes = new ModeListAdapter(null); // TODO: Add!
		slideList.setAdapter(slideModes);
	}
	
	@Override
	public void onClick(View arg0) {
	}
	
	private ModeListAdapter jsModes, mouseModes, slideModes;
	
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
	        TextView description = (TextView) convertView.findViewById(R.id.title);
	        
        	title.setText(modes.get(position).title);
        	description.setText(modes.get(position).description);

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
			if(modes == null) return 1;
			return modes.size();
		}
	}

}

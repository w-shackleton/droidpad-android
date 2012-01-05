package uk.digitalsquid.droidpad;

import android.app.TabActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

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
		
		spec = tabHost.newTabSpec("mouseTab");
		spec.setIndicator("Mouse");
		spec.setContent(R.id.mouseTab);
		tabHost.addTab(spec);
		
		spec = tabHost.newTabSpec("slideTab");
		spec.setIndicator("Slideshow");
		spec.setContent(R.id.slideTab);
		tabHost.addTab(spec);
	}
	
	@Override
	public void onClick(View arg0) {
	}
}

package uk.digitalsquid.droidpad2.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.digitalsquid.droidpad2.LogTag;
import uk.digitalsquid.droidpad2.buttons.Layout;
import uk.digitalsquid.droidpad2.buttons.ModeSpec;
import android.content.Context;
import android.os.Environment;
import android.util.Log;


/**
 * Scans the given folder for custom layouts then loads them.
 * @author william
 *
 */
public class Scanner implements LogTag {
	
	private final Context context;
	
	public Scanner(Context context) {
		this.context = context;
	}
	
	private List<Layout> jsModes;
	private List<Layout> mouseModes;
	private List<Layout> slideshowModes;
	
	private void scanForLayouts() throws IOException {
		jsModes = new ArrayList<Layout>();
		mouseModes = new ArrayList<Layout>();
		slideshowModes = new ArrayList<Layout>();
		
		if(!isExternalStorageReadable()) {
			return;
		}
		
		File sd = Environment.getExternalStorageDirectory();
		File jsFolder = new File(sd, String.format("/Android/data/%s/Custom layouts", context.getPackageName()));
		jsFolder.mkdirs();
		Log.i(TAG, "Checking for files in " + jsFolder.getAbsolutePath());
		File[] layouts = jsFolder.listFiles();
		if(layouts == null) {
			Log.w(TAG, jsFolder.getAbsolutePath() + " isn't a folder");
			jsModes = new ArrayList<Layout>();
			mouseModes = new ArrayList<Layout>();
			slideshowModes = new ArrayList<Layout>();
			return;
		}
		
		for(File layout : layouts) {
			try {
				ModeSpec spec = LayoutDecoder.decodeLayout(new FileInputStream(layout));
				switch(spec.getMode()) {
				case ModeSpec.LAYOUTS_JS:
					jsModes.add(spec.getLayout());
					break;
				case ModeSpec.LAYOUTS_MOUSE:
				case ModeSpec.LAYOUTS_MOUSE_ABS:
					mouseModes.add(spec.getLayout());
					break;
				case ModeSpec.LAYOUTS_SLIDE:
					slideshowModes.add(spec.getLayout());
					break;
				}
			}
			catch(IOException e) {
				Log.e(TAG, "Failed to parse custom XML layout", e);
			}
		}
	}
	
	private boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state))
			return true;
		if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
			return true;
		return false;
	}
	
	/**
	 * Rescans the SD card folder
	 * @return <code>true</code> on success
	 */
	public boolean rescan() {
		try {
			scanForLayouts();
			return true;
		} catch (IOException e) {
			Log.w(TAG, "Failed to scan custom layouts", e);
			return false;
		}
	}

	public List<Layout> getJsModes() {
		if(jsModes == null) rescan();
		return jsModes;
	}

	public List<Layout> getMouseModes() {
		if(mouseModes == null) rescan();
		return mouseModes;
	}

	public List<Layout> getSlideshowModes() {
		if(slideshowModes == null) rescan();
		return slideshowModes;
	}
}

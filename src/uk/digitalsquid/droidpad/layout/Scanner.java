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

package uk.digitalsquid.droidpad.layout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.digitalsquid.droidpad.LogTag;
import uk.digitalsquid.droidpad.buttons.Layout;
import uk.digitalsquid.droidpad.buttons.ModeSpec;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


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
				ModeSpec spec = decodeLayout(layout);
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
				Toast.makeText(context, "Failed to load custom layout " + layout.getName(), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	static final ModeSpec decodeLayout(File layout) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(layout));
		reader.mark(256);
		char[] head = new char[1];
		reader.read(head);
		reader.reset();
		switch(head[0]) {
		case '<': // XML
			return XmlDecoder.decodeLayout(reader);
		case '{': // JSON
			return JsonDecoder.decodeLayout(reader);
		default: // Not sure; try both
			try {
				return XmlDecoder.decodeLayout(reader);
			} catch(IOException e) {
				Log.w(TAG, "File " + layout.getAbsolutePath() + " couldn't be decoded as XML");
			}
			return JsonDecoder.decodeLayout(reader);
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

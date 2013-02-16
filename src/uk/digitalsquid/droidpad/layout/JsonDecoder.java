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

import java.io.IOException;
import java.io.Reader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.digitalsquid.droidpad.LogTag;
import uk.digitalsquid.droidpad.buttons.Layout;
import uk.digitalsquid.droidpad.buttons.ModeSpec;

public class JsonDecoder implements LogTag {
	
	public static final ModeSpec decodeLayout(Reader reader) throws IOException {
		StringBuffer contents = new StringBuffer();
		char[] buf = new char[1024];
		int read = -1;
		while((read = reader.read(buf)) != -1) {
			contents.append(buf, 0, read);
		}
		try {
			return decodeLayout(contents.toString());
		} catch (JSONException e) {
			IOException e1 = new IOException("Failed to parse JSON string from reader");
			e1.initCause(e);
			throw e1;
		}
	}
	
	public static final ModeSpec decodeLayout(String input) throws JSONException {
		JSONObject root = new JSONObject(input);
		ModeSpec result = new ModeSpec();
		
		result.setMode(getModeId(root.optString("mode")));
		
		// TODO: Check these against editor
		String title = root.optString("title", "Custom Layout");
		String description = root.optString("description", "A new custom layout");
		int width = root.optInt("width", 4);
		int height = root.optInt("", 5);
		Layout layout = new Layout(title, description, width, height);
		result.setLayout(layout);
		
		JSONArray items = root.getJSONArray("items");
		for(int i = 0; i < items.length(); i++) {
			JSONObject item = items.getJSONObject(i);
			// TODO: Decode item here
		}
		
		return result;
	}
	
	static final int getModeId(String mode) {
		// TODO: check these against editor
		if(mode.equalsIgnoreCase("joystick")) return ModeSpec.LAYOUTS_JS;
		if(mode.equalsIgnoreCase("mouse")) return ModeSpec.LAYOUTS_MOUSE;
		if(mode.equalsIgnoreCase("touch")) return ModeSpec.LAYOUTS_MOUSE_ABS;
		if(mode.equalsIgnoreCase("slideshow")) return ModeSpec.LAYOUTS_SLIDE;
		return ModeSpec.LAYOUTS_JS;
	}
}

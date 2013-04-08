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
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.digitalsquid.droidpad.LogTag;
import uk.digitalsquid.droidpad.buttons.Button;
import uk.digitalsquid.droidpad.buttons.Item;
import uk.digitalsquid.droidpad.buttons.Layout;
import uk.digitalsquid.droidpad.buttons.ModeSpec;
import uk.digitalsquid.droidpad.buttons.Orientation;
import uk.digitalsquid.droidpad.buttons.Slider;
import uk.digitalsquid.droidpad.buttons.ToggleButton;
import uk.digitalsquid.droidpad.buttons.TouchPanel;

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
		
		String title = root.optString("name", "Custom Layout");
		String description = root.optString("description", "A new custom layout");
		int gridX = root.optInt("gridX", 4);
		int gridY = root.optInt("gridY", 5);
		// Editor size used to work out orientation
		int editorWidth = root.optInt("width", 720);
		int editorHeight = root.optInt("height", 405);
		boolean horizontal = editorWidth > editorHeight;
		Layout layout = new Layout(title, description, gridX, gridY);
		layout.setActivityHorizontal(horizontal);
		result.setLayout(layout);
		
		JSONArray items = root.getJSONArray("items");
		for(int i = 0; i < items.length(); i++) {
			JSONObject item = items.getJSONObject(i);
			String type = item.optString("type", "button");
			boolean gridSnap = item.optBoolean("gridSnap", true);
			// These values are integers if grid snapping, floats if not
			float x = (float)item.optDouble("x", 0);
			float y = (float)item.optDouble("y", 0);
			float width = (float)item.optDouble("width", 1);
			float height = (float)item.optDouble("height", 1);
			int textSize = item.optInt("textSize", 20);
			String text = item.optString("text");
			if(text.equals("")) text = String.format(Locale.getDefault(), "%d", i+1);
			
			Orientation orientation = Orientation.Both;
			String orientationType = item.optString("orientation", "both");
			if(orientationType.equals("both")) orientation = Orientation.Both;
			if(orientationType.equals("x")) orientation = Orientation.X;
			if(orientationType.equals("y")) orientation = Orientation.Y;
			
			Item component = null;
			if(type.equals("button")) {
				component = new Button(x, y, width, height, !gridSnap, text, textSize);
			} else if(type.equals("toggle")) {
				component = new ToggleButton(x, y, width, height, !gridSnap, text, textSize);
			} else if(type.equals("slider")) {
				component = new Slider(x, y, width, height, !gridSnap, orientation);
			} else if(type.equals("panel")) {
				component = new TouchPanel(x, y, width, height, !gridSnap, orientation);
			}
			if(component != null) layout.add(component);
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

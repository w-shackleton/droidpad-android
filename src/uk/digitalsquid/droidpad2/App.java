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

package uk.digitalsquid.droidpad2;

import java.util.ArrayList;
import java.util.List;

import uk.digitalsquid.droidpad2.buttons.Button;
import uk.digitalsquid.droidpad2.buttons.Item;
import uk.digitalsquid.droidpad2.buttons.Layout;
import uk.digitalsquid.droidpad2.buttons.ModeSpec;
import uk.digitalsquid.droidpad2.buttons.Slider;
import uk.digitalsquid.droidpad2.buttons.Slider.SliderType;
import uk.digitalsquid.droidpad2.buttons.ToggleButton;
import uk.digitalsquid.droidpad2.buttons.TouchPanel;
import uk.digitalsquid.droidpad2.buttons.TouchPanel.PanelType;
import android.app.Application;

/**
 * Global session things, such as (new) button layout mechanism
 * @author william
 *
 */
public class App extends Application {
	private List<Layout> getJSLayouts() {
		List<Layout> ret = new ArrayList<Layout>();
		ret.add(new Layout(new Item[] {
				new Button(0, 0, 4, 3, "1", 30),
				new Button(0, 3, 4, 2, "2", 30),
		}));
		ret.add(new Layout(new Item[] {
				new Button(0, 0, 4, 2, "1", 30),
				new Button(0, 2, 2, 3, "2", 30),
				new Button(2, 2, 2, 3, "3", 30),
		}));
		ret.add(new Layout(new Item[] {
				new Button(0, 0, 2, 2, "1", 30),
				new Button(2, 0, 2, 2, "2", 30),
				new Button(0, 2, 2, 3, "3", 30),
				new Button(2, 2, 2, 3, "4", 30),
		}));
		ret.add(new Layout(new Item[] {
				new Button(0, 0, 2, 2, "1", 30),
				new Button(2, 0, 2, 2, "2", 30),
				new Button(0, 2, 2, 3, "3", 30),
				new Button(2, 2, 2, 2, "4", 30),
				new Button(2, 4, 2, 1, "5", 30),
		}));
		ret.add(new Layout(new Item[] {
				new Button(1, 0, 2, 1, "1", 30),
				new Slider(1, 1, 2, 2, SliderType.Both),
				new Slider(0, 1, 1, 2, SliderType.Y),
				new Button(3, 0, 1, 1, "2", 30),
				new Button(0, 0, 1, 1, "3", 30),
				new Button(3, 1, 1, 2, "4", 30),
				new Button(0, 3, 1, 1, "5", 30),
				new ToggleButton(1, 3, 1, 1, "6", 30),
				new ToggleButton(2, 3, 1, 1, "7", 30),
				new Button(3, 3, 1, 1, "8", 30),
				
				new ToggleButton(0, 4, 1, 1, "9", 30),
				new Button(1, 4, 2, 1, "10", 30),
				new Button(3, 4, 1, 1, "11", 30),
		}));
		ret.add(new Layout(new Item[] {
				new Slider(0, 1, 4, 3, SliderType.Both),
				
				new Button(0, 4, 1, 1, "1", 30),
				new Button(1, 4, 2, 1, "2", 30),
				new Button(3, 4, 1, 1, "3", 30),

				new Button(0, 0, 1, 1, "4", 30),
				new Button(1, 0, 1, 1, "5", 30),
				new Button(2, 0, 1, 1, "6", 30),
				new Button(3, 0, 1, 1, "7", 30),
		}));
		ret.add(new Layout(new Item[] {
				new Slider(0, 1, 4, 3, SliderType.Both),
				
				new Button(0, 4, 1, 1, "1", 30),
				new Button(1, 4, 2, 1, "2", 30),
				new ToggleButton(3, 4, 1, 1, "3", 30),

				new ToggleButton(0, 0, 1, 1, "4", 30),
				new Button(1, 0, 1, 1, "5", 30),
				new Button(2, 0, 1, 1, "6", 30),
				new ToggleButton(3, 0, 1, 1, "7", 30),
		}));
		ret.add(new Layout(new Item[] {
				new Slider(1, 1, 2, 2, SliderType.Both),
				
				new Slider(1, 0, 2, 1, SliderType.X),
				new Slider(0, 1, 1, 2, SliderType.Y),
				new Slider(1, 3, 2, 1, SliderType.X),
				new Slider(3, 1, 1, 2, SliderType.Y),
				
				new Button(0, 3, 1, 1, "1", 30),
				new Button(3, 3, 1, 1, "2", 30),

				new Button(0, 4, 1, 1, "3", 30),
				new Button(1, 4, 1, 1, "4", 30),
				new Button(2, 4, 1, 1, "5", 30),
				new Button(3, 4, 1, 1, "6", 30),
				
				new ToggleButton(0, 0, 1, 1, "7", 30),
				new ToggleButton(3, 0, 1, 1, "8", 30),
		}));
		return ret;
	}
	private List<Layout> getMouseLayouts() {
		List<Layout> ret = new ArrayList<Layout>();
		ret.add(new Layout(R.string.layout_mouse_simple, R.string.layout_mouse_simple_desc, 5, 5, new Item[] {
				new Button(0, 0, 2, 5, "Left"),
				new Button(2, 0, 1, 2, "Middle"),
				new TouchPanel(2, 2, 1, 3, PanelType.Y),
				new Button(3, 0, 2, 5, "Right"),
		}));
		ret.add(new Layout(R.string.layout_mouse_adv, R.string.layout_mouse_adv_desc, Layout.EXTRA_MOUSE_TRACKPAD, 5, 8, new Item[] {
				new TouchPanel(0, 0, 5, 5, PanelType.Both),
				new Button(0, 5, 2, 3, "Left"),
				new Button(2, 5, 1, 1, "Middle"),
				new TouchPanel(2, 6, 1, 2, PanelType.Y),
				new Button(3, 5, 2, 3, "Right"),
		}));
		ret.add(new Layout(R.string.layout_mouse_abs, R.string.layout_mouse_abs_desc, Layout.EXTRA_MOUSE_ABSOLUTE, 5, 5, new Item[] {
				new Button(0, 0, 2, 5, "Left"),
				new Button(2, 0, 1, 2, "Middle"),
				new TouchPanel(2, 2, 1, 3, PanelType.Y),
				new Button(3, 0, 2, 4, "Right"),
				new Button(3, 4, 2, 1, "Reset").setResetButton(true),
		}));
		return ret;
	}
	private List<Layout> getSlideLayouts() {
		List<Layout> ret = new ArrayList<Layout>();
		ret.add(new Layout(R.string.layout_slide, R.string.layout_slide_desc, 4, 5, new Item[] {
				new Button(0, 3, 4, 2, "Next slide", 30),
				new Button(1, 2, 2, 1, "Prev slide", 16),
				new Button(0, 2, 1, 1, "Start"),
				new Button(3, 2, 1, 1, "End"),
				new ToggleButton(3, 0, 1, 2, "White", 0),
				new ToggleButton(2, 0, 1, 2, "Black", 0),
				new Button(0, 0, 2, 1, "Beginning"),
				new Button(0, 1, 2, 1, "End"),
		}));
		return ret;
	}
	
	/**
	 * Gets the layouts with the title & description filled in. Makes them up if they aren't present.
	 * @param type
	 * @return
	 */
	public final List<Layout> getLayouts(int type) {
		List<Layout> ret;
		switch(type) {
		case ModeSpec.LAYOUTS_JS:
			ret = getJSLayouts();
			break;
		case ModeSpec.LAYOUTS_MOUSE:
			ret = getMouseLayouts();
			break;
		case ModeSpec.LAYOUTS_SLIDE:
			ret = getSlideLayouts();
			break;
		default:
			throw new IllegalArgumentException("Invalid value for 'type'");
		}
		
		// Used to label unlabelled modes.
		int posCounter = 0;
		for(Layout l : ret) {
			posCounter++;
			if(l.titleId != -1) {
				l.setTitle(getString(l.titleId));
			} else {
				String name = "???";
				switch(type) { // Get class name
				case ModeSpec.LAYOUTS_JS:
					name = "Joystick"; // i18nise?
					break;
				case ModeSpec.LAYOUTS_MOUSE:
					name = "Mouse";
					break;
				case ModeSpec.LAYOUTS_SLIDE:
					name = "Slideshow";
					break;
				}
				l.setTitle(getString(R.string.generic_layout_title, name, posCounter));
			}
			
			if(l.descriptionId != -1) {
				l.setDescription(getString(l.descriptionId));
			} else {
				int buttonCount = 0;
				int sliderCount = 0;
				int trackCount = 0;
				for(Item i : l) {
					if(i instanceof Button)
						buttonCount++;
					if(i instanceof Slider)
						sliderCount++;
					if(i instanceof TouchPanel)
						trackCount++;
				}
				l.setDescription(getString(R.string.generic_layout_description, buttonCount, sliderCount, trackCount));
			}
		}
		return ret;
	}
}

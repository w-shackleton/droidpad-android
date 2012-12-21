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

package uk.digitalsquid.droidpad2.buttons;

import java.io.Serializable;

/**
 * Specifies how DroidPad will run.
 * @author william
 *
 */
public class ModeSpec implements Serializable {

	private static final long serialVersionUID = 4294164511803037163L;

	private Layout layout;
	
	private int mode;
	
	private boolean landscape;
	
	public static final int LAYOUTS_SLIDE = 4;
	public static final int LAYOUTS_MOUSE = 3;
	/**
	 * Absolute mouse positioning
	 */
	public static final int LAYOUTS_MOUSE_ABS = 2;
	public static final int LAYOUTS_JS = 1;
	public Layout getLayout() {
		return layout;
	}
	public void setLayout(Layout layout) {
		this.layout = layout;
	}
	public int getMode() {
		return mode;
	}
	public String getModeString() {
		switch(mode) {
		case LAYOUTS_JS:
			return "1"; // Compatibility - old version assumes number = js mode
		case LAYOUTS_MOUSE:
			return "mouse";
		case LAYOUTS_MOUSE_ABS:
			return "absmouse";
		case LAYOUTS_SLIDE:
			return "slide";
		}
		return "other";
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	public boolean isLandscape() {
		return landscape;
	}
	public void setLandscape(boolean landscape) {
		this.landscape = landscape;
	}
}

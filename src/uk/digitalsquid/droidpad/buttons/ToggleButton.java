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

package uk.digitalsquid.droidpad.buttons;

import uk.digitalsquid.droidpad.LogTag;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

public class ToggleButton extends Button implements LogTag {

	private static final long serialVersionUID = -6001760405211069340L;

	public ToggleButton(int x, int y, int sx, int sy, String text) {
		super(x, y, sx, sy, text);
	}

	public ToggleButton(int x, int y, int sx, int sy, String text, int textSize) {
		super(x, y, sx, sy, text, textSize);
	}
	
	@Override
	public void drawInArea(Canvas c, RectF area, Point centre, boolean landscape) {
		super.drawInArea(c, area, centre, landscape);
		
		c.drawCircle(area.right - 10, area.bottom - 10, 5, isSelected() ? pTextS : pThinBorder);
	}
	
	@Override
	public void onMouseOn(float x, float y) {
		// Do nothing.
	}
	
	@Override
	public void onMouseOff() {
		super.onMouseOff();
		tmpSelected = !tmpSelected;
		selected = !selected;
		Log.v(TAG, "Released");
	}
	
	@Override
	public void resetStickyLock() {
		
	}
}

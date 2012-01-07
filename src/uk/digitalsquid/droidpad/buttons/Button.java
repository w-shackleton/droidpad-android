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

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;

public class Button extends Item {
	private static final long serialVersionUID = -7921469580817352801L;
	
	public final String text;
	public final int textSize;
	
	protected boolean tmpSelected = false;

	public Button(int x, int y, int sx, int sy, String text) {
		this(x, y, sx, sy, text, TEXT_SIZE);
	}

	public Button(int x, int y, int sx, int sy, String text, int textSize) {
		super(x, y, sx, sy);
		this.text = text;
		this.textSize = textSize == 0 ?  TEXT_SIZE : textSize;
	}

	@Override
	public void drawInArea(Canvas c, RectF area, Point centre, boolean landscape) {
		pTextS.setTextSize(textSize);
		pText.setTextSize(textSize);
		if(landscape)
		{
			c.rotate(90, centre.x, centre.y);
			c.drawText(text, centre.x, centre.y + (TEXT_SIZE / 2), isSelected() ? pTextS : pText);
			c.rotate(-90, centre.x, centre.y);
		}
		else
			c.drawText(text, centre.x, centre.y + (TEXT_SIZE / 2), isSelected() ? pTextS : pText);
	}

	@Override
	public String getOutputString() {
		return selected ? "1" : "0";
	}

	@Override
	public void resetStickyLock() {
		tmpSelected = false;
	}

	@Override
	public void finaliseState() {
		selected = tmpSelected;
	}

	@Override
	public void onMouseOn(float x, float y) {
		tmpSelected = true;
	}

	@Override
	public void onMouseOff() {
		
	}
}

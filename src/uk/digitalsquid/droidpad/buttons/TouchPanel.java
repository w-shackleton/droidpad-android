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
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.FloatMath;

// TODO: Copious documentation and cleanup.
public class TouchPanel extends Item implements LogTag {
	
	private static final long serialVersionUID = -8300732746587169324L;

	public static enum PanelType {
		X,
		Y,
		Both
	}
	
	static final int SLIDER_TOT = 16384;
	
	static final int SLIDER_GAP = 16;
	
	/**
	 * The maximum travelable distance for DP to consider
	 * this event as a click on the first button. (A tap.)
	 */
	static final int CLICK_THRESHOLD = 250;
	
	/**
	 * The maximum time for DP to consider this event as a
	 * click on the first button (short taps only).
	 */
	static final float CLICK_TIME = 100f / 1000f;
	
	public final PanelType type;
	
	/**
	 * Axis X direction thing
	 */
	public int ax;
	/**
	 * Axis Y direction thing
	 */
	public int ay;
	
	private float tmpAx, tmpAy;
	private float startX, startY;
	
	// Used to calculate total distance onscreen.
	private float startTmpAx, startTmpAy;
	
	private long startTime;

	public TouchPanel(int x, int y, int sx, int sy, PanelType type) {
		super(x, y, sx, sy);
		this.type = type;
	}

	@Override
	public void drawInArea(Canvas c, RectF area, PointF centre, boolean landscape) { }

	@Override
	public String getOutputString() {
		switch(type) {
		case X:
			return "{C" + ax + "}";
		case Y:
			return "{C" + ay + "}";
		case Both:
		default:
			return "{T" + ax + "," + ay + "}";
		}
	}
	
	private boolean newRun = true, tmpNewRun = true;

	@Override
	public void resetStickyLock() {
		tmpNewRun = true;
	}

	@Override
	public void finaliseState() {
		newRun = tmpNewRun;
		ax = (int) (tmpAx + startX);
		ay = (int) (tmpAy + startY);
	}

	@Override
	public void onMouseOn(ScreenInfo info, float x, float y) {
		PointF centre = pos.computeCentre(info);
		RectF area = pos.computeArea(info);
		final float tempXw = area.width() - (2 * SLIDER_GAP);
		final float tempYw = area.height() - (2 * SLIDER_GAP);
		
		if(type == PanelType.X || type == PanelType.Both) {
			tmpAx = ((float)(x - centre.x) / tempXw * 2f * SLIDER_TOT);
			if(tmpAx < -SLIDER_TOT)
				tmpAx = -SLIDER_TOT;
			else if(tmpAx > SLIDER_TOT)
				tmpAx = SLIDER_TOT;
		}
		if(type == PanelType.Y || type == PanelType.Both) {
			tmpAy = ((float)(y - centre.y) / tempYw * 2 * SLIDER_TOT);
			if(tmpAy < -SLIDER_TOT)
				tmpAy = -SLIDER_TOT;
			else if(tmpAy > SLIDER_TOT)
				tmpAy = SLIDER_TOT;
		}
		
		if(newRun) { // Reset, starting fresh if the start position wasn't anything
			newRun = false;
			startX = ax - tmpAx;
			startY = ay - tmpAy;
			startTmpAx = tmpAx;
			startTmpAy = tmpAy;
			startTime = System.nanoTime();
		}
		tmpNewRun = newRun;
	}

	@Override
	public void onMouseOff() {
		float dx = startTmpAx - tmpAx;
		float dy = startTmpAy - tmpAy;
		float dist = FloatMath.sqrt(dx * dx + dy * dy);
		float totalTime = (float)(System.nanoTime() - startTime) / 1000f / 1000f / 1000f;
		if(dist < CLICK_THRESHOLD && totalTime < CLICK_TIME) {
			if(callbacks != null)
				callbacks.tapDefaultButton();
		}
	}

	@Override
	int getFlags() {
		int extraFlags = 0;
		
		switch(type) {
		case X:
			extraFlags = FLAG_HAS_X_AXIS;
			break;
		case Y:
			extraFlags = FLAG_HAS_Y_AXIS;
			break;
		case Both:
			extraFlags = FLAG_HAS_X_AXIS | FLAG_HAS_Y_AXIS;
			break;
		}
		
		return FLAG_TRACKPAD | extraFlags;
	}

	@Override
	int getData1() {
		switch(type) {
		case X:
		case Both:
			return ax;
		default:
			return 0;
		}
	}

	@Override
	int getData2() {
		switch(type) {
		case Y:
		case Both:
			return ay;
		default:
			return 0;
		}
	}

	@Override
	int getData3() {
		return 0;
	}
}

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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;

public abstract class Item implements Serializable {
	private static final long serialVersionUID = -2217591684825043179L;
	
	public static final int FLAG_BUTTON			= 0x1;
	public static final int FLAG_TOGGLE_BUTTON	= 0x2 | FLAG_BUTTON;
	public static final int FLAG_SLIDER			= 0x4;
	public static final int FLAG_TRACKPAD		= 0x8;
	public static final int FLAG_HAS_X_AXIS		= 0x10;
	public static final int FLAG_HAS_Y_AXIS		= 0x20;
	public static final int FLAG_IS_RESET		= 0x40;
	
	protected static final int TEXT_SIZE = 14;
	public static final int BUTTON_GAP = 10;
	
	protected static final Paint bp = new Paint();
	protected static final Paint pText = new Paint();
    protected static final Paint bpS = new Paint();
	protected static final Paint pTextS = new Paint();
    protected static final Paint pThinBorder = new Paint();
    protected static final Paint pGrayBG = new Paint();
    
    private static final boolean ANTIALIAS = true;
	
    static {
		bp.setAntiAlias(ANTIALIAS);
        bp.setColor(0xffffffff);
        bp.setStrokeWidth(5);
        bp.setStyle(Style.STROKE);
        
        pText.setAntiAlias(ANTIALIAS);
        pText.setColor(0xffffffff);
        pText.setTextSize(TEXT_SIZE);
        pText.setTextAlign(Align.CENTER);
		
		bpS.setAntiAlias(ANTIALIAS);
        bpS.setColor(0xffffffff);
        bpS.setStrokeWidth(5);
        bpS.setStyle(Style.FILL_AND_STROKE);
        
        pTextS.setAntiAlias(ANTIALIAS);
        pTextS.setColor(0xff000000);
        pTextS.setTextSize(TEXT_SIZE);
        pTextS.setTextAlign(Align.CENTER);
        
        pThinBorder.setAntiAlias(ANTIALIAS);
        pThinBorder.setColor(0xffffffff);
        pThinBorder.setTextAlign(Align.CENTER);
        pThinBorder.setStrokeWidth(2);
        pThinBorder.setStyle(Style.STROKE);

        pGrayBG.setAntiAlias(ANTIALIAS);
        pGrayBG.setColor(0xff444444);
        pGrayBG.setStyle(Style.FILL);
        pGrayBG.setStrokeWidth(2);

    }
	
	public final int x, y;
	public final int sx, sy;
	
	private int lastWidth, lastHeight;
	
	protected boolean selected = false;
	
	public Item(int x, int y, int sx, int sy) {
		this.x = x;
		this.y = y;
		this.sx = sx < 1 ? 1 : sx;
		this.sy = sy < 1 ? 1 : sy;
	}
	
	public final void draw(Canvas c, int width, int height, boolean landscape) {
		RectF area = computeArea(width, height);
		Point centre = computeCentre(area);
		
		c.drawRoundRect(area, 10, 10, bp);
		
		drawInternal(c, area, centre, landscape);
		drawInArea(c, area, centre, landscape);
	}
	
	protected abstract void drawInArea(Canvas c, RectF area, Point centre, boolean landscape);
	
	private void drawInternal(Canvas c, RectF area, Point centre, boolean landscape) {
		c.drawRoundRect(area, 10, 10, isSelected() ? bpS : bp);
	}
	
	protected final RectF computeArea(int width, int height) {
		lastWidth = width;
		lastHeight = height;
		return new RectF(
				x * width + BUTTON_GAP,
				y * height + BUTTON_GAP,
				(x + sx) * width - BUTTON_GAP,
				(y + sy) * height - BUTTON_GAP);
	}
	protected final RectF computeArea() {
		return computeArea(lastWidth, lastHeight);
	}
	
	protected final Point computeCentre(RectF area) {
		return new Point((int)area.centerX(), (int)area.centerY());
	}
	
	protected final Point computeCentre() {
		return computeCentre(computeArea());
	}

	public boolean isSelected() {
		return selected;
	}
	
	public abstract String getOutputString();
	
	/**
	 * See binary spec for info on the layout of this
	 * @param os
	 * @throws IOException
	 */
	public void writeBinary(DataOutputStream os) throws IOException {
		os.writeInt(getFlags());
		os.writeInt(0); // Reserved
		os.writeInt(getData1());
		os.writeInt(getData2());
		os.writeInt(getData3());
	}
	
	/**
	 * Used for binary serialisation
	 * @return
	 */
	abstract int getFlags();
	abstract int getData1();
	abstract int getData2();
	abstract int getData3();
	
	public boolean pointIsInArea(float x2, float y2) {
		return computeArea().contains(x2, y2);
	}
	
	/**
	 * Resets the sticky thing that shows that the button is pressed with at least one finger
	 */
	public abstract void resetStickyLock();
	
	/**
	 * Copies the computed state to the actual state - avoids possible epifail later on...
	 */
	public abstract void finaliseState();
	
	public abstract void onMouseOn(float x, float y);
	public abstract void onMouseOff();
}

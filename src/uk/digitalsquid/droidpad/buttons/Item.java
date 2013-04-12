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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PointF;
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
	
	public final Position pos;
	
	protected boolean selected = false;
	
	protected ButtonPresses callbacks;
	
	/**
	 * Grid constructor - use to create a grid item
	 * @param x
	 * @param y
	 * @param sx
	 * @param sy
	 */
	public Item(int x, int y, int sx, int sy) {
		pos = new GridPosition(x, y, sx < 1 ? 1 : sx, sy < 1 ? 1 : sy);
	}
	
	/**
	 * General constructor
	 * @param x
	 * @param y
	 * @param sx
	 * @param sy
	 * @param free <code>true</code>, this will be a free (floating) item
	 */
	public Item(float x, float y, float sx, float sy, boolean free) {
		if(free)
			pos = new FreePosition(x, y, sx, sy);
		else
			pos = new GridPosition((int)x, (int)y, (int)sx < 1 ? 1 : (int)sx, (int)sy < 1 ? 1 : (int)sy);
	}
	
	public final void draw(Canvas c, ScreenInfo info) {
		RectF area = pos.computeArea(info);
		PointF centre = pos.computeCentre(info);
		
		c.drawRoundRect(area, 10, 10, bp);
		
		drawInternal(c, area, centre, info.landscape);
		drawInArea(c, area, centre, info.landscape);
	}
	
	protected abstract void drawInArea(Canvas c, RectF area, PointF centre, boolean landscape);
	
	private void drawInternal(Canvas c, RectF area, PointF centre, boolean landscape) {
		c.drawRoundRect(area, 10, 10, isSelected() ? bpS : bp);
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
	public final void writeBinary(DataOutputStream os) throws IOException {
		os.writeInt(getFlags());
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
	
	public boolean pointIsInArea(ScreenInfo info, float x2, float y2) {
		return pos.computeArea(info).contains(x2, y2);
	}
	
	/**
	 * Resets the sticky thing that shows that the button is pressed with at least one finger
	 */
	public abstract void resetStickyLock();
	
	/**
	 * Copies the computed state to the actual state - avoids possible epifail later on...
	 */
	public abstract void finaliseState();
	
	public abstract void onMouseOn(ScreenInfo info, float x, float y);
	public abstract void onMouseOff();

	protected void setCallbacks(ButtonPresses callbacks) {
		this.callbacks = callbacks;
	}
	
	public static class ScreenInfo {
		/**
		 * The width and height of the entire screen
		 */
		public float width, height;
		/**
		 * The width and height of one grid item
		 */
		public float gridWidth, gridHeight;
		
		public boolean landscape;
		
		public ScreenInfo() {
			width = 1000;
			height = 1000;
			gridWidth = 100;
			gridHeight = 100;
		}
		public ScreenInfo(float w, float h, float gw, float gh, boolean landscape) {
			width = w;
			height = h;
			gridWidth = gw;
			gridHeight = gh;
			this.landscape = landscape;
		}
		public void set(float w, float h, float gw, float gh, boolean landscape) {
			width = w;
			height = h;
			gridWidth = gw;
			gridHeight = gh;
			this.landscape = landscape;
		}
	}
	
	/**
	 * Describes the position of an onscreen button.
	 * Is either grid-based, or positioned absolutely.
	 */
	public static abstract class Position implements Serializable {
		private static final long serialVersionUID = -7465262293417889805L;
		private Position() { }
		
		public abstract RectF computeArea(ScreenInfo info);
		public abstract PointF computeCentre(ScreenInfo info);
	}
	
	/**
	 * A position which is locked to the grid
	 * @author william
	 *
	 */
	public static class GridPosition extends Position {
		private static final long serialVersionUID = 9207230734790003508L;
		
		/**
		 * Padding on buttons for buttons on a grid
		 */
		public static final int BUTTON_GAP = 10;
		/**
		 * The x-coordinate, grid based
		 */
		int x;
		/**
		 * The y-coordinate, grid-based
		 */
		int y;
		
		int sx, sy;
		
		public GridPosition(int x, int y, int sx, int sy) {
			this.x = x;
			this.y = y;
			this.sx = sx;
			this.sy = sy;
		}
		
		@Override
		public final RectF computeArea(ScreenInfo info) {
			return new RectF(
					x * info.gridWidth + BUTTON_GAP,
					y * info.gridHeight + BUTTON_GAP,
					(x + sx) * info.gridWidth - BUTTON_GAP,
					(y + sy) * info.gridHeight - BUTTON_GAP);
		}
		
		@Override
		public final PointF computeCentre(ScreenInfo info) {
			return new PointF(((float)x + (float)sx / 2) * info.gridWidth,
					((float)y + (float)sy / 2) * info.gridHeight);
		}
	}
	
	/**
	 * A completely free position on the screen.
	 * @author william
	 *
	 */
	public static class FreePosition extends Position {
		private static final long serialVersionUID = -7306582893921329366L;
		
		/**
		 * Padding for freely positioned buttons
		 */
		public static final int BUTTON_GAP = 0;
		
		/**
		 * The x-coordinate, between 0 and 1
		 */
		float x;
		/**
		 * The y-coordinate, between 0 and 1
		 */
		float y;
		
		float sx, sy;
		
		public FreePosition(float x, float y, float sx, float sy) {
			this.x = x;
			this.y = y;
			this.sx = sx;
			this.sy = sy;
		}
		
		@Override
		public final RectF computeArea(ScreenInfo info) {
			return new RectF(
					x * info.width + BUTTON_GAP,
					y * info.height + BUTTON_GAP,
					(x + sx) * info.width - BUTTON_GAP,
					(y + sy) * info.height - BUTTON_GAP);
		}
		
		@Override
		public final PointF computeCentre(ScreenInfo info) {
			return new PointF((x + sx / 2) * info.width,
					(y + sy / 2) * info.height);
		}
	}
}

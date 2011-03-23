package uk.digitalsquid.droidpad.buttons;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

public class ToggleButton extends Button {

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
		Log.v("DroidPad", "Released");
	}
	
	@Override
	public void resetStickyLock() {
		
	}
}

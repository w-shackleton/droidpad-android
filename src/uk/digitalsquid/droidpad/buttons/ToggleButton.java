package uk.digitalsquid.droidpad.buttons;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;

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
}

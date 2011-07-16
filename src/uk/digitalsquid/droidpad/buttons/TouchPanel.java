package uk.digitalsquid.droidpad.buttons;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;

public class TouchPanel extends Item {
	
	public static enum PanelType {
		X,
		Y,
		Both
	}
	
	public static final int SLIDER_TOT = 16384;
	
	public static final int SLIDER_GAP = 16;
	public static final int SLIDER_SIZE = 10;
	
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
	private float accumulatedAx, accumulatedAy;

	public TouchPanel(int x, int y, int sx, int sy, PanelType type) {
		super(x, y, sx, sy);
		this.type = type;
	}

	@Override
	public void drawInArea(Canvas c, RectF area, Point centre, boolean landscape) {
		// No drawing to do...
	}

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
		ax = (int) (tmpAx + startX + accumulatedAx);
		ay = (int) (tmpAy + startY + accumulatedAy);
	}

	@Override
	public void onMouseOn(float x, float y) {
		Point centre = computeCentre();
		RectF area = computeArea();
		float tempXw = area.width() - (2 * SLIDER_GAP);
		float tempYw = area.height() - (2 * SLIDER_GAP);
		
		if(type == PanelType.X || type == PanelType.Both)
		{
			tmpAx = ((float)(x - centre.x) / tempXw * 2f * SLIDER_TOT);
			if(tmpAx < -SLIDER_TOT)
				tmpAx = -SLIDER_TOT;
			else if(tmpAx > SLIDER_TOT)
				tmpAx = SLIDER_TOT;
		}
		if(type == PanelType.Y || type == PanelType.Both)
		{
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
		}
		tmpNewRun = newRun;
	}

	@Override
	public void onMouseOff() {
	}
}

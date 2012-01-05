package uk.digitalsquid.droidpad.buttons;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;

public class Slider extends Item {
	
	public static enum SliderType {
		X,
		Y,
		Both
	}
	
	public static final int SLIDER_TOT = 16384;
	public static final int SLIDER_CUTOFF = 700;
	
	public static final int SLIDER_GAP = 16;
	public static final int SLIDER_SIZE = 10;
	
	public final SliderType type;
	
	/**
	 * Axis X direction thing
	 */
	public int ax;
	/**
	 * Axis Y direction thing
	 */
	public int ay;
	
	private float tmpAx, tmpAy;

	public Slider(int x, int y, int sx, int sy, SliderType type) {
		super(x, y, sx, sy);
		this.type = type;
	}

	@Override
	public void drawInArea(Canvas c, RectF area, Point centre, boolean landscape) {
		float tempXw = area.width() - (2 * SLIDER_GAP);
		float tempYw = area.height() - (2 * SLIDER_GAP);

		float posOnScreenX = ((float)ax / (float)SLIDER_TOT * tempXw / 2f) + centre.x;
		float posOnScreenY = ((float)ay / (float)SLIDER_TOT * tempYw / 2) + centre.y;

		if(type == SliderType.X || type == SliderType.Both)
			c.drawLine(posOnScreenX, area.top, posOnScreenX, area.bottom, pGrayBG);
		
		if(type == SliderType.Y || type == SliderType.Both)
			c.drawLine(area.left, posOnScreenY, area.right, posOnScreenY, pGrayBG);
		
		c.drawCircle(posOnScreenX, posOnScreenY, SLIDER_SIZE, pText);
	}

	@Override
	public String getOutputString() {
		switch(type) {
		case X:
			return "{S" + ax + "}";
		case Y:
			return "{S" + ay + "}";
		case Both:
		default:
			return "{A" + ax + "," + ay + "}";
		}
	}
	
	private boolean axesFloat = false;
	
	/**
	 * When axes are floating, they don't reset when let go of
	 * @param axesFloat If true, axes will float.
	 */
	public void setAxesFloat(boolean axesFloat) {
		this.axesFloat = axesFloat;
	}

	@Override
	public void resetStickyLock() {
		if(!axesFloat) {
			tmpAx = 0;
			tmpAy = 0;
		}
	}

	@Override
	public void finaliseState() {
		ax = (int) tmpAx;
		ay = (int) tmpAy;
	}

	@Override
	public void onMouseOn(float x, float y) {
		Point centre = computeCentre();
		RectF area = computeArea();
		float tempXw = area.width() - (2 * SLIDER_GAP);
		float tempYw = area.height() - (2 * SLIDER_GAP);
		
		if(type == SliderType.X || type == SliderType.Both)
		{
			tmpAx = ((float)(x - centre.x) / tempXw * 2f * SLIDER_TOT);
			if(tmpAx < -SLIDER_TOT)
				tmpAx = -SLIDER_TOT;
			else if(tmpAx > SLIDER_TOT)
				tmpAx = SLIDER_TOT;
			ax = (int) tmpAx;
		}
		if(type == SliderType.Y || type == SliderType.Both)
		{
			tmpAy = ((y - centre.y) / tempYw * 2 * SLIDER_TOT);
			if(tmpAy < -SLIDER_TOT)
				tmpAy = -SLIDER_TOT;
			else if(tmpAy > SLIDER_TOT)
				tmpAy = SLIDER_TOT;
			ay = (int) tmpAy;
		}
	}

	@Override
	public void onMouseOff() {
	}
}

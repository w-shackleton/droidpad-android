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

package uk.digitalsquid.droidpad;

import uk.digitalsquid.droidpad.buttons.Item;
import uk.digitalsquid.droidpad.buttons.Layout;
import uk.digitalsquid.droidpad.buttons.LayoutManager;
import uk.digitalsquid.droidpad.buttons.Slider;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class ButtonView extends View
{
	private boolean landscape;
	
	public static final int BUTTONS_X = 4;
	public static final int BUTTONS_Y = 5;
	
	private final Layout currLayout;
	
	public ButtonView(DroidPadButtons parent, String type)
	{
		super(parent);
		
		Log.v("DroidPad", "Type: \"" + type + "\"");
		
        landscape = PreferenceManager.getDefaultSharedPreferences(parent).getBoolean("orientation", false);
        
        boolean floatingAxes = !PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("axesfloat", false);
        
        //type="2";
        currLayout = LayoutManager.getLayoutByName(type);
        for(Item item : currLayout) {
        	if(item instanceof Slider) {
        		((Slider)item).setAxesFloat(floatingAxes);
        	}
        }
		
		parent.sendEvent(currLayout);
	}

	private int widthIter;
	private int heightIter;
	
	private int width, height;
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		width = canvas.getWidth();
		height = canvas.getHeight();
		
		canvas.drawRect(0, 0, width, height, new Paint(0xFF000000));
		widthIter = width / BUTTONS_X;
		heightIter = height / BUTTONS_Y;
		
		for(Item item : currLayout) {
			item.draw(canvas, widthIter, heightIter, landscape);
		}
	}

	private void processPoint(float x, float y) {
		processPoint(x, y, false);
	}
	private void processPoint(float x, float y, boolean up) {
		for(Item item : currLayout) {
			if(item.pointIsInArea(x, y)) {
				if(!up)
					item.onMouseOn(x, y);
				else {
					item.onMouseOff();
					item.resetStickyLock();
				}
			}
		}
	}
	
	/**
	 * Removes the sticky lock from each button indicating that at least 1 thing is pressing it.
	 */
	private void resetItemSticky() {
		for(Item item : currLayout) {
			item.resetStickyLock();
		}
	}
	
	/**
	 * Sets the sticky lock from each button indicating that at least 1 thing is pressing it.
	 */
	private void finaliseItemState() {
		for(Item item : currLayout) {
			item.finaliseState();
		}
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);
		
		int actionCode = event.getAction() & MotionEvent.ACTION_MASK;
		if(actionCode == MotionEvent.ACTION_DOWN || actionCode == MotionEvent.ACTION_POINTER_DOWN) {
			// Something was pressed
			float x, y;
			if(actionCode == MotionEvent.ACTION_DOWN) {
				x = event.getX();
				y = event.getY();
			}
			else { // Other pointer
				int pid = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				x = event.getX(pid);
				y = event.getY(pid);
			}
			processPoint(x, y);
		}
		
		if(actionCode == MotionEvent.ACTION_UP || actionCode == MotionEvent.ACTION_POINTER_UP) {
			// Something was released
		
			float x, y;
			if(actionCode == MotionEvent.ACTION_UP) {
				x = event.getX();
				y = event.getY();
			}
			else { // Other pointer
				int pid = event.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				x = event.getX(pid);
				y = event.getY(pid);
			}
			processPoint(x, y, true);
		}
		
		if(actionCode == MotionEvent.ACTION_MOVE) {
			resetItemSticky();
			for(int i = 0; i < event.getPointerCount(); i++) {
				processPoint(event.getX(i), event.getY(i));
			}
		}
		finaliseItemState();
		
		invalidate();

		return true;
	}
}

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
import uk.digitalsquid.droidpad.buttons.ModeSpec;
import uk.digitalsquid.droidpad.buttons.Slider;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Shows the buttons onscreen.
 * @author william
 *
 */
public class ButtonView extends View implements LogTag
{
	private boolean landscape;
	
	private Layout layout;
	
	Buttons parent;
	
	public ButtonView(Context context, AttributeSet attrib) {
		super(context, attrib);
		if(isInEditMode()) return;
        landscape = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("orientation", false);
	}
	
	public ButtonView(Context context) {
		super(context);
		if(isInEditMode()) return;
        landscape = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("orientation", false);
	}
	
	/**
	 * Sets the current mode spec. Should only be called once, when the view is created.
	 * This is delayed as the class is instantiated through XML
	 * @param mode
	 */
	public void setModeSpec(Buttons parent, ModeSpec mode) {
        boolean floatingAxes = !PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("axesfloat", false);
		
		this.parent = parent;
		
		layout = mode.getLayout(); // No need to keep type?
        
        for(Item item : layout) {
        	if(item instanceof Slider) {
        		((Slider)item).setAxesFloat(floatingAxes);
        	}
        }
		
		parent.sendEvent(layout);
	}

	private int widthIter;
	private int heightIter;
	
	private int width, height;
	
	private static final Paint P_BLACK = new Paint(0xFF000000);
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		width = canvas.getWidth();
		height = canvas.getHeight();
		
		canvas.drawRect(0, 0, width, height, P_BLACK);
		if(isInEditMode()) return;
		widthIter = width / layout.width;
		heightIter = height / layout.height;
		
		for(Item item : layout) {
			item.draw(canvas, widthIter, heightIter, landscape);
		}
	}

	private void processPoint(float x, float y) {
		processPoint(x, y, false);
	}
	private void processPoint(float x, float y, boolean up) {
		for(Item item : layout) {
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
		for(Item item : layout) {
			item.resetStickyLock();
		}
	}
	
	/**
	 * Sets the sticky lock from each button indicating that at least 1 thing is pressing it.
	 */
	private void finaliseItemState() {
		for(Item item : layout) {
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
		
		parent.sendEvent(layout); // Make sure always latest - eg service restart
		
		finaliseItemState();
		
		invalidate();

		return true;
	}
}

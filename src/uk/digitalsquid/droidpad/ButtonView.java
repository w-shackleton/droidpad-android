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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class ButtonView extends View
{
	private DroidPadButtons parent;
	private boolean landscape;
	
	public static final int BUTTONS_X = 4;
	public static final int BUTTONS_Y = 5;
	
	private final Layout currLayout;
	
	public ButtonView(DroidPadButtons parent, String type)
	{
		super(parent);
		this.parent = parent;
		
		Log.v("DroidPad", "Type: \"" + type + "\"");
		
        landscape = PreferenceManager.getDefaultSharedPreferences(parent).getBoolean("orientation", false);
        
        //type="2";
        currLayout = LayoutManager.getLayoutByName(type);
		
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

	private int prevButton = -1;
	private int currButton = -1;
	private int origButton = -1;
	private boolean origWasAxis = false;
	
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
		dumpEvent(event);
		
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

		/* currButton = -1;
		if(event.getAction() != MotionEvent.ACTION_UP && !origWasAxis)
		{
			for(int i = 0; i < currLayout.items.length; i++)
			{
				posX1 =  currLayout.items[i].XPos							   * widthIter  + BUTTON_GAP;
				posY1 =  currLayout.items[i].YPos							   * heightIter + BUTTON_GAP;
				posX2 = (currLayout.items[i].XPos + currLayout.items[i].XSize) * widthIter  - BUTTON_GAP;
				posY2 = (currLayout.items[i].YPos + currLayout.items[i].YSize) * heightIter - BUTTON_GAP;
				
				if(
						event.getX() > posX1 &&
						event.getX() < posX2 &&
						event.getY() > posY1 &&
						event.getY() < posY2
						)
				{
					// Work out pressed button
					currButton = i;
				}
			}
		}
		if(origWasAxis)
		{
			posX1 =  currLayout.items[origButton].XPos							   * widthIter  + BUTTON_GAP;
			posY1 =  currLayout.items[origButton].YPos							   * heightIter + BUTTON_GAP;
			posX2 = (currLayout.items[origButton].XPos + currLayout.items[origButton].XSize) * widthIter  - BUTTON_GAP;
			posY2 = (currLayout.items[origButton].YPos + currLayout.items[origButton].YSize) * heightIter - BUTTON_GAP;
			
			posXm = (posX1 + posX2) / 2;
			posYm = (posY1 + posY2) / 2;
			
			tempXw = posX2 - posX1 - (2 * SLIDER_GAP);
			tempYw = posY2 - posY1 - (2 * SLIDER_GAP);
			
			if(
					currLayout.items[origButton].item == itemType.sliderX ||
					currLayout.items[origButton].item == itemType.axis
					)
			{
				tempPosX = (event.getX() - posXm) / tempXw * 2 * SLIDER_TOT;
				if(tempPosX < -SLIDER_TOT)
					currLayout.items[origButton].axisX = -SLIDER_TOT;
				else if(tempPosX > SLIDER_TOT)
					currLayout.items[origButton].axisX = SLIDER_TOT;
				else
					currLayout.items[origButton].axisX = tempPosX;
			}
			if(
					currLayout.items[origButton].item == itemType.sliderY ||
					currLayout.items[origButton].item == itemType.axis
					)
			{
				tempPosY = (event.getY() - posYm) / tempYw * 2 * SLIDER_TOT;
				if(tempPosY < -SLIDER_TOT)
					currLayout.items[origButton].axisY = -SLIDER_TOT;
				else if(tempPosY > SLIDER_TOT)
					currLayout.items[origButton].axisY = SLIDER_TOT;
				else
					currLayout.items[origButton].axisY = tempPosY;
			}
			parent.sendEvent(currLayout);
			invalidate();
		}
		
		if(currButton != prevButton)
		{
			// If button was actually let go, or just slid off.
			if((event.getAction() == MotionEvent.ACTION_UP) && (prevButton != -1))
			{
				if(currLayout.items[prevButton].item == itemType.toggle)
				{
					// Switch toggle button
					if(currLayout.items[prevButton].pressed)
						currLayout.items[prevButton].pressed = false;
					else if(!currLayout.items[prevButton].pressed)
						currLayout.items[prevButton].pressed = true;
				}
			}
			if(currButton != -1)
			{
				if(currLayout.items[currButton].item == itemType.button)
				{
					currLayout.items[currButton].pressed = true;
				}
			}
			else
			{
				if(currLayout.items[prevButton].item == itemType.button)
				{
					currLayout.items[prevButton].pressed = false;
				}
			}
			parent.sendEvent(currLayout);
			invalidate();
		}
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			origButton = currButton;
			if(origButton != -1)
				origWasAxis = (
						currLayout.items[origButton].item == itemType.sliderX ||
						currLayout.items[origButton].item == itemType.sliderY ||
						currLayout.items[origButton].item == itemType.axis);
		}
		else if(event.getAction() == MotionEvent.ACTION_UP)
		{
			origButton = -1;
			origWasAxis = false;
		}
		prevButton = currButton; */
		// prevEvent = event;
		return true;
	}
	
	/** Show an event in the LogCat view, for debugging */
	private void dumpEvent(MotionEvent event) {
	   String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
	      "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
	   StringBuilder sb = new StringBuilder();
	   int action = event.getAction();
	   int actionCode = action & MotionEvent.ACTION_MASK;
	   sb.append("event ACTION_" ).append(names[actionCode]);
	   if (actionCode == MotionEvent.ACTION_POINTER_DOWN
	         || actionCode == MotionEvent.ACTION_POINTER_UP) {
	      sb.append("(pid " ).append(
	      action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
	      sb.append(")" );
	   }
	   sb.append("[" );
	   for (int i = 0; i < event.getPointerCount(); i++) {
	      sb.append("#" ).append(i);
	      sb.append("(pid " ).append(event.getPointerId(i));
	      sb.append(")=" ).append((int) event.getX(i));
	      sb.append("," ).append((int) event.getY(i));
	      if (i + 1 < event.getPointerCount())
	         sb.append(";" );
	   }
	   sb.append("]" );
	   Log.d("DroidPad", sb.toString());
	}
}

package uk.digitalsquid.droidpad;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
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
	private static final int TEXT_SIZE = 14;
	private static final int BUTTON_GAP = 10;
	private static final int SLIDER_GAP = 16;
	private static final int SLIDER_SIZE = 10;
	public static final int SLIDER_TOT = 16384;
	public static final int SLIDER_CUTOFF = 700;
	
	public static enum itemType
	{
		button,
		toggle,
		sliderX,
		sliderY,
		axis,
	}
	
	public static final class layoutItem
	{
		int XPos;
		int YPos;
		int XSize, YSize;
		itemType item;
		String name;
		int Textsize;
		boolean pressed = false;
		float axisX = 0, axisY = 0;
		
		layoutItem(int XPos, int YPos, int XSize, int YSize, String name, itemType item, int Textsize)
		{
			this.XPos = XPos;
			this.YPos = YPos;
			this.XSize = XSize;
			this.YSize = YSize;
			this.name = name;
			this.item = item;
			if(Textsize != 0)
				this.Textsize = Textsize;
			else
				this.Textsize = TEXT_SIZE;
		}
	}
	
	public static final class layout
	{
		String name;
		layoutItem[] items;
		layout(String name, layoutItem[] items)
		{
			this.name = name;
			this.items = items;
		}
	}
	
	private static final layout[] layouts = {
		new layout("mouse", new layoutItem[] {
				new layoutItem(0, 0, 2, 5, "Left-Click", itemType.button, 0),
				new layoutItem(2, 0, 1, 5, "Middle", itemType.button, 0),
				new layoutItem(3, 0, 1, 5, "Right", itemType.button, 0),
		}),
		new layout("slide", new layoutItem[] {
				new layoutItem(0, 3, 4, 2, "Next slide", itemType.button, 30),
				new layoutItem(1, 2, 2, 1, "Prev slide", itemType.button, 16),
				new layoutItem(0, 2, 1, 1, "Start", itemType.button, 0),
				new layoutItem(3, 2, 1, 1, "End", itemType.button, 0),
				new layoutItem(3, 0, 1, 2, "White", itemType.toggle, 0),
				new layoutItem(2, 0, 1, 2, "Black", itemType.toggle, 0),
				new layoutItem(0, 0, 2, 1, "Beginning", itemType.button, 0),
				new layoutItem(0, 1, 2, 1, "End", itemType.button, 0),
		}),
		new layout("1", new layoutItem[] {
				new layoutItem(1, 0, 2, 1, "1", itemType.button, 30),
				new layoutItem(1, 1, 2, 2, "", itemType.axis, 0),
				new layoutItem(0, 1, 1, 2, "", itemType.sliderY, 0),
				new layoutItem(3, 0, 1, 1, "2", itemType.button, 30),
				new layoutItem(0, 0, 1, 1, "3", itemType.button, 30),
				new layoutItem(3, 1, 1, 2, "4", itemType.button, 30),
				new layoutItem(0, 3, 1, 1, "5", itemType.button, 30),
				new layoutItem(1, 3, 1, 1, "6", itemType.toggle, 30),
				new layoutItem(2, 3, 1, 1, "7", itemType.toggle, 30),
				new layoutItem(3, 3, 1, 1, "8", itemType.button, 30),
				
				new layoutItem(0, 4, 1, 1, "9", itemType.toggle, 30),
				new layoutItem(1, 4, 2, 1, "10", itemType.button, 30),
				new layoutItem(3, 4, 1, 1, "11", itemType.button, 30),
		}),
		new layout("2", new layoutItem[] {
				new layoutItem(0, 1, 4, 3, "", itemType.axis, 0),
				
				new layoutItem(0, 4, 1, 1, "1", itemType.button, 30),
				new layoutItem(1, 4, 2, 1, "2", itemType.button, 30),
				new layoutItem(3, 4, 1, 1, "3", itemType.button, 30),

				new layoutItem(0, 0, 1, 1, "4", itemType.button, 30),
				new layoutItem(1, 0, 1, 1, "5", itemType.button, 30),
				new layoutItem(2, 0, 1, 1, "6", itemType.button, 30),
				new layoutItem(3, 0, 1, 1, "7", itemType.button, 30),
		}),
		new layout("2b", new layoutItem[] {
				new layoutItem(0, 1, 4, 3, "", itemType.axis, 0),
				
				new layoutItem(0, 4, 1, 1, "1", itemType.button, 30),
				new layoutItem(1, 4, 2, 1, "2", itemType.button, 30),
				new layoutItem(3, 4, 1, 1, "3", itemType.toggle, 30),

				new layoutItem(0, 0, 1, 1, "4", itemType.toggle, 30),
				new layoutItem(1, 0, 1, 1, "5", itemType.button, 30),
				new layoutItem(2, 0, 1, 1, "6", itemType.button, 30),
				new layoutItem(3, 0, 1, 1, "7", itemType.toggle, 30),
		}),
		new layout("3", new layoutItem[] {
				new layoutItem(1, 1, 2, 2, "", itemType.axis, 0),
				
				new layoutItem(1, 0, 2, 1, "", itemType.sliderX, 30),
				new layoutItem(0, 1, 1, 2, "", itemType.sliderY, 30),
				new layoutItem(1, 3, 2, 1, "", itemType.sliderX, 30),
				new layoutItem(3, 1, 1, 2, "", itemType.sliderY, 30),
				
				new layoutItem(0, 3, 1, 1, "1", itemType.button, 30),
				new layoutItem(3, 3, 1, 1, "2", itemType.button, 30),

				new layoutItem(0, 4, 1, 1, "3", itemType.button, 30),
				new layoutItem(1, 4, 1, 1, "4", itemType.button, 30),
				new layoutItem(2, 4, 1, 1, "5", itemType.button, 30),
				new layoutItem(3, 4, 1, 1, "6", itemType.button, 30),
				
				new layoutItem(0, 0, 1, 1, "7", itemType.toggle, 30),
				new layoutItem(3, 0, 1, 1, "8", itemType.toggle, 30),
		}),
	};
	private layout currLayout;
	
	public ButtonView(DroidPadButtons parent, String type)
	{
		super(parent);
		this.parent = parent;
		
		bp.setAntiAlias(true);
        bp.setColor(0xffffffff);
        bp.setStrokeWidth(5);
        bp.setStyle(Style.STROKE);
        
        pText.setAntiAlias(true);
        pText.setColor(0xffffffff);
        pText.setTextSize(TEXT_SIZE);
        pText.setTextAlign(Align.CENTER);
		
		bpS.setAntiAlias(true);
        bpS.setColor(0xffffffff);
        bpS.setStrokeWidth(5);
        bpS.setStyle(Style.FILL_AND_STROKE);
        
        pTextS.setAntiAlias(true);
        pTextS.setColor(0xff000000);
        pTextS.setTextSize(TEXT_SIZE);
        pTextS.setTextAlign(Align.CENTER);
        
        pThinBorder.setAntiAlias(true);
        pThinBorder.setColor(0xffffffff);
        pThinBorder.setTextAlign(Align.CENTER);
        pThinBorder.setStrokeWidth(2);
        pThinBorder.setStyle(Style.STROKE);

        pGrayBG.setAntiAlias(true);
        pGrayBG.setColor(0xff444444);
        pGrayBG.setStyle(Style.FILL);
        pGrayBG.setStrokeWidth(2);

		Log.v("DroidPad", "Type: \"" + type + "\"");
		
        landscape = PreferenceManager.getDefaultSharedPreferences(parent).getBoolean("orientation", false);
        
        //type="2";
        
		for(int i = 0; i < layouts.length; i++)
		{
			//Log.v("DroidPad", "Type loop: \"" + layouts[i].name + "\"");
			if(layouts[i].name.equalsIgnoreCase(type))
			{
				//Log.v("DroidPad", "Type: " + type + ", " + i);
				currLayout = layouts[i];
				break;
			}
		}
		
		parent.sendEvent(currLayout);
	}

	private Paint bp = new Paint();
	private Paint pText = new Paint();
    private Paint bpS = new Paint();
	private Paint pTextS = new Paint();
    private Paint pThinBorder = new Paint();
    private Paint pGrayBG = new Paint();
    private Paint cp = null;
	private Paint cpText = null;
	
	private int widthIter;
	private int heightIter;
	
	private int width, height;
	
	private int posX1, posY1, posX2, posY2;//, temp;
	private float tempXw, tempYw, tempPosX, tempPosY, posXm, posYm;
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		width = canvas.getWidth();
		height = canvas.getHeight();
		
		canvas.drawRect(0, 0, width, height, new Paint(0xFF000000));
		widthIter = width / BUTTONS_X;
		heightIter = height / BUTTONS_Y;
		
		for(int i = 0; i < currLayout.items.length; i++)
		{
			cp = bp;
			cpText = pText;
			
			cpText.setTextSize(currLayout.items[i].Textsize);
			pTextS.setTextSize(currLayout.items[i].Textsize);
			
			posX1 =  currLayout.items[i].XPos							   * widthIter  + BUTTON_GAP;
			posY1 =  currLayout.items[i].YPos							   * heightIter + BUTTON_GAP;
			posX2 = (currLayout.items[i].XPos + currLayout.items[i].XSize) * widthIter  - BUTTON_GAP;
			posY2 = (currLayout.items[i].YPos + currLayout.items[i].YSize) * heightIter - BUTTON_GAP;
			
			posXm = (posX1 + posX2) / 2;
			posYm = (posY1 + posY2) / 2;
			if(
					currLayout.items[i].item == itemType.sliderX ||
					currLayout.items[i].item == itemType.sliderY ||
					currLayout.items[i].item == itemType.axis)
			{
				canvas.drawRoundRect(new RectF(posX1, posY1, posX2, posY2), 10, 10, cp);

				if(landscape)
				{
					canvas.rotate(90, posXm, posYm);
					canvas.drawText(currLayout.items[i].name, posXm, posYm + (TEXT_SIZE / 2), cpText);
					canvas.rotate(-90, posXm, posYm);
				}
				else
					canvas.drawText(currLayout.items[i].name, posXm, posYm + (TEXT_SIZE / 2), cpText);
				
				/* if(landscape)
				{
					temp = posY1;
					posY1 = posY2;
					posY2 = temp;
				} */

				tempXw = posX2 - posX1 - (2 * SLIDER_GAP);
				tempYw = posY2 - posY1 - (2 * SLIDER_GAP);

				tempPosX = (currLayout.items[i].axisX / SLIDER_TOT * tempXw / 2) + posXm;
				tempPosY = (currLayout.items[i].axisY / SLIDER_TOT * tempYw / 2) + posYm;

				if(
						currLayout.items[i].item == itemType.sliderX ||
						currLayout.items[i].item == itemType.axis)
					canvas.drawLine(tempPosX, posY1, tempPosX, posY2, pGrayBG);
				if(
						currLayout.items[i].item == itemType.sliderY ||
						currLayout.items[i].item == itemType.axis)
					canvas.drawLine(posX1, tempPosY, posX2, tempPosY, pGrayBG);
				
				canvas.drawCircle(tempPosX, tempPosY, SLIDER_SIZE, pText);
			}
			else
			{
				if(currButton == i)
				{
					canvas.drawRoundRect(new RectF(posX1, posY1, posX2, posY2), 10, 10, bpS);
					if(landscape)
					{
						canvas.rotate(90, posXm, posYm);
						canvas.drawText(currLayout.items[i].name, posXm, posYm + (TEXT_SIZE / 2), pTextS);
						canvas.rotate(-90, posXm, posYm);
					}
					else
						canvas.drawText(currLayout.items[i].name, posXm, posYm + (TEXT_SIZE / 2), pTextS);
					
					if(currLayout.items[i].item == itemType.toggle)
					{
						canvas.drawCircle(posX2 - 10, posY2 - 10, 5, pTextS);
					}
				}
				else
				{
					if(currLayout.items[i].item == itemType.toggle && currLayout.items[i].pressed)
						canvas.drawRoundRect(new RectF(posX1, posY1, posX2, posY2), 10, 10, pGrayBG);
					
					canvas.drawRoundRect(new RectF(posX1, posY1, posX2, posY2), 10, 10, cp);
					if(landscape)
					{
						canvas.rotate(90, posXm, posYm);
						canvas.drawText(currLayout.items[i].name, posXm, posYm + (TEXT_SIZE / 2), cpText);
						canvas.rotate(-90, posXm, posYm);
					}
					else
						canvas.drawText(currLayout.items[i].name, posXm, posYm + (TEXT_SIZE / 2), cpText);
					
					if(currLayout.items[i].item == itemType.toggle)
					{
						if(currLayout.items[i].pressed)
						{
							canvas.drawCircle(posX2 - 10, posY2 - 10, 5, bpS);
						}
						else
						{
							canvas.drawCircle(posX2 - 10, posY2 - 10, 5, pThinBorder);
						}
					}
				}
			}
		}
	}

	private int prevButton = -1;
	private int currButton = -1;
	private int origButton = -1;
	private boolean origWasAxis = false;
	
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);

		currButton = -1;
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
		
		prevButton = currButton;
		return true;
	}
}

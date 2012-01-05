package uk.digitalsquid.droidpad.buttons;

import java.util.LinkedList;

public class Layout extends LinkedList<Item> {

	private static final long serialVersionUID = -7330556550048198609L;
	@Deprecated
	public String name;
	
	// TODO: Implement this!!
	public String title;
	public String description;
	
	public final int titleId, descriptionId;
	
	private static final int BUTTONS_X = 4;
	private static final int BUTTONS_Y = 5;
	public final int width, height;

	public Layout(Item[] items) {
		this(BUTTONS_X, BUTTONS_Y, items);
	}
	
	public Layout(int width, int height, Item[] items) {
		this(-1, -1, width, height, items);
	}
	
	public Layout(int titleId, int descriptionId, int width, int height, Item[] items) {
		this.width = width;
		this.height = height;
		
		this.titleId = titleId;
		this.descriptionId = descriptionId;
		
		for(Item item : items) {
			add(item);
		}
	}
}

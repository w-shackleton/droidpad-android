package uk.digitalsquid.droidpad.buttons;

import java.util.LinkedList;

public class Layout extends LinkedList<Item> {

	private static final long serialVersionUID = -7330556550048198609L;
	@Deprecated
	public String name;
	
	// TODO: Implement this!!
	private String title;
	private String description;
	
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

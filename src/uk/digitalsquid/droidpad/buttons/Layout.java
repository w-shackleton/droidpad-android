package uk.digitalsquid.droidpad.buttons;

import java.util.LinkedList;

public class Layout extends LinkedList<Item> {

	private static final long serialVersionUID = -7330556550048198609L;
	public final String name;
	
	private static final int BUTTONS_X = 4;
	private static final int BUTTONS_Y = 5;
	public final int width, height;

	public Layout(String name, Item[] items) {
		this.name = name;
		this.width = BUTTONS_X;
		this.height = BUTTONS_Y;
		
		for(Item item : items) {
			add(item);
		}
	}
	public Layout(String name, int width, int height, Item[] items) {
		this.name = name;
		this.width = width;
		this.height = height;
		
		for(Item item : items) {
			add(item);
		}
	}
}

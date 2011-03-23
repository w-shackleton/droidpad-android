package uk.digitalsquid.droidpad.buttons;

import java.util.LinkedList;

public class Layout extends LinkedList<Item> {

	private static final long serialVersionUID = -7330556550048198609L;
	public final String name;

	public Layout(String name, Item[] items) {
		this.name = name;
		
		for(Item item : items) {
			add(item);
		}
	}
}

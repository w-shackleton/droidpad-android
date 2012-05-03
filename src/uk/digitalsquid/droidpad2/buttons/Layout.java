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

package uk.digitalsquid.droidpad2.buttons;

import java.io.Serializable;
import java.util.LinkedList;

public class Layout extends LinkedList<Item> implements Serializable {

	private static final long serialVersionUID = -7330556550048198609L;
	
	private String title;
	private String description;
	
	/**
	 * Extra details, specific to each layout type
	 */
	private int extraDetail;
	
	public static final int EXTRA_MOUSE_ABSOLUTE = 1;
	public static final int EXTRA_MOUSE_TRACKPAD = 2;
	
	public final int titleId, descriptionId;
	
	/**
	 * The default X size
	 */
	public static final int BUTTONS_X = 4;
	/**
	 * The default Y size
	 */
	public static final int BUTTONS_Y = 5;
	private final int width;

	private final int height;

	public Layout(Item[] items) {
		this(BUTTONS_X, BUTTONS_Y, items);
	}
	
	public Layout(int width, int height, Item[] items) {
		this(-1, -1, width, height, items);
	}
	
	public Layout(String title, String description, int width, int height) {
		setTitle(title);
		setDescription(description);
		titleId = -2; // -2 indicates already set
		descriptionId = -2;
		this.width = width;
		this.height = height;
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

	public Layout(int titleId, int descriptionId, int extraDetail, int width, int height, Item[] items) {
		this.width = width;
		this.height = height;
		
		this.extraDetail = extraDetail;
		
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

	public void setExtraDetail(int extraDetail) {
		this.extraDetail = extraDetail;
	}

	public int getExtraDetail() {
		return extraDetail;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}

package uk.digitalsquid.droidpad.buttons;

import java.util.HashMap;

import uk.digitalsquid.droidpad.buttons.Slider.SliderType;

public final class LayoutManager {
	
	private static final String[] names = new String[] {
		"mouse",
		"slide",
		"1s1",
		"1s2",
		"1s3",
		"1s4",
		"1",
		"2",
		"2b",
		"3"
	};
	
	private static final HashMap<Integer, Layout> generatedLayouts = new HashMap<Integer, Layout>();
	
	static {
	}
	
	public static final String getNameAt(int position) {
		if(position < 0 || position > names.length)
			return "";
		return names[position];
	}
	
	public static final Layout getLayoutAt(int pos) {
		if(pos < 0 || pos > names.length)
			return null;
		Layout l = null;
		try {
			l = generatedLayouts.get(pos);
		}
		catch(IndexOutOfBoundsException e) { }
		
		if(l != null) return l; // Get cached version - save memory etc...
		
		l = createLayoutAt(pos);
		generatedLayouts.put(pos, l);
		return l;
	}
	
	public static final Layout getLayoutByName(String name) {
		for(int i = 0; i < names.length; i++) {
			if(names[i].equalsIgnoreCase(name)) {
				return getLayoutAt(i);
			}
		}
		return getLayoutAt(0);
	}
	
	private static final Layout createLayoutAt(int pos) {
		switch(pos) {
		case 0:
		return new Layout(names[pos], new Item[] {
				new Button(0, 0, 2, 5, "Left-Click"),
				new Button(2, 0, 1, 5, "Middle"),
				new Button(3, 0, 1, 5, "Right"),
		});
		case 1:
		return new Layout(names[pos], new Item[] {
				new Button(0, 3, 4, 2, "Next slide", 30),
				new Button(1, 2, 2, 1, "Prev slide", 16),
				new Button(0, 2, 1, 1, "Start"),
				new Button(3, 2, 1, 1, "End"),
				new ToggleButton(3, 0, 1, 2, "White", 0),
				new ToggleButton(2, 0, 1, 2, "Black", 0),
				new Button(0, 0, 2, 1, "Beginning"),
				new Button(0, 1, 2, 1, "End"),
		});
		case 2:
		return new Layout(names[pos], new Item[] {
				new Button(0, 0, 4, 3, "1", 30),
				new Button(0, 3, 4, 2, "2", 30),
		});
		case 3:
		return new Layout(names[pos], new Item[] {
				new Button(0, 0, 4, 2, "1", 30),
				new Button(0, 2, 2, 3, "2", 30),
				new Button(2, 2, 2, 3, "3", 30),
		});
		case 4:
		return new Layout(names[pos], new Item[] {
				new Button(0, 0, 2, 2, "1", 30),
				new Button(2, 0, 2, 2, "2", 30),
				new Button(0, 2, 2, 3, "3", 30),
				new Button(2, 2, 2, 3, "4", 30),
		});
		case 5:
		return new Layout(names[pos], new Item[] {
				new Button(0, 0, 2, 2, "1", 30),
				new Button(2, 0, 2, 2, "2", 30),
				new Button(0, 2, 2, 3, "3", 30),
				new Button(2, 2, 2, 2, "4", 30),
				new Button(2, 4, 2, 1, "5", 30),
		});
		case 6:
		return new Layout(names[pos], new Item[] {
				new Button(1, 0, 2, 1, "1", 30),
				new Slider(1, 1, 2, 2, SliderType.Both),
				new Slider(0, 1, 1, 2, SliderType.Y),
				new Button(3, 0, 1, 1, "2", 30),
				new Button(0, 0, 1, 1, "3", 30),
				new Button(3, 1, 1, 2, "4", 30),
				new Button(0, 3, 1, 1, "5", 30),
				new ToggleButton(1, 3, 1, 1, "6", 30),
				new ToggleButton(2, 3, 1, 1, "7", 30),
				new Button(3, 3, 1, 1, "8", 30),
				
				new ToggleButton(0, 4, 1, 1, "9", 30),
				new Button(1, 4, 2, 1, "10", 30),
				new Button(3, 4, 1, 1, "11", 30),
		});
		case 7:
		return new Layout(names[pos], new Item[] {
				new Slider(0, 1, 4, 3, SliderType.Both),
				
				new Button(0, 4, 1, 1, "1", 30),
				new Button(1, 4, 2, 1, "2", 30),
				new Button(3, 4, 1, 1, "3", 30),

				new Button(0, 0, 1, 1, "4", 30),
				new Button(1, 0, 1, 1, "5", 30),
				new Button(2, 0, 1, 1, "6", 30),
				new Button(3, 0, 1, 1, "7", 30),
		});
		case 8:
		return new Layout(names[pos], new Item[] {
				new Slider(0, 1, 4, 3, SliderType.Both),
				
				new Button(0, 4, 1, 1, "1", 30),
				new Button(1, 4, 2, 1, "2", 30),
				new ToggleButton(3, 4, 1, 1, "3", 30),

				new ToggleButton(0, 0, 1, 1, "4", 30),
				new Button(1, 0, 1, 1, "5", 30),
				new Button(2, 0, 1, 1, "6", 30),
				new ToggleButton(3, 0, 1, 1, "7", 30),
		});
		case 9:
		return new Layout(names[pos], new Item[] {
				new Slider(1, 1, 2, 2, SliderType.Both),
				
				new Slider(1, 0, 2, 1, SliderType.X),
				new Slider(0, 1, 1, 2, SliderType.Y),
				new Slider(1, 3, 2, 1, SliderType.X),
				new Slider(3, 1, 1, 2, SliderType.Y),
				
				new Button(0, 3, 1, 1, "1", 30),
				new Button(3, 3, 1, 1, "2", 30),

				new Button(0, 4, 1, 1, "3", 30),
				new Button(1, 4, 1, 1, "4", 30),
				new Button(2, 4, 1, 1, "5", 30),
				new Button(3, 4, 1, 1, "6", 30),
				
				new ToggleButton(0, 0, 1, 1, "7", 30),
				new ToggleButton(3, 0, 1, 1, "8", 30),
		});
		default:
			return null;
		}
	}
}

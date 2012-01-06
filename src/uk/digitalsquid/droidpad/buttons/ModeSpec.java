package uk.digitalsquid.droidpad.buttons;

import java.io.Serializable;

/**
 * Specifies how DroidPad will run.
 * @author william
 *
 */
public class ModeSpec implements Serializable {

	private static final long serialVersionUID = 4294164511803037163L;

	private Layout layout;
	
	private int mode;

	public static final int LAYOUTS_SLIDE = 3;
	public static final int LAYOUTS_MOUSE = 2;
	public static final int LAYOUTS_JS = 1;
	public Layout getLayout() {
		return layout;
	}
	public void setLayout(Layout layout) {
		this.layout = layout;
	}
	public int getMode() {
		return mode;
	}
	public String getModeString() {
		switch(mode) {
		case LAYOUTS_JS:
			return "1"; // Compatibility - old version assumes number = js mode
		case LAYOUTS_MOUSE:
			return "mouse";
		case LAYOUTS_SLIDE:
			return "slide";
		}
		return "other";
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
}

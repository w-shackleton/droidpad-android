package uk.digitalsquid.droidpad2.serialise;

import uk.digitalsquid.droidpad2.buttons.AnalogueData;
import uk.digitalsquid.droidpad2.buttons.Item;
import uk.digitalsquid.droidpad2.buttons.Layout;

/**
 * Serialises data to the network stream using the old method, ie. by text. This is to be superseded
 * by a binary format.
 * @author william
 *
 */
public class ClassicSerialiser {
	public static final String formatLine(AnalogueData analogue, Layout layout) {
		String data = "[{" + (analogue.isInvertX() ? -analogue.getAccelerometer().x : analogue.getAccelerometer().x) + ","
				+ (analogue.isInvertY() ? -analogue.getAccelerometer().y : analogue.getAccelerometer().y) + "," +
				analogue.getAccelerometer().z + "}";
		if(layout != null) {
			for(Item item : layout) {
				data += ";";
				data += item.getOutputString();
			}
		}
		data += "]\n"; // [] for easy string view
		
		return data;
	}
	
	public static final String writeStopCommand() {
		return "<STOP>\n";
	}
}

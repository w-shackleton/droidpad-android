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
		String data = "[{" + (analogue.isInvertX() ? analogue.getAccelerometer()[0] : -analogue.getAccelerometer()[0]) + ","
				+ (analogue.isInvertY() ? analogue.getAccelerometer()[1] : -analogue.getAccelerometer()[1]) + "," +
				analogue.getAccelerometer()[2] + "}";
		if(layout != null) {
			for(Item item : layout) {
				data += ";";
				data += item.getOutputString();
			}
		}
		data += "]\n"; // [] for easy string view
		
		return data;
	}
}

package uk.digitalsquid.droidpad2.serialise;

import java.io.DataOutputStream;
import java.io.IOException;

import uk.digitalsquid.droidpad2.buttons.AnalogueData;
import uk.digitalsquid.droidpad2.buttons.Item;
import uk.digitalsquid.droidpad2.buttons.Layout;

/**
 * Format of this binary structure is available in a separate file
 * attached to this source
 * @author william
 *
 */
public class BinarySerialiser {
	
	static final int HEADER_FLAG_HAS_ACCEL = 0x1;
	static final int HEADER_FLAG_HAS_GYRO = 0x2;

	public static final void writeBinary(DataOutputStream out, AnalogueData analogue, Layout layout) throws IOException {
		writeHeader(out, analogue, layout);
		for(Item item : layout) {
			item.writeBinary(out);
		}
	}
	
	private static final void writeHeader(DataOutputStream out, AnalogueData analogue, Layout layout) throws IOException {
		final int elems = layout.size();
		final int flags = (analogue.hasAccelerometer() ? HEADER_FLAG_HAS_ACCEL : 0) |
					(analogue.hasGyroscope() ? HEADER_FLAG_HAS_GYRO : 0);
		
		final float accelX = analogue.hasAccelerometer() ? analogue.getAccelerometer()[0] : 0;
		final float accelY = analogue.hasAccelerometer() ? analogue.getAccelerometer()[1] : 0;
		final float accelZ = analogue.hasAccelerometer() ? analogue.getAccelerometer()[2] : 0;
		final float gyroX = analogue.hasGyroscope() ? analogue.getGyroscope()[0] : 0;
		final float gyroY = analogue.hasGyroscope() ? analogue.getGyroscope()[1] : 0;
		final float gyroZ = analogue.hasGyroscope() ? analogue.getGyroscope()[2] : 0;
		final float reservedX = 0;
		final float reservedY = 0;
		final float reservedZ = 0;
		
		out.writeInt(elems);
		out.writeInt(flags);
		out.writeFloat(accelX);
		out.writeFloat(accelY);
		out.writeFloat(accelZ);
		out.writeFloat(gyroX);
		out.writeFloat(gyroY);
		out.writeFloat(gyroZ);
		out.writeFloat(reservedX);
		out.writeFloat(reservedY);
		out.writeFloat(reservedZ);
	}
}

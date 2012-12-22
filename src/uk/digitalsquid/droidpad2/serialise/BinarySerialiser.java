package uk.digitalsquid.droidpad2.serialise;

import java.io.DataOutputStream;
import java.io.IOException;

import uk.digitalsquid.droidpad2.LogTag;
import uk.digitalsquid.droidpad2.buttons.AnalogueData;
import uk.digitalsquid.droidpad2.buttons.Item;
import uk.digitalsquid.droidpad2.buttons.Layout;

/**
 * Format of this binary structure is available in a separate file
 * attached to this source
 * @author william
 *
 */
public class BinarySerialiser implements LogTag {
	
	static final int HEADER_FLAG_HAS_ACCEL = 0x1;
	static final int HEADER_FLAG_HAS_GYRO = 0x2;
	static final int HEADER_FLAG_STOP = 0x4;
	
	static final byte[] HEADER_BYTES = "DPAD".getBytes();

	public static final void writeBinary(DataOutputStream out, AnalogueData analogue, Layout layout) throws IOException {
		writeHeader(out, analogue, layout);
		for(Item item : layout) {
			item.writeBinary(out);
		}
		out.flush();
	}
	
	private static final void writeHeader(DataOutputStream out, AnalogueData analogue, Layout layout) throws IOException {
		final int elems = layout.size();
		final int flags = (analogue.hasAccelerometer() ? HEADER_FLAG_HAS_ACCEL : 0) |
					(analogue.hasGyroscope() ? HEADER_FLAG_HAS_GYRO : 0);
		
		float accelX = analogue.hasAccelerometer() ? analogue.getAccelerometer().x : 0;
		float accelY = analogue.hasAccelerometer() ? analogue.getAccelerometer().y : 0;
		final float accelZ = analogue.hasAccelerometer() ? analogue.getAccelerometer().z : 0;
		if(analogue.isInvertX()) accelX = -accelX;
		if(analogue.isInvertY()) accelY = -accelY;
		final float gyroX = analogue.hasGyroscope() ? analogue.getGyroscope().x : 0;
		final float gyroY = analogue.hasGyroscope() ? analogue.getGyroscope().y : 0;
		final float gyroZ = analogue.hasGyroscope() ? analogue.getGyroscope().z : 0;
		final float gyroAcc = analogue.hasGyroscope() ? analogue.getWorldRotation() : 0;
		final float reservedX = 0;
		final float reservedY = 0;
		final float reservedZ = 0;
		
		out.write(HEADER_BYTES);
		out.writeInt(elems);
		out.writeInt(flags);
		out.writeFloat(accelX);
		out.writeFloat(accelY);
		out.writeFloat(accelZ);
		out.writeFloat(gyroX);
		out.writeFloat(gyroY);
		out.writeFloat(gyroZ);
		out.writeFloat(gyroAcc);
		out.writeFloat(reservedX);
		out.writeFloat(reservedY);
		out.writeFloat(reservedZ);
	}
	
	/**
	 * Sends a stop command out in the binary format.
	 * @param out
	 * @throws IOException 
	 */
	public static final void writeStopCommand(DataOutputStream out) throws IOException {
		final int elems = 0;
		final int flags = HEADER_FLAG_STOP;
		
		final float accelX = 0;
		final float accelY = 0;
		final float accelZ = 0;
		final float gyroX = 0;
		final float gyroY = 0;
		final float gyroZ = 0;
		final float gyroZN = 0;
		final float reservedX = 0;
		final float reservedY = 0;
		final float reservedZ = 0;
		
		out.write(HEADER_BYTES);
		out.writeInt(elems);
		out.writeInt(flags);
		out.writeFloat(accelX);
		out.writeFloat(accelY);
		out.writeFloat(accelZ);
		out.writeFloat(gyroX);
		out.writeFloat(gyroY);
		out.writeFloat(gyroZ);
		out.writeFloat(gyroZN);
		out.writeFloat(reservedX);
		out.writeFloat(reservedY);
		out.writeFloat(reservedZ);
		out.flush();
	}
}

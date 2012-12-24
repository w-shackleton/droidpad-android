package uk.digitalsquid.droidpad.buttons;

import uk.digitalsquid.droidpad.Vec3;

public class AnalogueData {
	private final Vec3 accelerometer;
	private final Vec3 gyroscope;
	private final float worldRotation;
	
	private final boolean invertX;
	private final boolean invertY;
	
	public AnalogueData(Vec3 accelerometer, Vec3 gyroscope, float worldRotation, boolean invX, boolean invY) {
		this.accelerometer = accelerometer;
		this.gyroscope = gyroscope;
		this.worldRotation = worldRotation;
		invertX = invX;
		invertY = invY;
	}
	
	@Deprecated
	public AnalogueData(float[] accelerometer, float[] gyroscope, boolean invX, boolean invY) {
		this.accelerometer = new Vec3(accelerometer);
		this.gyroscope = new Vec3(gyroscope);
		this.worldRotation = gyroscope[3];
		invertX = invX;
		invertY = invY;
	}

	@Deprecated
	public float[] getAccelerometerOld() {
		return accelerometer.toFloats();
	}
	
	public Vec3 getAccelerometer() {
		return accelerometer;
	}

	@Deprecated
	public float[] getGyroscopeOld() {
		return accelerometer.toFloats();
	}
	
	public Vec3 getGyroscope() {
		return accelerometer;
	}
	
	public boolean hasAccelerometer() {
		return accelerometer != null;
	}
	public boolean hasGyroscope() {
		return gyroscope != null;
	}

	public boolean isInvertX() {
		return invertX;
	}

	public boolean isInvertY() {
		return invertY;
	}

	public float getWorldRotation() {
		return worldRotation;
	}
}

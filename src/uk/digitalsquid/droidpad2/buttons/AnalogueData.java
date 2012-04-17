package uk.digitalsquid.droidpad2.buttons;

public class AnalogueData {
	private final float[] accelerometer;
	private final float[] gyroscope;
	
	private final boolean invertX;
	private final boolean invertY;
	
	public AnalogueData(float[] accelerometer, float[] gyroscope, boolean invX, boolean invY) {
		this.accelerometer = accelerometer;
		this.gyroscope = gyroscope;
		invertX = invX;
		invertY = invY;
	}

	public float[] getAccelerometer() {
		return accelerometer;
	}

	public float[] getGyroscope() {
		return gyroscope;
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
}

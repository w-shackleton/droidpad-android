package uk.digitalsquid.droidpad2;

import android.util.FloatMath;

/**
 * Simple 3D vector class
 * @author william
 *
 */
public class Vec3 {
	float x, y, z;
	
	public Vec3() { }
	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3(float[] vals) {
		this.x = vals[0];
		this.y = vals[1];
		this.z = vals[2];
	}
	
	public Vec3 getUnitVector() {
		float m = magnitude();
		return new Vec3(x / m, y / m, z / m);
	}
	
	public float magnitude() {
		return FloatMath.sqrt(x*x + y*y + z*z);
	}
	
	public float dot(Vec3 other) {
		return x * other.x +
			   y * other.y +
			   z * other.z;
	}
}

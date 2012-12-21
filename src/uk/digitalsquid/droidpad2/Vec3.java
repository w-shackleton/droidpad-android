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
	
	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	/**
	 * @param data Must be a float array of 3 elements
	 */
	public void set(float[] data) {
		this.x = data[0];
		this.y = data[1];
		this.z = data[2];
	}
	
	public Vec3 mulLocal(float scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}
	public Vec3 mul(float scalar) {
		Vec3 ret = new Vec3();
		ret.x = x * scalar;
		ret.y = y * scalar;
		ret.z = z * scalar;
		return ret;
	}
	
	public Vec3 addLocal(Vec3 r) {
		x += r.x;
		y += r.y;
		z += r.z;
		return this;
	}
	
	public Vec3 minusLocal(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
}

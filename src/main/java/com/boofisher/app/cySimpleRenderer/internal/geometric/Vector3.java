package com.boofisher.app.cySimpleRenderer.internal.geometric;

/**
 * This class represents a 3-dimensional vector object, which can be useful
 * for certain vector calculations
 */
public class Vector3 {
	
	/** A unit vector representing the direction of the positive x-axis. */
	public static final Vector3 POSITIVE_X_DIRECTION = new Vector3(1, 0, 0);
	
	/** A unit vector representing the direction of the positive y-axis. */
	public static final Vector3 POSITIVE_Y_DIRECTION = new Vector3(0, 1, 0);
	
	/** A unit vector representing the direction of the positive z-axis. */
	public static final Vector3 POSITIVE_Z_DIRECTION = new Vector3(0, 0, 1);
	
	/** The x-coordinate */
	private double x;
	
	/** The y-coordinate */
	private double y;
	
	/** The z-coordinate */
	private double z;
	
	/** Default constructor */
	public Vector3() {
		// Note that this method uses Java's default setting to 
		// initialize doubles to 0
	}
	
	/** Create a vector with specified coordinates
	 * 
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param z The z coordinate
	 */
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/** Create a vector with equal coordinates to another vector
	 * 
	 * @param other The other vector to obtain coordinates from
	 */
	public Vector3(Vector3 other) {
		x = other.x;
		y = other.y;
		z = other.z;
	}
	
	/** Return the x-coordinate
	 * 
	 * @return The x coordinate
	 */
	public double x() {
		return x;
	}
	
	/** Return the y-coordinate
	 * 
	 * @return The y coordinate
	 */
	public double y() {
		return y;
	}
	
	/** Return the z-coordinate
	 * 
	 * @return The z coordinate
	 */
	public double z() {
		return z;
	}
	
	/** Set the coordinate values of the vector
	 * 
	 * @param x The x-coordinate
	 * @param y The y-coordinate
	 * @param z The z-coordinate
	 */
	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/** Set the coordinate values of the vector to be equal to another vector
	 * 
	 * @param other The other vector to compare against
	 */
	public void set(Vector3 other) {
		x = other.x;
		y = other.y;
		z = other.z;
	}
	
	/** Return a new vector equal to the result of this plus another vector
	 * 
	 * @param other The vector to add
	 * @return A new {@link Vector3} representing the sum
	 */
	public Vector3 plus(Vector3 other) {
		return new Vector3(x + other.x, y + other.y, z + other.z);
	}
	
	/** Return a new vector equal to the result of this plus a vector
	 * with the given coordinates
	 * 
	 * @param x The x-coordinate of the vector to add
	 * @param y The y-coordinate of the vector to add
	 * @param z The z-coordinate of the vector to add
	 * @return A new vector representing the result of this plus a vector
	 * with the given coordinates
	 */
	public Vector3 plus(double x, double y, double z) {
		return new Vector3(this.x + x, this.y + y, this.z + z);
	}
	
	/** Add another vector to this vector, such that this vector becomes
	 * the sum
	 * 
	 * @param other The vector to add
	 */
	public void addLocal(Vector3 other) {
		x += other.x;
		y += other.y;
		z += other.z;
	}
	
	/** Add the specified values to the coordinates of this vector
	 * 
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param z Z-coordinate
	 */
	public void addLocal(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}
	
	/** Subtract another vector from this vector
	 * 
	 * @param other The vector to subtract
	 * @return A new vector representing the result of this minus the
	 * other vector
	 */
	public Vector3 subtract(Vector3 other) {
		return new Vector3(x - other.x, y - other.y, z - other.z);
	}
	
	/** Subtract a 3-vector with the specified coordinates from this vector
	 * 
	 * @param x The x-coordinate of the vector to subtract
	 * @param y The y-coordinate of the vector to subtract
	 * @param z The z-coordinate of the vector to subtract
	 * @return A new vector representing the result of this minus a vector
	 * with the given coordinates
	 */
	public Vector3 subtract(double x, double y, double z) {
		return new Vector3(this.x - x, this.y - y, this.z - z);
	}
	
	/** Subtract the specified values from the coordinates of this vector
	 * 
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 * @param z Z-coordinate
	 */
	public void subtractLocal(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
	}
	
	/** Find the angle (<180 degrees) between this vector and another vector
	 * 
	 * @param other The other vector used to find the angle 
	 * @return The angle, in radians, less than pi rad
	 */
	public double angle(Vector3 other) {
		// TODO: Find a faster arccos and sqrt method
		
		double lengthSquare = x * x + y * y + z * z;
		double otherLengthSquare = other.x * other.x + other.y * other.y
				+ other.z * other.z;

		// TODO: Check if alternative is needed to prevent NaN
		double cosArgument = (x * other.x + y * other.y + z * other.z)
				/ Math.sqrt(lengthSquare * otherLengthSquare);

		if (Double.isNaN(cosArgument)) {
			System.out.println("cosArgument NaN for Vector3.angle(Vector3)");
			Thread.dumpStack();
			return 0;
		} else if (cosArgument >= 1) {
			return 0;
		} else if (cosArgument <= -1) {
			return Math.PI;
		} else {
			return Math.acos(cosArgument);
		}
	}
	
	public double angleAcute(Vector3 other) {
		double angle = this.angle(other);
		
		if (angle > Math.PI / 2) {
			return (Math.PI - angle);
		} else {
			return angle;
		}
	}
	
	/** Find the cross product between this vector and another vector
	 * 
	 * @param other The other vector used to find the cross product
	 * @return The cross product, as a new {@link Vector3} object
	 */
	public Vector3 cross(Vector3 other) {
		return new Vector3(y * other.z - z * other.y,
				z * other.x - x * other.z, x * other.y - y * other.x);
	}
	
	/** Find the cross product between this vector and a vector with the
	 * specified coordinates
	 * 
	 * @param x The x-coordinate of the other vector
	 * @param y The y-coordinate of the other vector
	 * @param z The z-coordinate of the other vector
	 * @return A new {@link Vector3} object representing the cross product
	 */
	public Vector3 cross(double x, double y, double z) {
		return new Vector3(this.y * z - this.z * y,
				this.z * x - this.x * z, this.x * y - this.y * x);
	}
	
	/** Find the dot product between this and another vector
	 * 
	 * @param other The other vector used to find the dot product
	 * @return The dot product
	 */
	public double dot(Vector3 other) {
		return (x * other.x + y * other.y + z * other.z);
	}
	
	/** Set this vector to be equal to the cross product between itself
	 * and the other vector
	 * 
	 * @param other The other vector used to find the cross product
	 */
	public void crossLocal(Vector3 other) {
		set(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y
				- y * other.x);
	}
	
	/** Multiply this vector by a scalar, returning the result as a 
	 * new {@link Vector3} object
	 * 
	 * @param scalar The scalar used to perform the multiplication
	 * @return A new vector representing the result
	 */
	public Vector3 multiply(double scalar) {
		return new Vector3(x * scalar, y * scalar, z * scalar);
	}
	
	/** Set this vector to be equal to the result of itself multiplied by
	 * a scalar
	 * 
	 * @param scalar The scalar used to perform the mutliplication
	 */
	public void multiplyLocal(double scalar) {
		x = scalar * x;
		y = scalar * y;
		z = scalar * z;
	}
	
	/** Set this vector to be equal to the result of dividing it by a scalar
	 * 
	 * @param scalar The scalar used to perform the division
	 */
	public void divideLocal(double scalar) {
		x = x / scalar;
		y = y / scalar;
		z = z / scalar;
	}
	
	/** Perform a normalization operation on the vector by dividing it by its
	 * magnitude
	 * 
	 * @return A new vector representing the normalized result, or the
	 * 0-vector if its previous magnitude was too close to 0
	 */
	public Vector3 normalize() {
		double length = Math.sqrt(x * x + y * y + z * z);
		if (length > Double.MIN_NORMAL) {
			return new Vector3(x / length, y / length, z / length);
		} else {
			return new Vector3(0, 0, 0);
		}
	}
	
	/** Obtain the magnitude, or the length, of the vector
	 * 
	 * @return The magnitude of the vector
	 */
	public double magnitude() {
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	/** Obtain the square of the magnitude, which is faster than obtaining 
	 * the magnitude itself for comparison calculations
	 * 
	 * @return The square of the magnitude
	 */
	public double magnitudeSquared() {
		return (x * x + y * y + z * z);
	}
	
	/** Inverts this vector so that the result points in the opposite direction.
	 * 
	 * @return A vector representing the invert of this vector.
	 */
	public Vector3 invert() {
		return multiply(-1);
	}
	
	/** Set this vector to be equal to itself divided by its magnitude
	 */
	public void normalizeLocal() {
		double length = Math.sqrt(x * x + y * y + z * z);
		if (length > Double.MIN_NORMAL) {
			x /= length;
			y /= length;
			z /= length;
		} else {
			x = y = z = 0;
		}
	}
	
	/** Obtain the distance between the position vectors represented by 
	 * this and another vector
	 *
	 * @param other The other vector used to find the distance
	 * @return
	 */
	public double distance(Vector3 other) {
		return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2)
				+ Math.pow(z - other.z, 2));
	}
	
	/** Obtain the square of the distance between the position vectors 
	 * represented by this and another vector
	 * 
	 * @param other
	 * @return
	 */
	public double distanceSquared(Vector3 other) {
		return Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2)
				+ Math.pow(z - other.z, 2);
	}
	
	/** Project the vector onto the plane passing through the origin, 
	 * perpendicular to the given normal
	 * 
	 * @param The normal vector that is perpendicular to to the mentioned plane                          
	 * @return A new vector representing the projection of this vector onto the
	 * plane perpendicular to the given normal vector
	 */
	public Vector3 projectNormal(Vector3 normal) {
		return subtract(normal.multiply(this.dot(normal)));
	}
	
	/** Treating this vector as a position vector, rotate it about the
	 * given normal that passes through the origin by the specified
	 * angle in the right-hand rule direction, in radians.
	 * 
	 * The given normal must be perpendicular to this vector.
	 * 
	 * @param normal The normal vector used for the rotation
	 * @param angle The angle in radians to rotate this vector
	 * @return A new vector representing the rotated vector
	 */
	public Vector3 rotateOld(Vector3 normal, double angle) {
    	// Parametric equation for circle in 3D space:
    	// P = Rcos(t)u + Rsin(t)nxu + c
    	//
    	// Where:
    	//  -u is a unit vector from the centre of the circle to any point
    	// on the circumference
    	//  -R is the radius
    	//  -n is a unit vector perpendicular to the plane
        //  -c is the centre of the circle.
    	
		//TODO: obtain a more efficient sin function
    	Vector3 rotated;
    	
    	rotated = normal.normalize();
    	rotated.crossLocal(this);
    	rotated.multiplyLocal(Math.sin(angle));
    	rotated.addLocal(this.multiply(Math.cos(angle)));
    	
    	return rotated;
    }

	
	/**
	 * Rotates this vector about the axis specified by the normal vector,
	 * by a specified angle, in accordance with the right-hand rule .
	 * 
	 * @param normal The normal vector used to perform the rotation
	 * @param angle The angle, in radians, to rotate about the normal in
	 * in accordance with the right-hand rule (thumb points along direction
	 * of normal).
	 * @return The rotated vector.
	 */
	public Vector3 rotate(Vector3 normal, double angle) {
    	// Parametric equation for circle in 3D space:
    	// P = Rcos(t)u + Rsin(t)nxu + c
    	//
    	// Where:
    	//  -u is a unit vector from the centre of the circle to any point
    	// on the circumference
    	//  -R is the radius
    	//  -n is a unit vector perpendicular to the plane
        //  -c is the centre of the circle.
    	
		//TODO: obtain a more efficient sin function
    	Vector3 rotated;
    	
    	// Decompose vector to be rotated into 2 components:
    	// 1. perpendicular to normal
    	// 2. parallel to normal
    	Vector3 perpendicularOffset = this.projectNormal(normal);
    	Vector3 parallelOffset = this.subtract(perpendicularOffset);
    	
    	rotated = normal.normalize();
    	rotated.crossLocal(perpendicularOffset);
    	rotated.multiplyLocal(Math.sin(angle));
    	rotated.addLocal(perpendicularOffset.multiply(Math.cos(angle)));
    	
    	// Add back the parallel component
    	rotated.addLocal(parallelOffset);
    	
    	return rotated;
    }
	
	/** Treating this and the given vector as position vectors,
	 * moves this vector towards the given vector such that
	 * the distance between them becomes equal to "fraction"
	 * times the original distance.
	 * 
	 * @param other The vector to move towards
	 * @param fraction How much to multiply the distance between the
	 * vectors. Distance is reduced for values less than 1, increased
	 * for values greater than 1. Negative values will place this
	 * vector on the other side of the target vector as in a mirror image.
	 * @return
	 */
	public Vector3 towards(Vector3 other, double fraction) {
		Vector3 offset = other.subtract(this);
		offset.multiplyLocal(fraction);
	
		return offset.plus(this);
	}
	
	/** Give a string representation of this vector
	 * 
	 * @return The string representation, in format (x, y, z)
	 */
	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}
	
	/** Make a copy of this vector
	 * 
	 * @return A copy of this vector object
	 */
	public Vector3 copy() {
		return new Vector3(this);
	}
}


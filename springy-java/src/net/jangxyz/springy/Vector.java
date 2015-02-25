package net.jangxyz.springy;

public class Vector {
	public double x;
	public double y;

    public static Vector ZERO = new Vector(0, 0);
	public static Vector random() {
		return new Vector(10.0 * (Math.random() - 0.5), 10.0 * (Math.random() - 0.5));
	}


	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString() {
		return String.format("(%f, %f)", this.x, this.y);		
	}
	public String toString(int precision) {
		String formatStr = String.format("(%%.%df, %%.%df)", precision, precision);
		return String.format(formatStr, this.x, this.y);		
	}

	//
	// operations
	//
	public Vector add(Vector v) {
		return new Vector(this.x + v.x, this.y + v.y);
	}

	public Vector subtract(Vector v) {
		return new Vector(this.x - v.x, this.y - v.y);
	}

	public Vector multiply(double n) {
		return new Vector(this.x * n, this.y * n);
	}

	public Vector divide(double n) {
		// Avoid divide by zero errors..
		return new Vector((this.x / n), (this.y / n));
	}
	
	public double magnitude2() {
		return this.x*this.x + this.y*this.y;
	}

	public double magnitude() {
		return Math.sqrt(this.x*this.x + this.y*this.y);
	}

	public Vector normal() {
		return new Vector(-this.y, this.x);
	}

	public Vector normalise() {
		return this.divide(this.magnitude());
	}
	
	public boolean equals(Vector v) {
		return this.x == v.x && this.y == v.y;
	}

}

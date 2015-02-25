package mikechambers.quadtree;

public class Bound {
	double x;
	double y;
	double width;
	double height;
	
	public Bound(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public double getX() { 
		return this.x;
	}
	public double getY() { 
		return this.y;
	}
	public double getWidth() {
		return width;
	}
	public double getHeight() {
		return height;
	}
	
	public String toString() {
		return String.format("(%.2f,%.2f: %.2fx%.2f)", x,y,width,height);
	}
}
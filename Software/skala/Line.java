package skala;

import javafx.geometry.Point3D;

public class Line {
	private Point3D from;
	private Point3D to;
	
	public Line(Point3D from, Point3D to) {
		super();
		this.from = from;
		this.to = to;
	}
	

	public Line(double x1, double y1, double x2, double y2) {
		super();
		this.from = new Point3D(x1, y1, 0.0);
		this.to = new Point3D(x2, y2, 0.0);
	}
	
	public Point3D getFrom() {
		return from;
	}
	public void setFrom(Point3D from) {
		this.from = from;
	}
	public Point3D getTo() {
		return to;
	}
	public void setTo(Point3D to) {
		this.to = to;
	}
	
	
}

package skala;

import javafx.geometry.Point3D;

public interface RealWorldObject {
	
	public static String type = null;
	public Point3D getPosition();
	public void setPosition(Point3D position);
	String getType();
}

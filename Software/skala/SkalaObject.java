package skala;

import java.util.HashMap;

import javafx.geometry.Point3D;
import skala.behaviour.Behaviourable;

public abstract class SkalaObject implements RealWorldObject, Behaviourable{

	public Point3D position = null;	
	String type = null;
	private HashMap<String, Object> data;

	public SkalaObject() {
		this.position = new Point3D(0.0,0.0,0.0);
		this.data = new HashMap<String, Object>();
	}
	
	public HashMap<String, Object> getData() {
		return this.data;
	}
	
	public Object getData(String key) {
		return this.data.get(key);
	}
	
	public boolean hasData(String key){
		return this.getData().containsKey(key);
	}
	
	public void setData(HashMap<String, Object> data ) {
		this.data = data;
	}
	
	public void setData(String key, Object value) {
		this.data.put(key, value);
	}

	public double distance(SkalaObject to){
		return this.position.distance(to.position);
	}
	

	public double hDistance(SkalaObject to){
		return this.getHPosition().distance(to.getHPosition());
	}

	public double hDistance(Point3D to){
		return this.getHPosition().distance(new Point3D(to.getX(), 2.5, to.getZ()));
	}

	public Point3D getHPosition() {
		return new Point3D(this.position.getX(), 2.5, this.position.getZ());
	}
	
	public Point3D getHPosition(double y) {
		return new Point3D(this.position.getX(), y, this.position.getZ());
	}
	
	@Override
	public Point3D getPosition() {
		return position;
	}

	@Override
	public void setPosition(Point3D position) {
		this.position = position;
		
	}
	
	@Override
	public String getType() {
		return this.type;
	}
	
	

}

package skala;

import java.util.ArrayList;
import java.util.HashMap;

import edu.ufl.digitalworlds.j4k.DepthMap;
import edu.ufl.digitalworlds.j4k.J4KSDK;
import edu.ufl.digitalworlds.j4k.Skeleton;
import javafx.geometry.Point3D;
import skala.behaviour.Behaviourable;

public class Sensor extends J4KSDK implements RealWorldObject, Behaviourable{

	Point3D angles;
	
	protected String type = "sensor";
	
	int id = 0;
	HashMap<String, Object> data;
	
	public Sensor(int id, Point3D position, Point3D angles) {
		super();

		this.position = position;
		this.angles = angles;
		this.id = id;
	}
	
	Point3D position = new Point3D(0, 0.93, 0);
	
	int counter=0;
	long time=0;
	
	private ArrayList<KinectListener> listeners = new ArrayList<KinectListener>();

	public void addListener(KinectListener listener){
		listeners.add(listener);
	}
	
	public double[] skeletonToRealWorld(double[] ds){
		Point3D point = new Point3D(ds[0], ds[1], ds[2]);
		
		point.subtract(getPosition());
		
		double cx = Math.cos(angles.getX());
		double sx = Math.sin(angles.getX());
		double cy = Math.cos(angles.getY());
		double sy = Math.sin(angles.getY());
		
		double pX = point.getX();
		double pY = point.getY();
		double pZ = point.getZ();
		
		pY = (cx*pY) - (sx*pZ);
		pZ = (sx*pY) + (cx*pZ);
		
		pX = (cy * pX) - (sy * pZ);
		pZ = (sy * pX) + (cy * pZ);
		

		pX += getPosition().getX();
		pY += getPosition().getY();
		pZ += getPosition().getZ();
		
		double ret[] = {pX, pY, pZ};
		
		return ret;
	}
	
	@Override
	public void onSkeletonFrameEvent(boolean[] skeleton_tracked, float[] positions, float[] orientations, byte[] joint_status) {
//		System.out.println("A new skeleton frame was received.");

		Skeleton[] skeletons = getSkeletons();
		
	    for(int i=0;i<skeletons.length;i++){
	    	Skeleton skeleton = skeletons[i];
	    	double realWorldPositions[][] = new double[skeleton.JOINT_COUNT][3];
			
	    	for(int j = 0; j < skeleton.JOINT_COUNT; j++) {
	    		realWorldPositions[j] = skeletonToRealWorld(skeleton.get3DJoint(j));
	    	}
	    	
	    	if(skeleton!=null)
	    	{
	    		if(skeleton.isTracked())
	    		{	
	    			if(listeners.size() != 0) {
	    				for(KinectListener listener : listeners){
	    					listener.handleSkeletonEvent(skeleton, realWorldPositions, id);
	    				}
	    			}
	    		}
	    	}
	    }	
	}

	@Override
	public void onColorFrameEvent(byte[] color_frame) {
//		System.out.println("A new color frame was received.");
	}

	@Override
	public void onDepthFrameEvent(short[] depth_frame, byte[] body_index, float[] xyz, float[] uv) {

		for(KinectListener listener : listeners){
			listener.handleDepthFrame(new DepthMap(getDepthWidth(), getDepthHeight(), xyz), xyz);
		}
	}
	
	public boolean start() {
		super.start(J4KSDK.COLOR|J4KSDK.DEPTH|J4KSDK.UV|J4KSDK.XYZ|J4KSDK.SKELETON);
		return isInitialized();
	}

	@Override
	public Point3D getPosition() {
		return position;
	}

	@Override
	public void setPosition(Point3D position) {
		this.position = position;
	}
	
	public String getType() {
		return this.type;
	}

	public HashMap<String, Object> getData() {
		return this.data;
	}
	
	public Object getData(String key) {
		return this.data.get(key);
	}
	
	public void setData(HashMap<String, Object> data ) {
		this.data = data;
	}
	
	public void setData(String key, Object value) {
		this.data.put(key, value);
	}
	
	
}

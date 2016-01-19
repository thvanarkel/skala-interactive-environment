package skala;

import java.util.Vector;

import edu.ufl.digitalworlds.j4k.Skeleton;
import javafx.geometry.Point3D;

public class User extends SkalaObject implements Stated<User.State>{

	protected State state;
	public static enum State {
		_Initial,
		Calming,
		Climaxing,
		Sitting,
		Invalid
	};	
	
	Skeleton skeleton;
	
	final String type = "user";
	
	private int id;
	
	public long lastSeen;
	public long firstSeen;
	public long invalidationTime;
	
	Vector<UserListener> listeners;
	
	public Vector<Point3D> joints;
	Vector<Point3D> previousJoints;

	boolean valid = true;
	
	public final static double SUDDEN_MOVEMENT_THRESHOLD = 0.005;
	public final double MOVEMENT_THRESHOLD = 0.0001;
	public static final double POINTING_THRESHOLD = 0.3; 
	
	public User(int id) {
		super();
		listeners = new Vector<UserListener>();
		this.setId(id);
		joints = new Vector<Point3D>();
		previousJoints = new Vector<Point3D>();
		for(int i = 0; i < skeleton.JOINT_COUNT; i++) {
			joints.addElement(new Point3D(0.0,0.0,0.0));
			previousJoints.addElement(new Point3D(0.0,0.0,0.0));
		}
		lastSeen = System.nanoTime();
		firstSeen = System.nanoTime();
		invalidationTime = 0;
		this.state = State._Initial;
	}
	
	public double getVelocity(int jointId){
		long now = System.nanoTime();

		double d = getDistance(jointId);
		
		double dt = (now - lastSeen) / 1e9;
		
		return (d/dt);
	}
	
	public double getDistance(int jointId){
		Point3D curPos = joints.get(jointId);
		Point3D prevPos = previousJoints.get(jointId);
		
		return curPos.distance(prevPos);
	}
	
	public Point3D getDirection(int jointId) {

		Point3D curPos = joints.get(jointId);
		Point3D prevPos = previousJoints.get(jointId);
		
		return curPos.subtract(prevPos).normalize();
	}
	
	public Vector<Point3D> updateSkeleton(Skeleton skeleton, double[][] realWorldPositions) {
		byte[] jointTrackingStates = skeleton.getJointTrackingStates();
		for(int i = 0; i < skeleton.JOINT_COUNT; i++) {
			double[] pos = realWorldPositions[i];
			Point3D point = new Point3D(pos[0], pos[1], pos[2]);
			
			previousJoints.set(i, joints.get(i));
			
			joints.set(i, point);
			
			if(i == Skeleton.HAND_RIGHT || i == Skeleton.HAND_LEFT ){
				double velocity = getVelocity(i);
				Point3D direction = getDirection(i);
				if(jointTrackingStates[i] == Skeleton.TRACKED){
					if(velocity > SUDDEN_MOVEMENT_THRESHOLD) {
						this.fireUserSuddenMoveEvent(i, direction, velocity);
					}
					
				}
			}
		}
		
		this.setPosition(joints.get(Skeleton.SPINE_MID));
		
		double velocity = getVelocity(Skeleton.SPINE_MID);
		
		if(jointTrackingStates[Skeleton.SPINE_MID] == Skeleton.TRACKED && velocity > MOVEMENT_THRESHOLD) {
			this.fireUserMoveEvent(velocity);
		}
		
		lastSeen = System.nanoTime();

		return joints;
	}
	
	public double getAge(){
		return (lastSeen - firstSeen) / 1e9;
	}
	
	public boolean isValid() {
		return this.getState() != State.Invalid;
	}
	
	public void addListener(UserListener l) {
		listeners.add(l);
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.fireStateTransitionEvent(this.state, state);
		this.state = state;
	}

	public void fireStateTransitionEvent(State from, State to) {
		for (UserListener l : this.listeners) {
			l.onUserStateTransition(this, from, to);
		}
	}
	
	public boolean isPointing() {
		Point3D rHand = joints.get(Skeleton.HAND_RIGHT);
		Point3D rElbow = joints.get(Skeleton.ELBOW_RIGHT);
		Point3D rShoulder= joints.get(Skeleton.SHOULDER_RIGHT);

		double d = rHand.subtract(rElbow).normalize().distance(rElbow.subtract(rShoulder).normalize());
		
		return d < POINTING_THRESHOLD;
	}
	
	public boolean isPointingAt(RealWorldObject target) {
		if(!isPointing()) return false;

		Point3D rHand = joints.get(Skeleton.HAND_RIGHT);
		Point3D rElbow = joints.get(Skeleton.ELBOW_RIGHT);
		Point3D rShoulder= joints.get(Skeleton.SHOULDER_RIGHT);
		
		double d2 = target.getPosition().subtract(rHand).normalize().distance(rHand.subtract(rShoulder).normalize());
		if(d2 < POINTING_THRESHOLD) {
			return true;
		}
		return false;
	}
	
	public void firePointingEvent(RealWorldObject target) {
		for(UserListener listener : this.listeners) {
			listener.onUserPointing(this, target);
		}
	}

	public void fireUserEnterEvent() {
		for(UserListener listener : this.listeners) {
			listener.onUserEnter(this);
		}
	}
	public void fireUserExitEvent() {
		for(UserListener listener : this.listeners) {
			listener.onUserExit(this);
		}
	}
	public void fireUserMoveEvent(double velocity) {
		for(UserListener listener : this.listeners) {
			listener.onUserMove(this, velocity);
		}
	}
	public void fireUserSuddenMoveEvent(int joint_id, Point3D direction, double velocity) {
		for(UserListener listener : this.listeners) {
			listener.onUserSuddenMove(this, joint_id, direction, velocity);
		}
	}

	public void invalidate() {
		System.out.println("INVALIDATE");
		this.invalidationTime = System.nanoTime();
		this.setState(State.Invalid);
	}
	
	public void revalidate(User u) {
		this.setId(u.getId());
		this.setState(User.State.Calming);
		this.lastSeen = System.nanoTime();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}

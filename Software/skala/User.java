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
	
	public static final double SUDDEN_MOVEMENT_THRESHOLD = 8;
	public static final double MOVEMENT_THRESHOLD = 0.1;
	public static final double POINTING_THRESHOLD = 0.3; 
	public static final double USER_TOUCHING_DISTANCE = 0.1; 
	
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
						System.out.println(velocity);
						this.fireUserSuddenMoveEvent(i, direction, velocity);
					}
				}
			}
			joints.set(i, point.midpoint(previousJoints.get(i)));
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

	public void removeListener(UserListener l) {
		listeners.removeElement(l);
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
		
		Point3D lHand = joints.get(Skeleton.HAND_LEFT);
		Point3D lElbow = joints.get(Skeleton.ELBOW_LEFT);
		Point3D lShoulder= joints.get(Skeleton.SHOULDER_LEFT);

				
		double dR = rHand.subtract(rElbow).normalize().distance(rElbow.subtract(rShoulder).normalize());
		double dL = lHand.subtract(lElbow).normalize().distance(lElbow.subtract(lShoulder).normalize());

		double dry = Math.abs(rHand.getY() - rShoulder.getY());
		double dly = Math.abs(lHand.getY() - lShoulder.getY());
		
		return (dry < 0.5 && dR < POINTING_THRESHOLD) || (dly < 0.5 && dL < POINTING_THRESHOLD);
	}
	
	public boolean isPointingAt(RealWorldObject target) {
		if(!isPointing()) return false;


		Point3D tPos = target.getPosition();
		
		Point3D rHand = joints.get(Skeleton.HAND_RIGHT);
		Point3D rShoulder= joints.get(Skeleton.SHOULDER_RIGHT);
		

		Point3D tPosH = new Point3D(tPos.getX(), 0.0, tPos.getZ());
		Point3D rhPosH = new Point3D(rHand.getX(), 0.0, rHand.getZ());
		Point3D rsPosH = new Point3D(rShoulder.getX(), 0.0, rShoulder.getZ());		
		
		Point3D lHand = joints.get(Skeleton.HAND_LEFT);
		Point3D lShoulder= joints.get(Skeleton.SHOULDER_LEFT);

		Point3D lhPosH = new Point3D(lHand.getX(), 0.0, lHand.getZ());
		Point3D lsPosH = new Point3D(lShoulder.getX(), 0.0, lShoulder.getZ());		

		double d2 = tPosH.subtract(lhPosH).normalize().distance(lhPosH.subtract(lsPosH).normalize());
		if(d2 < POINTING_THRESHOLD && (lHand.getY() - lShoulder.getY() > -0.2)) {
			return true;
		}
		
		double d3 = tPosH.subtract(rhPosH).normalize().distance(rhPosH.subtract(rsPosH).normalize());
		if(d3 < POINTING_THRESHOLD && (rHand.getY() - rShoulder.getY() > -0.2)) {
			return true;
		}
		return false;
	}

	public boolean isTouching(RealWorldObject target) {
		Point3D rHand = joints.get(Skeleton.HAND_RIGHT);
		Point3D lHand = joints.get(Skeleton.HAND_LEFT);
		
		Point3D rhPosH = new Point3D(rHand.getX(), 0.0, rHand.getZ());
		Point3D lhPosH = new Point3D(lHand.getX(), 0.0, lHand.getZ());

		Point3D tPos = target.getPosition();
		Point3D tPosH = new Point3D(tPos.getX(), 0.0, tPos.getZ());
		
		return (rhPosH.distance(tPosH) < USER_TOUCHING_DISTANCE || lhPosH.distance(tPosH) < USER_TOUCHING_DISTANCE);
	}
	
	public void firePointingEvent(RealWorldObject target) {
		for(UserListener listener : this.listeners) {
			listener.onUserPointing(this, target);
		}
	}

	public void fireTouchingEvent(RealWorldObject target) {
		for(UserListener listener : this.listeners) {
			listener.onUserTouch(this, target);
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

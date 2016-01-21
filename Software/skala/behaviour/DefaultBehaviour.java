package skala.behaviour;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;

import edu.ufl.digitalworlds.j4k.Skeleton;
import javafx.geometry.Point3D;
import skala.Ladder;
import skala.Line;
import skala.RealWorldObject;
import skala.Skala;
import skala.User;

public class DefaultBehaviour extends StatedBehaviour<DefaultBehaviour.State> {

	public enum State {
		Normal,
		Disabled
	}
	
	public DefaultBehaviour.State state;  
	
	static final double CALMNESS_VELOCITY_MULTIPLYER = 0.001;
	static final double CALMNESS_VELOCITY_MULTIPLYER_CLIMAXING = 0.025;
	static final double SUDDENMOVE_VELOCITY_MULTIPLYER = 300;
	static final double MAX_CALMNESS = 1.0;
	static final double MIN_CALMNESS = 0.0;
	static final double CALMNESS_INC = 0.002;
	;
	static final double CALMNESS_MULT_CLIMAX = 1.001;

	static final double USER_MATCH_THRESHOLD = 1.0;
	static final double HDIST_THRESHOLD = 0.30;
	static final double AURA_SIZE = 2.0;
	
	static final double AURA_ACTIVATION_SIZE = 0.05;
	

	public DefaultBehaviour(Skala installation, Vector<Ladder> ladders) {
		super(installation, ladders);
		
		for(User u : installation.getUsers()) {
			u.setData("aura", MAX_CALMNESS);
		}
		
		state = DefaultBehaviour.State.Normal;
	}

	public void tick() {
//		super.tick();
//		System.out.println(getUsers().size());
		if(this.getState() != State.Disabled) {
			long now = System.nanoTime();
			Vector<User> toDestroy = new Vector<User>();
			for(User u : installation.getUsers()) {
				if(u.isValid()){
					if((now - u.lastSeen) / 1e9 > USER_INVALIDATE_TIME) {
						u.invalidate();
					}
					if(u.getState() == User.State._Initial && u.getAge() > USER_INVALIDATE_TIME) {
						u.setState(User.State.Calming);
					}
					if(u.getState() == User.State.Calming) {
						u.setData("aura", (Double) u.getData("aura") - CALMNESS_INC);
						u.setData("aura", Math.min((Double) u.getData("aura"), MAX_CALMNESS));
						u.setData("aura", Math.max((Double) u.getData("aura"), MIN_CALMNESS));
						
						Double aura = (Double) (u.getData("aura")) * AURA_SIZE;
						
						for(Ladder l : installation.getLadders()){
							double d = l.getHPosition(1.5).distance(u.getHPosition(1.5));
							if(Math.abs(d - aura) < AURA_ACTIVATION_SIZE){
								l.buzz();
							}
						}
						if((Double) u.getData("aura") == MIN_CALMNESS){
							u.setState(User.State.Climaxing);
							u.setData("aura", 0.01);
						}
					}
					if(u.getState() == User.State.Climaxing) {
						u.setData("aura", (Double) u.getData("aura") * (1.0 + (0.05 * (Double) u.getData("aura"))));
						u.setData("aura", Math.min((Double) u.getData("aura"), MAX_CALMNESS));
						u.setData("aura", Math.max((Double) u.getData("aura"), MIN_CALMNESS));
						
						if((Double) u.getData("aura") == MAX_CALMNESS){
							u.setState(User.State.Calming);
						}
						
						Double aura = (Double) (u.getData("aura")) * AURA_SIZE;
						
						for(Ladder l : installation.getLadders()){
							double d = l.getHPosition(1.5).distance(u.getHPosition(1.5));
							if(Math.abs(d - aura) < AURA_ACTIVATION_SIZE){
								l.cascade();
							}
						}
					}
					
					
					
				} else {
					if((now - u.invalidationTime) / 1e9 > USER_REMOVE_TIME){
						toDestroy.add(u);
					}
				}
			}
			for(User u : toDestroy) {
				installation.removeUser(u);
			}
		}
	}
	
	@Override
	public void onUserEnter(User nU) {
		System.out.println("USER ENTER " + nU.getId());
		
		
		nU.setData("aura", MAX_CALMNESS);
		Color uColor = Color.getHSBColor((float) Math.random(), 1f, 1f);
		nU.setData("color", uColor);
	}

	@Override
	public void onUserExit(User u) {
		System.out.println("USER EXIT " + u.getId());
		u.invalidate();
	}

	@Override
	public void onUserMove(User nU, double velocity) {
		super.onUserMove(nU, velocity);
		if(!nU.isValid()) return;

		double calmness = (Double) nU.getData("aura");
		if(nU.getState() == User.State.Climaxing) {
			calmness -= velocity * CALMNESS_VELOCITY_MULTIPLYER_CLIMAXING;
		} else if(nU.getState() == User.State.Calming){
			calmness += velocity * CALMNESS_VELOCITY_MULTIPLYER;
		}
		nU.setData("aura", calmness);
	}

	@Override
	public void onUserSuddenMove(User u, int joint_id, Point3D direction, double velocity) {
//		System.out.println("USER SUDDEN MOVE " + u.getId());
//		if(!u.isValid()) return;
//		Point3D joint = u.joints.get(joint_id);
//		
//		Point3D target = joint.add(direction.multiply(velocity * SUDDENMOVE_VELOCITY_MULTIPLYER));
//		target.dotProduct(1.0, 0.0, 1.0);
//		for(Ladder l : installation.getLadders()){
//			if(target.subtract(u.getHPosition(1.5)).normalize().distance(l.getHPosition().normalize()) < 0.5){
////				l.buzz();
//			}
//		}
	}

	@Override
	public void onUserPointing(User user, RealWorldObject target) {
//		System.out.println("USER POINTING " + user.getId() + " AT " + target.getType());
//		Line line = new Line(user.joints.get(Skeleton.HAND_RIGHT).getX(), user.joints.get(Skeleton.HAND_RIGHT).getZ(), target.getPosition().getX(), target.getPosition().getZ());
//		installation.lines.add(line);
//		if(target.getType() == "ladder") {
//			Ladder l = (Ladder) target;
//			double y = user.joints.get(Skeleton.HAND_RIGHT).getY();
//			if(user.joints.get(Skeleton.HAND_RIGHT).distance(l.getHPosition(y)) < 0.1) {
//				l.cascade();
//			} else {
//				l.buzz();
//			}
//		}
	}

	@Override
	public void onSkalaStateTransition(Skala skala, Skala.State from, Skala.State to) {
//		if(to == Skala.State.Tired) {
//			this.setState(State.Disabled);
//		}
	}

	@Override
	public void onUserStateTransition(User u, User.State from, User.State to) {
		System.out.println("USER STATE " + (to==User.State.Climaxing ? "CLIMAX" : "CALMING"));
		if(to == User.State.Climaxing) {
			getClosestLadder(u).cascade((byte) 1);
		}
	}
	
	public void onStateTransition(DefaultBehaviour.State from, DefaultBehaviour.State to) {
		
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		for(User u : installation.getUsers()) {
			double aura = (Double) u.getData("aura");
			Point3D uPos = u.getHPosition(2);
			
			draw(g, uPos, Color.ORANGE, aura * AURA_SIZE);
		}
	}

	@Override
	public DefaultBehaviour.State getState() {		
		return this.state;
	}

	@Override
	public void onUserTouch(User user, RealWorldObject target) {
//		if(target.getType() == "ladder") {
//			Ladder l = (Ladder) target;
//			l.cascade();
//		}
//		
	}

}

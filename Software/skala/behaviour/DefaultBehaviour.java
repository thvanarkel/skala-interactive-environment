package skala.behaviour;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;

import edu.ufl.digitalworlds.j4k.Skeleton;
import javafx.geometry.Point3D;
import javafx.scene.shape.Line;
import skala.Ladder;
import skala.RealWorldObject;
import skala.Skala;
import skala.User;

public class DefaultBehaviour extends StatedBehaviour<DefaultBehaviour.State> {

	public enum State {
		Normal,
		Disabled
	};
	
	public DefaultBehaviour.State state;  
	
	final double CALMNESS_VELOCITY_MULTIPLYER = 0.001;
	final double SUDDENMOVE_VELOCITY_MULTIPLYER = 300;
	final double MAX_CALMNESS = 1.0;
	final double MIN_CALMNESS = 0.0;
	final double CALMNESS_INC = 0.003;

	final double USER_MATCH_THRESHOLD = 1.0;
	final double HDIST_THRESHOLD = 0.30;
	
	
	public void tick() {
		super.tick();
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
					}
					u.setData("aura", Math.min((Double) u.getData("aura"), MAX_CALMNESS));
					u.setData("aura", Math.max((Double) u.getData("aura"), MIN_CALMNESS));
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
	
	public DefaultBehaviour(Skala installation, Vector<Ladder> ladders) {
		super(installation, ladders);
		state = DefaultBehaviour.State.Normal;
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
		Point3D hpos2 = nU.joints.get(Skeleton.HEAD);
		System.out.println("USER MOVE " + hpos2);
		
		if(nU.getState() == User.State._Initial){
			boolean destroy = false;
			for(User u : installation.getUsers()) {
				double dist = nU.getPosition().distance(u.getPosition());
				Point3D hpos1 = u.joints.get(Skeleton.HEAD);
//				Point3D hpos2 = nU.joints.get(Skeleton.HEAD);
				
				double hDist = Math.abs(hpos1.getY() - hpos2.getY());
				
				if(!u.isValid()) {
					if(dist <= USER_MATCH_THRESHOLD && hDist < HDIST_THRESHOLD) {			
						installation.setStatus("REPLACE " + u.getId() + " WITH " + nU.getId());
						u.revalidate(nU);
						destroy = true;
					}
				}
			}
			if(destroy){
				installation.removeUser(nU);
			} 
		}
		if(!nU.isValid()) return;

		double calmness = (Double) nU.getData("aura");
		calmness += velocity * CALMNESS_VELOCITY_MULTIPLYER;
		nU.setData("aura", calmness);
	}

	@Override
	public void onUserSuddenMove(User u, int joint_id, Point3D direction, double velocity) {
//		System.out.println("USER SUDDEN MOVE " + u.getId());
		if(!u.isValid()) return;
		Point3D joint = u.joints.get(joint_id);
		
		Point3D target = joint.add(direction.multiply(velocity * SUDDENMOVE_VELOCITY_MULTIPLYER));
		
		Line l = new Line(joint.getX(), joint.getZ(), target.getX(), target.getZ());

		u.setData("aura", (Double) 1.0);
	}

	@Override
	public void onUserPointing(User user, RealWorldObject target) {
		System.out.println("USER POINTING " + user.getId() + " AT " + target.getType());
		Line line = new Line(user.joints.get(Skeleton.HAND_RIGHT).getX(), user.joints.get(Skeleton.HAND_RIGHT).getZ(), target.getPosition().getX(), target.getPosition().getZ());
		installation.lines.add(line);
		if(target.getType() == "ladder") {
			Ladder l = (Ladder) target;
			l.buzz();
		}
	}

	@Override
	public void onSkalaStateTransition(Skala skala, Skala.State from, Skala.State to) {
//		if(to == Skala.State.Tired) {
//			this.setState(State.Disabled);
//		}
	}

	@Override
	public void onUserStateTransition(User u, User.State from, User.State to) {
//		if(to == User.State.Climaxing) {
//			getClosestLadder(u).cascade();
//		}
	}
	
	public void onStateTransition(DefaultBehaviour.State from, DefaultBehaviour.State to) {
		
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		for(User u : installation.getUsers()) {
			double aura = (Double) u.getData("aura");
			installation.draw(g, u.getPosition(), Color.ORANGE, aura * 3.0);
		}
		
	}

	@Override
	public DefaultBehaviour.State getState() {		
		return this.state;
	}

}

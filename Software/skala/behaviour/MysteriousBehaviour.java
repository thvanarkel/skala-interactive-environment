package skala.behaviour;

import java.awt.Color;
import java.util.Vector;

import javafx.geometry.Point3D;
import skala.Ladder;
import skala.RealWorldObject;
import skala.Skala;
import skala.User;
import skala.User.State;

public class MysteriousBehaviour extends Behaviour {
	
	public MysteriousBehaviour(Skala installation) {
		super(installation);
	}
	
	@Override
	public void tick(){
		super.tick();
		long now = System.nanoTime();
		Vector<User> toDestroy = new Vector<User>();
		for(User u : getInstallation().getUsers()) {
			if(u.isValid()){
				if((now - u.lastSeen) / 1e9 > USER_INVALIDATE_TIME) {
					u.invalidate();
				}
				if(u.getState() == User.State._Initial && u.getAge() > USER_INVALIDATE_TIME) {
					u.setState(User.State.Calming);
				}
			} else {
				if((now - u.invalidationTime) / 1e9 > USER_REMOVE_TIME){
					toDestroy.add(u);
				}
			}
		}
		for(User u : toDestroy) {
			getInstallation().removeUser(u);
		}
		
		for(User u : getInstallation().getUsers()){
			if(u.getAge() > 2.0 && u.isValid()) {
				Ladder l = getFarthestLadder(u);
				System.out.println(l);
				l.cascade();
			}
		}
	}
	

	@Override
	public void onUserEnter(User nU) {
//		System.out.println("USER ENTER " + nU.getId());
		
		Color uColor = Color.getHSBColor((float) Math.random(), 1f, 1f);
		nU.setData("color", uColor);
	}

	@Override
	public void onUserExit(User u) {
		u.invalidate();
	}

	@Override
	public void onUserMove(User nU, double velocity) {
		super.onUserMove(nU, velocity);
		if(!nU.isValid()) return;

	}
	
	@Override
	public void onUserSuddenMove(User u, int joint_id, Point3D direction, double velocity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserPointing(User user, RealWorldObject target) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserTouch(User user, RealWorldObject target) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSkalaStateTransition(Skala skala, skala.Skala.State from, skala.Skala.State to) {
		// TODO Auto-generated method stub

	}
//
//	@Override
//	public void onUserStateTransition() {
//		// TODO Auto-generated method stub
//
//	}

	@Override
	public void onUserStateTransition(User user, State from, State to) {
		// TODO Auto-generated method stub
		
	}

}

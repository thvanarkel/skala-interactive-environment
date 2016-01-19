package skala.behaviour;

import java.awt.Graphics;
import java.util.Vector;

import javafx.geometry.Point3D;
import skala.Ladder;
import skala.RealWorldObject;
import skala.Skala;
import skala.Stated;
import skala.User;

public abstract class StatedBehaviour<S> extends Behaviour implements Stated<S> {

	public enum State {
		Normal,
		Disabled
	}
	
	public S state;
	
	public StatedBehaviour(Skala installation) {
		super(installation);
	}

	public StatedBehaviour(Skala installation, Vector<Ladder> ladders) {
		super(installation, ladders);
	}

	@Override
	public void onUserEnter(User u) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserExit(User u) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserMove(User u, double distance) {
		// TODO Auto-generated method stub

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
	public void onUserStateTransition(User user, User.State from, User.State to) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSkalaStateTransition(Skala skala, Skala.State from,
			Skala.State to) {
		// TODO Auto-generated method stub

	}

	
	
	@Override
	public void tick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setState(S state) {
		fireStateTransitionEvent(this.state, state);
		this.state = state;
	}

	@Override
	public void fireStateTransitionEvent(S from, S to) {
		this.onStateTransition(from, to);
	}

	private void onStateTransition(S from, S to) {
		// TODO Auto-generated method stub
	}

}

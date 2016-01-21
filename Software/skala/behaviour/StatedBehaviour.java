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

package skala.behaviour;

import java.awt.Graphics;
import java.util.Vector;

import javafx.geometry.Point3D;
import skala.Ladder;
import skala.RealWorldObject;
import skala.Skala;
import skala.Stated;
import skala.StatedListener;
import skala.User;

public abstract class StatedBehaviour<S> extends Behaviour implements Stated<S> {


	protected Vector<StatedListener<S>> listeners;
	
	public void addListener(StatedListener<S> listener) {
		listeners.add(listener);
	}
	
	public enum State {
		Normal,
		Disabled
	}
	
	public S state;
	
	public StatedBehaviour(Skala installation) {
		super(installation);
		this.listeners = new Vector<StatedListener<S>>();
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
		for(StatedListener<S> l : listeners) {
			l.onStateTransitionEvent(this, from, to);
		}
	}
}

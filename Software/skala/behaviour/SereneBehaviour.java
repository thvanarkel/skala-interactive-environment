package skala.behaviour;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javafx.geometry.Point3D;
import skala.RealWorldObject;
import skala.Skala;
import skala.StatedListener;
import skala.User;
import skala.User.State;

public class SereneBehaviour extends StatedBehaviour<SereneBehaviour.State> implements StatedListener<AutomaticBehaviour.State>{

	private Timer timer;
	
	public enum State {
		Idle,
		Sweep
	}
	
	
	public SereneBehaviour(Skala installation) {
		super(installation);
		setState(State.Idle);
		this.timer = new Timer();
		this.run();
	}

	@Override
	public void tick() {
		super.tick();
	}

	public void scheduleRun(int delay) {
		SereneBehaviour beh = this;
		this.timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				beh.run();
			}
		}, delay*1000);
	}

	public void scheduleRun() {
		scheduleRun(60);
	}
	
	public void run() {
		double numStates = State.values().length;
		int rIndex = (int) Math.floor(Math.random() * numStates);
		System.out.println("Picked " + rIndex);
		setState(State.values()[rIndex]);
	}
	
	@Override
	public void onUserEnter(User nU) {
		super.tick();
		this.timer.cancel();
		getInstallation().setBehaviour(new DefaultBehaviour(getInstallation()));
	}

	
	@Override
	public void onUserSuddenMove(User u, int joint_id, Point3D direction, double velocity) {
		
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

	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return this.state;
	}

	@Override
	public void fireStateTransitionEvent(State from, State to) {
		// TODO Auto-generated method stub
		
		AutomaticBehaviour behaviour;
		switch(to) {
			case Sweep: 
				behaviour = new SweepBehaviour(getInstallation());
				break;
			default:
				behaviour = new SweepBehaviour(getInstallation());
				break;
		}
		
		getInstallation().setBehaviour(behaviour);
		behaviour.addListener(this);
				
	}

	
	@Override
	public void onUserStateTransition(User user, skala.User.State from, skala.User.State to) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStateTransitionEvent(Object origin, skala.behaviour.AutomaticBehaviour.State from,
			skala.behaviour.AutomaticBehaviour.State to) {
		if(to == AutomaticBehaviour.State.Finished) {
			setState(State.Idle);
		}
		
	}

}

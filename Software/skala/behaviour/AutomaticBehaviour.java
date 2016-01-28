package skala.behaviour;

import java.util.Timer;
import java.util.TimerTask;

import javafx.geometry.Point3D;
import skala.RealWorldObject;
import skala.Skala;
import skala.User;
import skala.User.State;

public abstract class AutomaticBehaviour extends StatedBehaviour<AutomaticBehaviour.State>{
	
	Timer timer;
	
	public enum State {
		Idle,
		Running,
		Finished
	};
	
	public AutomaticBehaviour(Skala installation) {
		super(installation);

		this.timer = new Timer();
		
		AutomaticBehaviour ab = this;
		
		timer.schedule( new TimerTask() {
		    public void run() {
		       ab.tick();
		    }
		 }, 0, 200);

	}
	
	public boolean isFinished() {
		return getState() == State.Finished;
	}
	
	public boolean isRunning(){
		return getState() == State.Running;
	}
	
	public void start() {
		if(getState() == State.Idle){
			setState(State.Running);
		}
	}
	
	public void stop() {
		setState(State.Finished);
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
	public void onUserStateTransition(User user, skala.User.State from, skala.User.State to) {
		
	}

	@Override
	public void onSkalaStateTransition(Skala skala, skala.Skala.State from, skala.Skala.State to) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void fireStateTransitionEvent(State from, State to) {
		super.fireStateTransitionEvent(from, to);
		if(to == State.Finished) {
			timer.cancel();
			getInstallation().setBehaviour(new DefaultBehaviour(getInstallation()));
		}
	}

}

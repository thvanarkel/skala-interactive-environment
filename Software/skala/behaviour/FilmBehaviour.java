package skala.behaviour;

import javafx.geometry.Point3D;
import skala.RealWorldObject;
import skala.Skala;
import skala.User;
import skala.User.State;

public class FilmBehaviour extends Behaviour {

	
	public FilmBehaviour(Skala installation) {
		super(installation);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void tick() {
		
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
	public void onUserStateTransition(User user, State from, State to) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSkalaStateTransition(Skala skala, skala.Skala.State from, skala.Skala.State to) {
		// TODO Auto-generated method stub

	}

}

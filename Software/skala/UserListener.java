package skala;

import javafx.geometry.Point3D;

public interface UserListener {
	public void onUserEnter(User u);
	public void onUserExit(User u);

	public void onUserMove(User u, double distance);

	public void onUserSuddenMove(User u, int joint_id, Point3D direction, double velocity);

	public void onUserPointing(User user, RealWorldObject target);
	public void onUserTouch(User user, RealWorldObject target);
	
	public void onUserStateTransition(User user, User.State from, User.State to);
}

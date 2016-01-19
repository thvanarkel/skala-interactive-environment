package skala.behaviour;

import java.awt.Graphics;
import java.util.Vector;

import edu.ufl.digitalworlds.j4k.Skeleton;
import javafx.geometry.Point3D;
import skala.Ladder;
import skala.RealWorldObject;
import skala.Skala;
import skala.User;
import skala.Skala.State;

public class CalibrationBehaviour extends Behaviour {

	private Vector<Ladder> calibrationQueue;

	private boolean waitingForPosition = false;
	
	private Ladder calibratingLadder;
	private User user;
	
	public CalibrationBehaviour(Skala installation, Vector<Ladder> ladders, User u) {
		super(installation);
		
		this.user = u;
		
		
		calibrationQueue = new Vector<Ladder>(ladders);
		ladders = new Vector<Ladder>();
		
		calibratingLadder = calibrationQueue.firstElement();
		
		installation.isCalibrated = false;
		installation.isCalibrating = true;
		
		this.waitingForPosition = true;
	}
	
	public void tick(){
		
		
		Point3D rHandPos = user.joints.get(Skeleton.HAND_RIGHT);
		Point3D lHandPos = user.joints.get(Skeleton.HAND_LEFT);
		Point3D headPos  = user.joints.get(Skeleton.HEAD);
		
		installation.setStatus("Calibrating " + calibratingLadder.getId() + ": " + rHandPos);		

		if(!waitingForPosition){
			if(lHandPos.getY() < rHandPos.getY()){
				calibrationQueue.remove(calibratingLadder);
				if(!calibrationQueue.isEmpty()){
					waitingForPosition = true;
					calibratingLadder = calibrationQueue.firstElement();
					
					installation.setStatus("Calibrating " + calibratingLadder.getId());
				} else {
					installation.setIsCalibrated(true);
					installation.setBehaviour(new DefaultBehaviour(installation, installation.getLadders()));
				}
			}
		}
		if(lHandPos.getY() > headPos.getY()){
			waitingForPosition = false;
			calibratingLadder.position = rHandPos;
		} else {
			calibratingLadder.buzz();
		}
	}

	@Override
	public void onUserEnter(User u) {
	}

	@Override
	public void onUserExit(User u) {
		if(u.getId() == user.getId()) {
			installation.setStatus("Calibration cancelled");
			installation.setBehaviour(new DefaultBehaviour(installation, installation.getLadders()));
		}
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
	public void onSkalaStateTransition(Skala skala, State from, State to) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUserStateTransition(User user, skala.User.State from, skala.User.State to) {
		// TODO Auto-generated method stub
		
	}

}

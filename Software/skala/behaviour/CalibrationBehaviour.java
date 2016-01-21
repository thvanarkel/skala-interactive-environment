package skala.behaviour;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;

import edu.ufl.digitalworlds.j4k.Skeleton;
import javafx.geometry.Point3D;
import skala.Ladder;
import skala.RealWorldObject;
import skala.Skala;
import skala.Skala.State;
import skala.User;

public class CalibrationBehaviour extends Behaviour {

	private Vector<Ladder> calibrationQueue;

	private boolean running;
	
	private Ladder calibratingLadder;
	private User user;
	
	public CalibrationBehaviour(Skala installation, Vector<Ladder> ladders) {
		super(installation);
		
		if(installation.getUsers().size() > 0) {
			this.user = installation.getUsers().firstElement();
			this.running = true;
		} else {
			this.running = false;
		}
		
		setCalibrationQueue(new Vector<Ladder>(ladders));
		ladders = new Vector<Ladder>();
		
		calibratingLadder = getCalibrationQueue().firstElement();
		
		installation.isCalibrated = false;
		installation.isCalibrating = true;
		
	}
	
	public void tick(){
		super.tick();
		
		if(!user.isValid()) {
			user = installation.getUsers().firstElement();
		}
		
		if(running) {
			Point3D rFootPos = user.joints.get(Skeleton.HAND_RIGHT);
			Point3D lFootPos = user.joints.get(Skeleton.HAND_LEFT);
			
			Point3D rHandPos = user.joints.get(Skeleton.HAND_RIGHT);
			Point3D lHandPos = user.joints.get(Skeleton.HAND_LEFT);
			
			if(rHandPos.getY() > lHandPos.getY()) {
				calibratingLadder.position = rHandPos;
			} else {
				calibratingLadder.position = lHandPos;
			}	
		}
	}

	@Override
	public void onUserEnter(User u) {
		super.onUserEnter(u);
		if(installation.getUsers().size() == 1) {
			this.user = u;
			installation.setStatus("Calibration continued");
			this.running = true;
		}
	}

	@Override
	public void onUserExit(User u) {
		super.onUserExit(u);
		installation.removeUser(u);
		if(installation.getUsers().size() == 0) {
			installation.setStatus("Calibration paused");
			this.running = false;
		} else {
			if(u.getId() == user.getId()) {
				user = installation.getUsers().firstElement();
			}
		}
	}

	@Override
	public void onUserMove(User nU, double velocity) {
		super.onUserMove(nU, velocity);
	}

	@Override
	public void onUserSuddenMove(User u, int joint_id, Point3D direction, double velocity) {
//		super.onUserSuddenMove(u, joint_id, direction, velocity);
	}

	@Override
	public void onUserPointing(User user, RealWorldObject target) {
//		super.onUserPointing(user, target);
		

	}

	@Override
	public void onSkalaStateTransition(Skala skala, State from, State to) {
//		super.onSkalaStateTransition(skala, from, to);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		draw(g, calibratingLadder.getPosition(), Color.CYAN, 0.1);
	}

	@Override
	public void onUserStateTransition(User user, skala.User.State from, skala.User.State to) {
		// TODO Auto-generated method stub
		
	}

	public void capture() {
		installation.getLadders().add(calibratingLadder);
		getCalibrationQueue().remove(calibratingLadder);
		if(!getCalibrationQueue().isEmpty()){
			calibratingLadder = getCalibrationQueue().firstElement();
			calibratingLadder.buzz();
		} else {
			installation.setIsCalibrated(true);
			installation.setBehaviour(new DefaultBehaviour(installation, installation.getLadders()));
		}
		
	}

	public Vector<Ladder> getCalibrationQueue() {
		return calibrationQueue;
	}

	public void setCalibrationQueue(Vector<Ladder> calibrationQueue) {
		this.calibrationQueue = calibrationQueue;
	}

	@Override
	public void onUserTouch(User user, RealWorldObject target) {
		// TODO Auto-generated method stub
		
	}

}

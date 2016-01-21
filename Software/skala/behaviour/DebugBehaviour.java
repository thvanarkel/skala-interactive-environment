package skala.behaviour;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.Vector;

import edu.ufl.digitalworlds.j4k.Skeleton;
import javafx.geometry.Point3D;
import skala.Arduino;
import skala.Ladder;
import skala.RealWorldObject;
import skala.Skala;
import skala.User;
import skala.User.State;

public class DebugBehaviour extends Behaviour {

	private byte ladderId;
	private boolean dragging;
	private User dU;
	
	public DebugBehaviour(Skala installation) {
		super(installation);
		ladderId = 0;
		dragging = false;
		dU = new User(1337);
		dU.setPosition(new Point3D(2.5,1.5,2.5));
		installation.addUser(dU);
	}

	@Override
	public void onUserEnter(User u) {
		// TODO Auto-generated method stub
//		super.onUserEnter(u);
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
	public void onUserStateTransition(User user, State from, State to) {
		// TODO Auto-generated method stub
//		super.onUserStateTransition(user, from, to);
	}

	@Override
	public void onSkalaStateTransition(Skala skala, skala.Skala.State from, skala.Skala.State to) {
		// TODO Auto-generated method stub

	}

	public void onRelease(Point3D p) {
		dragging = false;
	}

	public void onPress(Point3D p) {
		dragging = true;
		dU.setPosition(p);
	}

	public void onDrag(Point3D p) {
		dU.setPosition(p);
		dU.setData("aura", ((Double) dU.getData("aura")) + 0.01);
	}
	
	public void onExit(Point3D p) {
		// TODO Auto-generated method stub
		
	}

	public void onEnter(Point3D p) {
		// TODO Auto-generated method stub
		
	}

	private boolean testForLadder(Point3D p) {
		double d = Double.POSITIVE_INFINITY;
		for(Ladder l : installation.getLadders()) {
			double ld = l.getHPosition(1.5).distance(p);
			if(ld < d){
				d = ld;
			}
		}
		return d < 0.15;
	}
	
	private Ladder getClosestLadder(Point3D p) {
		Ladder c = null;
		double d = Double.POSITIVE_INFINITY;
		for(Ladder l : installation.getLadders()) {
			
			double ld = l.getHPosition(1.5).distance(p);
			if(ld < d){
				c = l;
				d = ld;
			}
		}
		return c;
	}
	
	public void onClick(Point3D p) {
		System.out.println("LCLICK");
		if(testForLadder(p)){
			System.out.println("HIT");
			Ladder c = getClosestLadder(p);
			c.buzz();
		}
	}

	public void onRightClick(Point3D p) {
		System.out.println("RCLICK");		
		if(!installation.isCalibrated){
			System.out.println(installation.getArduinos().get(Math.floorDiv(ladderId,5)));
			Ladder c = new Ladder((byte) (ladderId % 5), installation.getArduinos().get(Math.floorDiv(ladderId++,5)));
			c.setPosition(p);
			installation.getLadders().add(c);
			if(ladderId == 10){
				installation.isCalibrated = true;
			}
		} else {
			dU.joints.set(Skeleton.SPINE_MID, p);
			dU.setData("aura", 1.0);
			
			if(testForLadder(p)){
				System.out.println("HIT");
				installation.startCalibration();
				CalibrationBehaviour cb = (CalibrationBehaviour) installation.getBehaviour();
				Vector<Ladder> q = new Vector<Ladder>();
				q.add(getClosestLadder(p));
				cb.setCalibrationQueue(q);
			}
			
		}
	}

	public void onMiddleClick(Point3D p) {
		System.out.println("MCLICK");
		if(testForLadder(p)){
			System.out.println("HIT");
			Ladder c = getClosestLadder(p);
			c.cascade((byte)1);
		}
	}
	
	public void tick(){
		super.tick();
		dU.lastSeen = System.nanoTime();
	}
	
	public void paint(Graphics g){
		super.paint(g);
	}

	public void onMove(Point3D p) {
		
	}

	@Override
	public void onUserTouch(User user, RealWorldObject target) {
		// TODO Auto-generated method stub
		
	}

}

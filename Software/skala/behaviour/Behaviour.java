package skala.behaviour;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;

import edu.ufl.digitalworlds.j4k.Skeleton;
import javafx.geometry.Point3D;
import skala.Ladder;
import skala.RealWorldObject;
import skala.Sensor;
import skala.Skala;
import skala.SkalaListener;
import skala.User;
import skala.UserListener;

public abstract class Behaviour implements UserListener, SkalaListener{
	
	private Vector<User> users;
	Vector<Ladder> ladders;
	
	Skala installation;

	final double USER_INVALIDATE_TIME = 0.5;
	final double USER_REMOVE_TIME = 3.0;

	final double USER_MATCH_THRESHOLD = 1.0;
	final double HDIST_THRESHOLD = 0.30;
	
	public Behaviour(Skala installation) {
		this.installation = installation;

		this.setUsers(new Vector<User>());
		this.ladders = new Vector<Ladder>();
	}
	
	public Behaviour(Skala installation, Vector<Ladder> ladders) {
		this.ladders = ladders;
		this.installation = installation;
	}

	public void draw(Graphics g, Point3D pos, Color color, double size) {

		g.setColor(color);
		
		int width = installation.getWidth();
		int height = installation.getHeight();
		
		double z = pos.getY();
		
		int xsize = (int) Math.round((width / 5.0) * z * size);
		int ysize = (int) Math.round((height / 5.0) * z * size);
		
		int x = (int) Math.round(((width / 5.0) * pos.getX()) - (xsize / Math.sqrt(4)));
		int y = (int) Math.round(((height / 5.0) * pos.getZ()) - (ysize / Math.sqrt(4)));
		
		g.drawOval(x,y, xsize, ysize);
	}
	
	public void drawLine(Graphics g, Point3D from, Point3D to, Color color) {

		g.setColor(color);
		
		int width = installation.getWidth();
		int height = installation.getHeight();
		
		int x1 = (int) Math.round(((width / 5.0) * (from.getX())));
		int y1 = (int) Math.round(((height / 5.0) * (from.getZ())));
		
		int x2 = (int) Math.round(((width / 5.0) * (to.getX())));
		int y2 = (int) Math.round(((height / 5.0) * (to.getZ())));
		
		
		
		g.drawLine(x1, y1, x2, y2);
	}
	
	public void tick(){};
	public void paint(Graphics g) {

		int width = installation.getWidth();
		int height = installation.getHeight();
		
		if(!getUsers().isEmpty()){
			Vector<User> invalids = new Vector<User>();
			for(User u : getUsers()){
				Color color = new Color(255, 255, 255);
				if(u.isValid()){
					color = (Color) u.getData("color");
				}
			
				Point3D hPos= u.joints.get(Skeleton.HEAD);

				Point3D nPos= u.joints.get(Skeleton.NECK);

				Point3D lsPos= u.joints.get(Skeleton.SHOULDER_LEFT);
				Point3D rsPos= u.joints.get(Skeleton.SHOULDER_RIGHT);
				
				Point3D lePos= u.joints.get(Skeleton.ELBOW_LEFT);
				Point3D rePos= u.joints.get(Skeleton.ELBOW_RIGHT);
				
				Point3D lhPos= u.joints.get(Skeleton.HAND_LEFT);
				Point3D rhPos= u.joints.get(Skeleton.HAND_RIGHT);
				
				Point3D smPos= u.joints.get(Skeleton.SPINE_MID);
				Point3D sbPos= u.joints.get(Skeleton.SPINE_BASE);
				
				int x = (int) Math.round(((width / 5.0) * hPos.getX()));
				int y = (int) Math.round(((height / 5.0) * hPos.getZ()));
				
				g.setColor(Color.white);
				
				Vector<String> label = new Vector<String>();
				

				label.add("height: " + hPos.getY());
				label.add("lheight: " + lhPos.getY());
				label.add("rheight: " + rhPos.getY());
				label.add(u.getState() == User.State.Calming ? "Calming" : "NOTCALMING");
				label.add("Age: " + u.getAge());
				
				int r = 0;
				for(String s : label) {
					g.drawChars(s.toCharArray(), 0, s.length(), x + 20, y + 20 * r++);
				}
				
				Point3D dhPos = hPos.add(0.0, 0.2, 0.0);
				
				drawLine(g, hPos, nPos, color);
				
				drawLine(g, nPos, lsPos, color);
				drawLine(g, nPos, rsPos, color);

				drawLine(g, lsPos, lePos, color);
				drawLine(g, rsPos, rePos, color);

				drawLine(g, lePos, lhPos, color);
				drawLine(g, rePos, rhPos, color);

				drawLine(g, nPos, smPos, color);
				drawLine(g, smPos, sbPos, color);

				
				draw(g, dhPos, color , 0.15);

				draw(g, lhPos, color, 0.10);

				draw(g, rhPos, color, 0.10);

//				}
			}
		}
		
		for(Sensor s : installation.getSensors()) {
			draw(g, s.getPosition(), Color.RED, 0.05);
			draw(g, s.getPosition(), Color.RED, 0.10);
			draw(g, s.getPosition(), Color.RED, 0.15);
		}
//		
//		draw(g, kinect2.getPosition(), Color.RED, 0.05);
//		draw(g, kinect2.getPosition(), Color.RED, 0.10);
//		draw(g, kinect2.getPosition(), Color.RED, 0.15);
		
		for(Ladder l: installation.getLadders()){
			Point3D position = l.getHPosition(1.5);
			draw(g, position, Color.GREEN, .1);
		}	
	};
	
	public Ladder getClosestLadder(RealWorldObject from) {
		Ladder candidate = null;
		Double minDistance = Double.POSITIVE_INFINITY;
		
		Point3D origin = from.getPosition();
		
		for(Ladder l : ladders) {
			double dist = origin.distance(l.getPosition());
			if(dist < minDistance) {
				candidate = l;
				minDistance = dist;
			}
		}
		return candidate;
	}

	public Vector<User> getUsers() {
		return installation.getUsers();
	}

	public void setUsers(Vector<User> users) {
		installation.setUsers(users);
	}	

	public void onUserEnter(User u) {

	}

	public void onUserExit(User u) {
		
	}
	
	
	public void onUserMove(User nU, double velocity) {
		Point3D hpos2 = nU.joints.get(Skeleton.HEAD);
//		System.out.println("USER MOVE " + hpos2);
		
		if(nU.getState() == User.State._Initial){
			boolean destroy = false;
			for(User u : installation.getUsers()) {
				double dist = nU.getPosition().distance(u.getPosition());
				Point3D hpos1 = u.joints.get(Skeleton.HEAD);
				
				double hDist = Math.abs(hpos1.getY() - hpos2.getY());
				
				if(!u.isValid()) {
					if(dist <= USER_MATCH_THRESHOLD && hDist < HDIST_THRESHOLD) {			
						installation.setStatus("REPLACE " + u.getId() + " WITH " + nU.getId());
						u.revalidate(nU);
						destroy = true;
					}
				}
			}
			if(destroy){
				installation.removeUser(nU);
			} 
		}

	}
	
}

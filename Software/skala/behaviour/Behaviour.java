package skala.behaviour;

import java.awt.Graphics;
import java.util.Vector;

import javafx.geometry.Point3D;
import skala.Ladder;
import skala.RealWorldObject;
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
	
	public Behaviour(Skala installation) {
		this.installation = installation;

		this.setUsers(new Vector<User>());
		this.ladders = new Vector<Ladder>();
	}
	
	public Behaviour(Skala installation, Vector<Ladder> ladders) {
		this.ladders = ladders;
		this.installation = installation;
	}

	public abstract void tick();
	public abstract void paint(Graphics g);
	
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
		return this.users;
	}

	public void setUsers(Vector<User> users) {
		this.users = users;
	}	

}

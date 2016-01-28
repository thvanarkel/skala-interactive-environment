package skala.behaviour;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Timer;

import javafx.geometry.Point3D;
import skala.Ladder;
import skala.RealWorldObject;
import skala.Skala;
import skala.User;

public class SweepBehaviour extends AutomaticBehaviour {

	double sweepPosition;
	long _timestamp;
	
	public SweepBehaviour(Skala installation) {
		super(installation);
		
		this.sweepPosition = 0.0;
	}

	public void tick(){
		super.tick();
		
		
		_timestamp = System.nanoTime();
		
		this.sweepPosition += 0.01;
		
		System.out.println(sweepPosition);
		
		for(Ladder l : getInstallation().getLadders()){
			if(Math.abs(l.getPosition().getX() - sweepPosition) <= 0.01) {
				l.cascade();
			}
		}
		
		if(this.sweepPosition > 5.9){
			this.setState(State.Finished);
			this.timer.cancel();
		}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		super.drawLine(g, new Point3D(this.sweepPosition, 1.5, 0.0), new Point3D(this.sweepPosition, 1.5, 6.0), Color.white);
	}
	
	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return state;
	}
}

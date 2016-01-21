package skala;

import java.util.Vector;

public class Ladder extends SkalaObject implements Stated<Ladder.State> {	

	static String type = "ladder";
	
	private Vector<StatedListener> listeners;
	
	public enum MovementType
	{
	  Buzz, Tease, Cascade
	};

	public enum State
	{
	  Idle, Buzzing, Swinging, Cascading 
	};

	private byte id;
	
	private State state;
	Arduino arduino;

	public Ladder(byte id, Arduino arduino) {
		this.setId(id);
		this.arduino = arduino;
	}
	
	public void buzz(){
		setState(State.Buzzing);
//		System.out.println("BUZZ");
		this.arduino.sendBuzz(this.getId(), (byte)90);
	};

	public void swing(){
//		setState(State.Swinging);
	};

	public void cascade(){
		setState(State.Cascading);
		this.arduino.sendCascade(this.getId(), (byte)90);
	};
	
	public void stop(){
//		setState(State.Idle);
	};
	
	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.fireStateTransitionEvent(this.state, state);
		this.state = state;
		
	}
	
	public String getType() {
		return "ladder";
	}

	@Override
	public void fireStateTransitionEvent(State from, State to) {
		
	}

	public byte getId() {
		return id;
	}

	public void setId(byte id) {
		this.id = id;
	}
}

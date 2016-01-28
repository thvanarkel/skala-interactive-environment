package skala;

import java.util.Vector;

public class Ladder extends SkalaObject implements Stated<Ladder.State> {	

	static String type = "ladder";
	public long lockTime;

	public boolean busy;
	
	private Vector<LadderListener> listeners;
	
	
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
		this.listeners = new Vector<LadderListener>();
	}
	
	public void buzz(){
		setState(State.Buzzing);
		System.out.println("BUZZ");
		this.arduino.sendBuzz(this.getId(), (byte)90);
	};

	public void swing(){
//		setState(State.Swinging);
	};


	public void cascade(){
		cascade((byte) 90);
	};
	
	public void cascade(byte speed){
		if(System.nanoTime() < this.lockTime) {
			return;
		}
		this.lockTime = System.nanoTime() + (long) 3e9;
		setState(State.Cascading);
		System.out.println("cascading");
		this.arduino.sendCascade(this.getId(), speed);
	};
	
	public void stop(){
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
		for(LadderListener sl : listeners){
			sl.onLadderStateTransitionEvent(this, from, to);
		}
	}

	public byte getId() {
		return id;
	}

	public void setId(byte id) {
		this.id = id;
	}
}

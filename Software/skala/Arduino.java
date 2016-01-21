package skala;

import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;

public class Arduino implements jssc.SerialPortEventListener {
	private Vector<Ladder> ladders;
	
	public String address;

	private String serialBuffer = "";
	
	private Vector<Skala> listeners;
	public SerialPort serialPort; 

	static private Pattern msgPattern = Pattern.compile("^\n*.*;\n$");
	static private Pattern newLadderPattern = Pattern.compile("L(?<id>[0-9]+)");
	static private Pattern readyPattern = Pattern.compile("^R$");
	
	private HashMap<Pattern, String> msgPatterns;

	boolean isReady = false;
	
	public Arduino(String address) {
		this.address = address;
		this.listeners = new Vector<Skala>();
		this.setLadders(new Vector<Ladder>());
		this.msgPatterns = new HashMap<Pattern, String>();

		this.msgPatterns.put(newLadderPattern, "addLadder");
		this.msgPatterns.put(readyPattern, "ready");
	}
	

	public void initialize(){
		serialPort = new SerialPort(address);
	    try {
	        serialPort.openPort();
	        serialPort.setParams(SerialPort.BAUDRATE_9600, 
	                SerialPort.DATABITS_8,
	                SerialPort.STOPBITS_1,
	                SerialPort.PARITY_NONE);
	        serialPort.setEventsMask(2* SerialPort.MASK_RING -1);
	        serialPort.addEventListener(this);
	    }
	    catch (SerialPortException ex) {
	        System.err.println(ex);
	    }
	}

	
	public void buzz(int id) {
		if(hasLadder(id)){
			buzz(getLadder(id));
		}
	}
	
	public void buzz(Ladder l){
		try {
			serialPort.writeString("b;");
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
	
	public void sendBuzz(byte address, byte velocity) {
		byte[] message = {address, 0, velocity};
//		System.out.println("SENDBUZZ " + message[0] + message[1] + message[2]);
		try {
			this.serialPort.writeBytes(message);
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendCascade(byte address, byte velocity) {
		byte[] message = {address, 2, velocity};
		try {
			this.serialPort.writeBytes(message);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasLadder(int id) {
		for(Ladder l : getLadders()) {
			if(l.getId() == id) return true;
		}
		return false;
	}
	
	public boolean hasLadder(Ladder l) {
		return hasLadder(l.getId());
	}
	
	public Ladder getLadder(int id) {
		for(Ladder l : getLadders()) {
			if(l.getId() == id) return l;
		}
		return null;
	}

	public Ladder getLadder(Ladder l) {
		return getLadder(l.getId());
	}
	
	public void addEventListener(Skala listener) {
		listeners.addElement(listener);
	}
	
	private void parseMessage(String msg) {
		msg = msg.trim();		
		System.out.println("ARDUINO::Parsemessage " + msg);
		for(Pattern pattern : this.msgPatterns.keySet()) {
			Matcher matcher = pattern.matcher(msg);
			if(matcher.matches()){
				String action = this.msgPatterns.get(matcher.pattern());
				this.doAction(action, matcher);
			}
		}
	}
	
	public Ladder addLadder(byte id) {
		if(!this.hasLadder(id)){
			return this.addLadder(new Ladder(id, this));
		}
		return this.getLadder(id);
	}
	

	public void doAction(String action, Matcher matcher) {
		System.out.println("ARDUINO::ACTION " + action);
		if(action == "addLadder") {
			this.addLadder(Byte.parseByte(matcher.group("id")));
		} else if (action == "ready") {
			this.ready();
		}
	}
	
	public Ladder addLadder(Ladder l) {
		if(!hasLadder(l.getId())) {
			this.getLadders().addElement(l);
			for(Skala listener : this.listeners) {
				listener.newLadderEvent(l);
			}
		}
		return l;
	}
	
	public void ready() {
		System.out.println("ARDUINO::READY");
		this.isReady = true;

		for(Skala listener : this.listeners) {
			listener.arduinoReadyEvent(this);
		}
		
	}
	
	@Override
	public void serialEvent(SerialPortEvent event) {
//		System.out.println("EVENT" + event.getEventType() + ": " + event.getEventValue());
		for(Skala l : listeners) {
			try {
				String msg = serialPort.readString();
				if(msg != null) {
					serialBuffer += msg;
				}
				if(serialBuffer.contains(";")) {
					String[] msgs = serialBuffer.split(";");
					for(String msgi : msgs){
						parseMessage(msgi);
					}
					if(msgs.length > 1){
						serialBuffer = msgs[msgs.length - 1];
					} else {
						serialBuffer = "";
					}
				}
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
			l.serialEvent(event);
		}	
	}


	public Vector<Ladder> getLadders() {
		return ladders;
	}


	public void setLadders(Vector<Ladder> ladders) {
		this.ladders = ladders;
	}	
}

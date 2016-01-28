package skala;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
//import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import edu.ufl.digitalworlds.gui.DWApp;
import edu.ufl.digitalworlds.j4k.DepthMap;
import edu.ufl.digitalworlds.j4k.Skeleton;
import javafx.geometry.Point3D;
import javafx.scene.control.ButtonBar;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortList;
import skala.behaviour.Behaviour;
import skala.behaviour.CalibrationBehaviour;
import skala.behaviour.DebugBehaviour;
import skala.behaviour.DefaultBehaviour;
import skala.behaviour.MysteriousBehaviour;
import skala.behaviour.SereneBehaviour;
import skala.behaviour.TouchBehaviour;

public class Skala extends DWApp
		implements KinectListener, SerialPortEventListener, Stated<Skala.State>, MouseListener, MouseMotionListener {
	protected State state;

	public static enum State {
		_Initial, Serene, Active, Tired, Calibrating, Debug
	};

	public double width = 6.0;
	public double depth = 6.0;

	private Behaviour behaviour;

	private static final long serialVersionUID = 1L;

	private JMenuItem calibrateButton;
	private JMenuItem kinectMenu;

	private Vector<SkalaListener> listeners;

	Sensor kinect1;
	Sensor kinect2;
	boolean kinect1_started;
	boolean kinect2_started;
	private Vector<User> users;
	private Vector<Arduino> arduinos;
	private Vector<Ladder> ladders;
	private Vector<Sensor> sensors;

	public Vector<Line> lines;

	public boolean isCalibrating = false;
	public boolean isCalibrated = false;

	private JPanel statusPanel;
	private JLabel statusLabel;

	private JPanel skalaPanel;

	private JButton calibrationButton;
	private JButton debugButton;
	private JButton defaultButton;
	private JButton printButton;
	private JButton loadButton;
	private JButton sereneButton;

	private DebugBehaviour debugBehaviour;

	Timer ticker;
	
	public void startCalibration() {
		System.out.println("Initializing calibration...");
		if (getArduinos().isEmpty()) {
			setStatus("Calibration failed: No arduinos available");
			return;
		}
		// if(getUsers().isEmpty()){
		// setStatus("Calibration failed: No users available");
		// return;
		// }
		// if(getUsers().size() > 1){
		// setStatus("Calibration failed: Only 1 user allowed");
		// return;
		// }

		Vector<Ladder> calibrationQueue = new Vector<Ladder>();

		// Arduino arduino = getArduinos().firstElement();
		for (Arduino arduino : arduinos) {
			calibrationQueue.addAll(arduino.getLadders());
		}

		setState(State.Calibrating);
		setBehaviour(new CalibrationBehaviour(this, calibrationQueue));
	}

	public void refreshLadders() {

	}

	public void tick(){
		getBehaviour().tick();
		repaint();
	}
	
	@Override
	public void MenuGUIsetup(JMenuBar menuBar) {
		JMenu skalaMenu = new JMenu("SKALA");

		calibrateButton = new JMenuItem("Calibrate");

		skalaMenu.add(calibrateButton);
		calibrateButton.addActionListener(this);

		skalaMenu.add(kinectMenu);

		menuBar.add(skalaMenu);
	}

	public void setStatus(String status) {
		System.out.println("STATUS: " + status);
		statusLabel.setText(status);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		getBehaviour().tick();
		getBehaviour().paint(g);
		if (this.debugBehaviour != null) {
			this.debugBehaviour.tick();
			this.debugBehaviour.paint(g);
		}

	}

	@Override
	public void GUIsetup(JPanel p_root) {
		setLoadingProgress("Intitializing Kinect...", 20);
//
//		this.ticker = new Timer();
//		Skala installation = this;
//		
////		ticker.scheduleAtFixedRate(new TimerTask() {
////			
////			@Override
////			public void run() {
////				installation.tick();				
////			}
////		}, 100, 30);
		
		setUsers(new Vector<User>());
		setArduinos(new Vector<Arduino>());
		setLadders(new Vector<Ladder>());
		setSensors(new Vector<Sensor>());

		this.listeners = new Vector<SkalaListener>();

		lines = new Vector<Line>();

		this.setState(State.Serene);
		this.setBehaviour(new DefaultBehaviour(this));

		String[] portNames = SerialPortList.getPortNames();

		for (String port : portNames) {
			System.out.println("PORT: " + port);
			Arduino arduino = new Arduino(port);
			getArduinos().add(arduino);
			arduino.addEventListener(this);
			arduino.initialize();
		}

		BorderLayout layout = new BorderLayout();
		skalaPanel = new JPanel(layout);

		skalaPanel.setBackground(Color.BLACK);
		p_root.setPreferredSize(new Dimension(800, 600));
		p_root.add(skalaPanel);

		kinect1 = new Sensor(0, new Point3D(1.315, 2.54, 0.0), new Point3D((23.0 / 180.0) * Math.PI, 0.0, 0.0));
		kinect2 = new Sensor(1, new Point3D(3.84, 2.95, 4.73), new Point3D((27.0 / 180.0) * Math.PI, Math.PI, 0.0));

		kinectMenu = new JMenu("Kinects");

		JPanel buttonBar = new JPanel();
		buttonBar.setPreferredSize(new java.awt.Dimension(skalaPanel.getWidth(), 24));
		skalaPanel.add(buttonBar, layout.NORTH);

		calibrationButton = new JButton("Calibrate");
		buttonBar.add(calibrationButton);
		calibrationButton.addActionListener(this);

		debugButton = new JButton("Debug");
		buttonBar.add(debugButton);
		debugButton.addActionListener(this);

		defaultButton = new JButton("Default");
		buttonBar.add(defaultButton);
		defaultButton.addActionListener(this);

		printButton = new JButton("PrintLadders");
		buttonBar.add(printButton);
		printButton.addActionListener(this);

		loadButton = new JButton("Load");
		buttonBar.add(loadButton);
		loadButton.addActionListener(this);


		sereneButton = new JButton("Serene");
		buttonBar.add(sereneButton);
		sereneButton.addActionListener(this);
		
		
		statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		skalaPanel.add(statusPanel, layout.SOUTH);

		java.awt.Dimension preferredSize = new java.awt.Dimension(skalaPanel.getWidth(), 24);
		statusPanel.setPreferredSize(preferredSize);
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel = new JLabel("Initializing...");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);

		skalaPanel.addMouseListener(this);
		skalaPanel.addMouseMotionListener(this);

		if (!kinect1.start()) {
			DWApp.showErrorDialog("ERROR",
					"<html><center><br>ERROR: The Kinect #1 device could not be initialized.<br><br>1. Check if the Microsoft's Kinect SDK was succesfully installed on this computer.<br> 2. Check if the Kinect is plugged into a power outlet.<br>3. Check if the Kinect is connected to a USB port of this computer.</center>");
		} else {
			System.out.println("KINECT 1 STARTED");
			int xAngle = kinect1.getElevationAngle();
			System.out.println(xAngle);
			kinect1_started = true;
			kinect1.addListener(this);

			getSensors().add(kinect1);

			JMenuItem k1Button = new JMenuItem("Kinect 1");
			k1Button.setActionCommand("view");
			k1Button.setMnemonic(kinect1.id);
			k1Button.addActionListener(this);
			kinectMenu.add(k1Button);
		}

		if (!kinect2.start()) {
			DWApp.showErrorDialog("ERROR",
					"<html><center><br>ERROR: The Kinect #2 device could not be initialized.<br><br>1. Check if the Microsoft's Kinect SDK was succesfully installed on this computer.<br> 2. Check if the Kinect is plugged into a power outlet.<br>3. Check if the Kinect is connected to a USB port of this computer.</center>");
		} else {
			System.out.println("KINECT 2 STARTED");
			int xAngle = kinect2.getElevationAngle();
			System.out.println(xAngle);
			// kinect2.angles.subtract(new Point3D((xAngle/180)*Math.PI, 0.0,
			// 0.0));

			kinect2_started = true;
			kinect2.addListener(this);

			sensors.add(kinect2);

			JMenuItem k2Button = new JMenuItem("Kinect 2");
			k2Button.setActionCommand("view");
			k2Button.setMnemonic(kinect2.id);
			k2Button.addActionListener(this);
			kinectMenu.add(k2Button);
		}
	}

	public void GUIclosing() {
		kinect1.stop();
		kinect2.stop();
	}

	public static void main(String args[]) {
		createMainFrame("Skala");
		app = new Skala();
		setFrameSize(900, 900, null);
	}

	@Override
	public void GUIactionPerformed(ActionEvent e) {
		Object source = e.getSource();
		// System.out.println(e.getActionCommand());
		if (e.getActionCommand() == "view") {
			JMenuItem menuItem = (JMenuItem) source;
			int kinectId = menuItem.getMnemonic();
			// System.out.println("VIEW " + kinectId);

			for (Sensor sensor : getSensors()) {
				if (sensor.id == kinectId) {
					sensor.showViewerDialog(false);
				}
			}
		}
		if (source == calibrateButton) {
			startCalibration();
		}

		if (source == calibrationButton) {
			if (this.getState() == State.Calibrating)
				((CalibrationBehaviour) this.getBehaviour()).capture();
			else
				startCalibration();
		}

		if (source == debugButton) {
			setState(State.Debug);
			this.debugBehaviour = new DebugBehaviour(this);
		}
		if (source == defaultButton) {
			this.setBehaviour(new TouchBehaviour(this));
		}
		if (source == printButton) {
			for (Ladder l : this.getLadders()) {
				 System.out.println("if(i == " + l.getId() +
				 "){l.setPosition(new Point3D(" + l.position.getX() + ", " +
				 l.position.getY() + ", " + l.position.getZ()+ "));}");
			}
		}
		if (source == loadButton) {
			loadLadders();
		}

		if (source == sereneButton) {
			this.setBehaviour(new SereneBehaviour(this));
		}
		
		
	}

	public User addUser(User u) {
		getUsers().add(u);
		u.addListener(this.getBehaviour());
		u.fireUserEnterEvent();
		return u;
	}

	public User getUser(int id) {
		for (User u : getUsers()) {
			if (u.getId() == id)
				return u;
		}
		return addUser(new User(id));
	}

	public boolean hasUser(int id) {
		for (User v : getUsers()) {
			if (v.getId() == id)
				return true;
		}
		return false;
	}

	public boolean hasUser(User u) {
		return hasUser(u.getId());
	}

	@Override
	public void handleDepthFrame(DepthMap depthMap, float[] XYZ) {
		// repaint();
	}

	public Vector<RealWorldObject> getRealWorldObjects() {
		Vector<RealWorldObject> objects = new Vector<RealWorldObject>();
		objects.addAll(getLadders());
		objects.addAll(getUsers());
		objects.addAll(getSensors());

		return objects;
	}

	@Override
	public void handleSkeletonEvent(Skeleton skeleton, double[][] realWorldPositions, int kinectId) {
		int uId = kinectId * 1000 + (skeleton.getPlayerID() % 1000);

		if (skeleton.isTracked()) {
			User u = getUser(uId);
			u.updateSkeleton(skeleton, realWorldPositions);

			if (u.isPointing()) {
				for (RealWorldObject target : getRealWorldObjects()) {
					if (u.isTouching(target)) {
						u.fireTouchingEvent(target);
					} else if (u.isPointingAt(target)) {
						u.firePointingEvent(target);
					}
				}
			}
		}
		repaint();
	}

	public void removeUser(User u) {
		getUsers().remove(u);
		u.invalidate();
		u.fireUserExitEvent();
	}

	@Override
	public void handleVideoFrame(byte[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleIRFrame(byte[] data) {
		// TODO Auto-generated method stub
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		// System.out.println("IM SUPER SERIAL: " + event.getPortName() + " : "
		// + event.getEventType()+ " : " + event.getEventValue());
	}

	public void newLadderEvent(Ladder l) {
		// repaint();
	}

	public void arduinoReadyEvent(Arduino arduino) {
		// System.out.println("Arduino " + arduino.address + " ready for
		// action");
		boolean ready = true;
		for (Arduino a : this.getArduinos()) {
			ready = ready && a.isReady;
		}
		if (ready) {
			setState(State.Serene);
			setBehaviour(new DefaultBehaviour(this));

			setStatus("Ready for calibration...");
			statusPanel.setBackground(new Color(255, 170, 0));
		}
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.fireStateTransitionEvent(this.state, state);
		this.state = state;
	}

	public void fireStateTransitionEvent(State from, State to) {		
		for (SkalaListener l : this.listeners) {
			l.onSkalaStateTransition(this, from, to);
		}
	}

	public void addListener(SkalaListener listener) {
		this.listeners.addElement(listener);
	}

	public Behaviour getBehaviour() {
		return behaviour;
	}

	public void setBehaviour(Behaviour behaviour) {
		for (User u : getUsers()) {
			u.addListener(behaviour);
			u.removeListener(this.behaviour);
		}
		this.behaviour = behaviour;
	}

	public void setIsCalibrated(boolean b) {
		isCalibrated = true;
		isCalibrating = false;
		setStatus("Calibration complete");
		statusPanel.setBackground(new Color(33, 200, 44));
	}

	public Vector<Ladder> getLadders() {
		return ladders;
	}

	public void setLadders(Vector<Ladder> ladders) {
		this.ladders = ladders;
	}

	public Vector<User> getUsers() {
		return users;
	}

	public void setUsers(Vector<User> users) {
		this.users = users;
	}

	public Vector<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(Vector<Sensor> sensors) {
		this.sensors = sensors;
	}

	public Point3D screen2World(int x, int y) {
		return new Point3D((5.0 * x) / getWidth(), 1.5, (5.0 * y) / getHeight());
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if (this.debugBehaviour == null)
			return;

		int screenX = event.getX();
		int screenY = event.getY();

		Point3D p = screen2World(screenX, screenY);
		DebugBehaviour b = (DebugBehaviour) this.getDebugBehaviour();

		int butt = event.getButton();

		switch (butt) {
		case MouseEvent.BUTTON1:
			b.onClick(p);
			break;
		case MouseEvent.BUTTON2:
			b.onMiddleClick(p);
			break;
		case MouseEvent.BUTTON3:
			b.onRightClick(p);
			break;
		}

	}

	@Override
	public void mouseEntered(MouseEvent event) {
		if (this.debugBehaviour == null)
			return;
		int screenX = event.getX();
		int screenY = event.getY();

		Point3D p = screen2World(screenX, screenY);
		DebugBehaviour b = (DebugBehaviour) this.debugBehaviour;

		b.onEnter(p);
	}

	@Override
	public void mouseExited(MouseEvent event) {
		if (this.debugBehaviour == null)
			return;
		int screenX = event.getX();
		int screenY = event.getY();

		Point3D p = screen2World(screenX, screenY);
		DebugBehaviour b = (DebugBehaviour) this.debugBehaviour;

		b.onExit(p);
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if (this.debugBehaviour == null)
			return;
		int screenX = event.getX();
		int screenY = event.getY();

		if (!isCalibrated) {

		}

		Point3D p = screen2World(screenX, screenY);
		// System.out.println("mousePressed " + p);
		DebugBehaviour b = (DebugBehaviour) this.debugBehaviour;

		b.onPress(p);
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (this.debugBehaviour == null)
			return;
		int screenX = event.getX();
		int screenY = event.getY();

		Point3D p = screen2World(screenX, screenY);
		// System.out.println("mouseReleased " + p);
		DebugBehaviour b = (DebugBehaviour) this.debugBehaviour;

		b.onRelease(p);
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		if (this.debugBehaviour == null)
			return;
		int screenX = event.getX();
		int screenY = event.getY();

		Point3D p = screen2World(screenX, screenY);
		// System.out.println("mouseReleased " + p);
		DebugBehaviour b = (DebugBehaviour) this.debugBehaviour;

		b.onDrag(p);
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		if (this.debugBehaviour == null)
			return;
		int screenX = event.getX();
		int screenY = event.getY();

		Point3D p = screen2World(screenX, screenY);
		// System.out.println("mouseReleased " + p);
		DebugBehaviour b = (DebugBehaviour) this.debugBehaviour;

		b.onMove(p);
	}

	private DebugBehaviour getDebugBehaviour() {
		// TODO Auto-generated method stub
		return debugBehaviour;
	}

	public Vector<Arduino> getArduinos() {
		return arduinos;
	}

	public void setArduinos(Vector<Arduino> arduinos) {
		this.arduinos = arduinos;
	}

	private void loadLadders() {
		for (Arduino arduino : arduinos) {
			getLadders().addAll(arduino.getLadders());
		}
		Vector<Ladder> calibrationQueue = new Vector<Ladder>();
		for (int i = 0; i < getLadders().size(); i++) {
			Ladder l = getLadders().get(i);
			if(i == 0){l.setPosition(new Point3D(1.7595142100533587, 1.0907482065015621, 1.9662736106745067));}
			if(i == 1){l.setPosition(new Point3D(0.9807414966622449, 0.8089512844403319, 1.7323150883683445));}
			if(i == 2){l.setPosition(new Point3D(1.0792043792598236, 0.8419521246142861, 1.2713038136635477));}
			if(i == 3){l.setPosition(new Point3D(1.193003985437587, 1.1513958388031873, 2.2202792832884883));}
			if(i == 4){l.setPosition(new Point3D(1.3055591973951803, 1.2351719884311727, 1.078944957737182));}
			if(i == 10){l.setPosition(new Point3D(1.5914960488762944, 0.9930189474562847, 1.2135311959332447));}
			if(i == 11){l.setPosition(new Point3D(1.2936608502252873, 1.3166968466769244, 1.5139135816497888));}
			if(i == 12){l.setPosition(new Point3D(1.6496426633407426, 1.3532008031048721, 1.8665039313746834));}
			if(i == 13){l.setPosition(new Point3D(1.3413452301356086, 1.3755209973600842, 2.6206435828886168));}
			if(i == 14){l.setPosition(new Point3D(0.7273631396180957, 1.0531951908275974, 2.081339635642819));}

			if(i == 15){l.setPosition(new Point3D(3.813005974797158, 0.9958837997945194, 2.761402668808488));}
			if(i == 16){l.setPosition(new Point3D(4.748672650259115, 1.9619683651125532, 2.6168574536741467));}
			if(i == 17){l.setPosition(new Point3D(3.6092246348282995, 1.2546528401754249, 3.109398872315892));}
			if(i == 18){l.setPosition(new Point3D(4.2682316079149825, 1.2912708841222589, 3.094884112826797));}
			if(i == 19){l.setPosition(new Point3D(3.923374910708108, 1.1137233808161597, 3.7663016068793516));}
			if(i == 5){l.setPosition(new Point3D(4.4794353767983095, 1.5520925736162767, 3.4549565567643796));}
			if(i == 6){l.setPosition(new Point3D(4.226392420770584, 0.9846233887698124, 2.640587161522782));}
			if(i == 7){l.setPosition(new Point3D(4.497599071373282, 1.8621605747471426, 3.5176271293343744));}
			if(i == 8){l.setPosition(new Point3D(3.694831647241466, 0.9970374403917233, 3.3990259090812875));}
			if(i == 9){l.setPosition(new Point3D(4.298648281113466, 1.0189592895103887, 2.2563567357765226));}
			
//			if(i == 15){l.setPosition(new Point3D(3.813005974797158, 0.9958837997945194, 2.761402668808488));}
//			if(i == 16){l.setPosition(new Point3D(4.748672650259115, 1.9619683651125532, 2.6168574536741467));}
//			if(i == 17){l.setPosition(new Point3D(3.6092246348282995, 1.2546528401754249, 3.109398872315892));}
//			if(i == 18){l.setPosition(new Point3D(4.2682316079149825, 1.2912708841222589, 3.094884112826797));}
//			if(i == 19){l.setPosition(new Point3D(3.923374910708108, 1.1137233808161597, 3.7663016068793516));}
//			if(i == 5){l.setPosition(new Point3D(4.4794353767983095, 1.5520925736162767, 3.4549565567643796));}
//			if(i == 6){l.setPosition(new Point3D(4.226392420770584, 0.9846233887698124, 2.640587161522782));}
//			if(i == 7){l.setPosition(new Point3D(4.497599071373282, 1.8621605747471426, 3.5176271293343744));}
//			if(i == 8){l.setPosition(new Point3D(3.694831647241466, 0.9970374403917233, 3.3990259090812875));}
//			if(i == 9){l.setPosition(new Point3D(4.298648281113466, 1.0189592895103887, 2.2563567357765226));}

			if(l.getPosition().getX() == 0.0) {
				calibrationQueue.add(l);
			}
		}
		
		
		if(calibrationQueue.isEmpty()) {
			isCalibrated = true;
		} else {
			setState(State.Calibrating);
			setBehaviour(new CalibrationBehaviour(this, calibrationQueue));
		}
		repaint();
	}

}

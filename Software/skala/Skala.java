package skala;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
//import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.BoxLayout;
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
import javafx.scene.shape.Line;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortList;
import skala.behaviour.Behaviour;
import skala.behaviour.CalibrationBehaviour;
import skala.behaviour.DefaultBehaviour;

public class Skala extends DWApp implements KinectListener, SerialPortEventListener, Stated<Skala.State>{	
	protected State state;
	
	public static enum State {
		_Initial,
		Serene,
		Active,
		Tired
	};
	
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
	Vector<Arduino> arduinos;
	private Vector<Ladder> ladders;
	Vector<Sensor> sensors;
	
	public Vector<Line> lines;
	
	public boolean isCalibrating = false;
	public boolean isCalibrated = false;
	
	private JPanel statusPanel;
	private JLabel statusLabel;

	private JPanel skalaPanel;
	
	private Canvas skalaCanvas;
	
	public void startCalibration() {
		System.out.println("Initializing calibration...");
		if(arduinos.isEmpty()){
			setStatus("Calibration failed: No arduinos available");
			return;
		}
		if(getUsers().isEmpty()){
			setStatus("Calibration failed: No users available");
			return;
		}
		if(getUsers().size() > 1){
			setStatus("Calibration failed: Only 1 user allowed");
			return;
		}

		for(Arduino arduino : arduinos) {
			getLadders().addAll(arduino.ladders);
		}
		
		setBehaviour(new CalibrationBehaviour(this, getLadders(), getUsers().firstElement()));
	}
	
	public void refreshLadders(){
		
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
	
	public void draw(Graphics g, Point3D pos, Color color, double size) {

		g.setColor(color);
		
		int width = getWidth();
		int height = getHeight();
		
		double z = pos.getY();
		
		int xsize = (int) Math.round((width / 5.0) * z * size);
		int ysize = (int) Math.round((height / 5.0) * z * size);
		
		int x = (int) Math.round(((width / 5.0) * pos.getX()) - (xsize / Math.sqrt(4)));
		int y = (int) Math.round(((height / 5.0) * pos.getZ()) - (ysize / Math.sqrt(4)));
		
		
		g.drawOval(x,y, xsize, ysize);
	}
	public void drawLine(Graphics g, Point3D from, Point3D to, Color color) {

		g.setColor(color);
		
		int width = getWidth();
		int height = getHeight();
		
		int x1 = (int) Math.round(((width / 5.0) * (from.getX())));
		int y1 = (int) Math.round(((height / 5.0) * (from.getZ())));
		
		int x2 = (int) Math.round(((width / 5.0) * (to.getX())));
		int y2 = (int) Math.round(((height / 5.0) * (to.getZ())));
		
		
		
		g.drawLine(x1, y1, x2, y2);
	}
	
	
	@Override
	public void paint(Graphics g){
		super.paint(g);
		
		getBehaviour().tick();
		getBehaviour().paint(g);

		int width = getWidth();
		int height = getHeight();
		
		
		if(!getUsers().isEmpty()){
			Vector<User> invalids = new Vector<User>();
			for(User u : getUsers()){
				Color color = new Color(255, 255, 255);
				if(u.isValid()){
					color = (Color) u.getData("color");
				}
			
				Point3D hPos= u.joints.get(Skeleton.HEAD);

				int x = (int) Math.round(((width / 5.0) * hPos.getX()));
				int y = (int) Math.round(((height / 5.0) * hPos.getZ()));
				
				g.setColor(Color.white);
				
				Vector<String> label = new Vector<String>();
				
				
				label.add("Aura: " + u.getData("aura"));
				label.add(u.getState() == User.State.Calming ? "Calming" : "NOTCALMING");
				label.add("Age: " + u.getAge());
				
				int r = 0;
				for(String s : label) {
					g.drawChars(s.toCharArray(), 0, s.length(), x + 20, y + 20 * r++);
				}
				
				Point3D nPos= u.joints.get(Skeleton.NECK);

				Point3D lsPos= u.joints.get(Skeleton.SHOULDER_LEFT);
				Point3D rsPos= u.joints.get(Skeleton.SHOULDER_RIGHT);
				
				Point3D lePos= u.joints.get(Skeleton.ELBOW_LEFT);
				Point3D rePos= u.joints.get(Skeleton.ELBOW_RIGHT);
				
				Point3D lhPos= u.joints.get(Skeleton.HAND_LEFT);
				Point3D rhPos= u.joints.get(Skeleton.HAND_RIGHT);
				
				Point3D smPos= u.joints.get(Skeleton.SPINE_MID);
				Point3D sbPos= u.joints.get(Skeleton.SPINE_BASE);
				
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

		draw(g, kinect1.getPosition(), Color.RED, 0.05);
		draw(g, kinect1.getPosition(), Color.RED, 0.10);
		draw(g, kinect1.getPosition(), Color.RED, 0.15);
		
		draw(g, kinect2.getPosition(), Color.RED, 0.05);
		draw(g, kinect2.getPosition(), Color.RED, 0.10);
		draw(g, kinect2.getPosition(), Color.RED, 0.15);
		
		for(Ladder l: getLadders()){
			Point3D position = l.position;
			draw(g, position, Color.RED, 10);
		}	
	}
	
	@Override
	public void GUIsetup(JPanel p_root) {
		setLoadingProgress("Intitializing Kinect...",20);	

		setUsers(new Vector<User>());
		arduinos = new Vector<Arduino>();
		setLadders(new Vector<Ladder>());
		sensors = new Vector<Sensor>();
		
		lines = new Vector<Line>();
		
		this.setBehaviour(new DefaultBehaviour(this, getLadders()));
		
		String[] portNames = SerialPortList.getPortNames();
		
//		for(String port : portNames) {
		String port = portNames[0];
			System.out.println("PORT: " + port);
			Arduino arduino = new Arduino(port);
			arduinos.add(arduino);
			arduino.addEventListener(this);
			arduino.initialize();
//		}

		
		BorderLayout layout = new BorderLayout();
		skalaPanel = new JPanel(layout);

		skalaPanel.setBackground(Color.BLACK);
		p_root.add(skalaPanel);
		
//		double xAngle = Math.PI * 0.0 / 180.0;
		
		kinect1=new Sensor(0, new Point3D(2.5,.97,0.5), new Point3D((1.0/180.0)*Math.PI, 0.0, 0.0));
		kinect2=new Sensor(1, new Point3D(2.5,1.15,3.5), new Point3D((1.0/180.0)*Math.PI, Math.PI, 0.0));
		
		kinectMenu = new JMenu("Kinects");
		
		statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		skalaPanel.add(statusPanel, layout.SOUTH);
		
		skalaCanvas = new Canvas();
//		skalaPanel.add(skalaCanvas, layout.CENTER);
		
		
		java.awt.Dimension preferredSize = new java.awt.Dimension(skalaPanel.getWidth(), 24);
		statusPanel.setPreferredSize(preferredSize);
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel = new JLabel("Initializing...");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);
		
		if(!kinect1.start())
		{
			DWApp.showErrorDialog("ERROR", "<html><center><br>ERROR: The Kinect #1 device could not be initialized.<br><br>1. Check if the Microsoft's Kinect SDK was succesfully installed on this computer.<br> 2. Check if the Kinect is plugged into a power outlet.<br>3. Check if the Kinect is connected to a USB port of this computer.</center>");
		}
		else{
			System.out.println("KINECT 1 STARTED");
			int xAngle = kinect1.getElevationAngle();
			System.out.println(xAngle);
//			kinect1.angles.subtract(new Point3D((xAngle/180)*Math.PI, 0.0, 0.0));
			kinect1_started = true;
			kinect1.addListener(this);
			
			sensors.add(kinect1);
			
			JMenuItem k1Button = new JMenuItem("Kinect 1");
			k1Button.setActionCommand("view");
			k1Button.setMnemonic(kinect1.id);
			k1Button.addActionListener(this);
			kinectMenu.add(k1Button);
		}

		
		if(!kinect2.start())
		{
			DWApp.showErrorDialog("ERROR", "<html><center><br>ERROR: The Kinect #2 device could not be initialized.<br><br>1. Check if the Microsoft's Kinect SDK was succesfully installed on this computer.<br> 2. Check if the Kinect is plugged into a power outlet.<br>3. Check if the Kinect is connected to a USB port of this computer.</center>");
		}
		else{
			System.out.println("KINECT 2 STARTED");
			int xAngle = kinect2.getElevationAngle();
			System.out.println(xAngle);
//			kinect2.angles.subtract(new Point3D((xAngle/180)*Math.PI, 0.0, 0.0));
			
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

	public void GUIclosing()
	{
		kinect1.stop();
		kinect2.stop();
	}
	public static void main(String args[]) {
		createMainFrame("Skala");
		app=new Skala();
		setFrameSize(1600,900,null);
	}
	@Override
	public void GUIactionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		System.out.println(e.getActionCommand());
		if(e.getActionCommand() == "view"){
			JMenuItem menuItem = (JMenuItem) source;
			int kinectId = menuItem.getMnemonic();
			System.out.println("VIEW " + kinectId);
			
			for(Sensor sensor : sensors) {
				if(sensor.id == kinectId){
					sensor.showViewerDialog(false);		
				}
			}
		}
		if(source==calibrateButton){
			startCalibration();
		}
	}
	
	public User addUser(User u) {
		getUsers().add(u);
		u.addListener(this.getBehaviour());
		u.fireUserEnterEvent();
		return u;
	}
	public User getUser(int id) {
		for(User u : getUsers()) {
			if(u.getId() == id) return u;
		}
		return addUser(new User(id));
	}
	public boolean hasUser(int id) {
		for(User v : getUsers()) {
			if(v.getId() == id) return true;
		}
		return false;
	}
	public boolean hasUser(User u) {
		return hasUser(u.getId());
	}
	@Override
	public void handleDepthFrame(DepthMap depthMap, float[] XYZ) {
		repaint();
	}

	public Vector<RealWorldObject> getRealWorldObjects(){
		Vector<RealWorldObject> objects = new Vector<RealWorldObject>();
		objects.addAll(getLadders());
		objects.addAll(getUsers());
		objects.addAll(sensors);
		
		return objects;
	}
	
	@Override
	public void handleSkeletonEvent(Skeleton skeleton, double[][] realWorldPositions, int kinectId) {
		int uId = kinectId*1000 + (skeleton.getPlayerID() % 1000);
		
		if(skeleton.isTracked()){
			User u = getUser(uId);
			u.updateSkeleton(skeleton, realWorldPositions);
			
			if(u.isPointing()){
				for(RealWorldObject target : getRealWorldObjects()) {
					if(u.isPointingAt(target)){
						u.firePointingEvent(target);
					}
				}
			}
		}		
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
//		System.out.println("IM SUPER SERIAL: " + event.getPortName() + " : " + event.getEventType()+ " : " + event.getEventValue());
	}
	public void newLadderEvent(Ladder l) {
		repaint();
	}
	
	public void arduinoReadyEvent(Arduino arduino) {
		System.out.println("Arduino " + arduino.address + " ready for action");
		boolean ready = true;
		for(Arduino a : this.arduinos) {
			ready = ready && a.isReady;
		}
		if(ready) {
			setBehaviour(new DefaultBehaviour(this, getLadders()));
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
}

import java.awt.*;



import java.awt.event.*;

import javax.swing.*;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import java.io.IOException;
import java.util.ArrayList;

public class SkyFrame extends JFrame {

	/**
	 * The constructor sets up the window and makes it visible on the screen.  
	 * It starts a thread that will open a connection to a SkyHub.
	 * The window will become operational when the game stops, or it will be closed
	 * and the program terminated if the connection attempt fails.
	 * @param hubHostName the host name or IP address where the SkyHub is listening.
	 * @param hubPort the port number where the SkyHub is listening.
	 */
	public SkyFrame(final String hubHostName, final int hubPort, boolean host, int numObstacles) {
		super("HuntingJupiter");
		winner=false;
		loser=false;
		start=false;
		this.host=host;
		crashing=false;
		receivedObstacles=false;
		this.numObstacles=numObstacles;
		stars=makeStars();
		winPoint=new Point(700,2/500/4,0);
		display = new Display();
		setContentPane(display);
		pack();
		setResizable(false);
		setLocation(200,100);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowAdapter() {  // A listener to end the program when the user closes the window.
			public void windowClosing(WindowEvent evt) {
				doQuit();
			}
		});
		setVisible(true);
		winThread=new Thread() {//keeps track of coordinates for the ending screen
			public void run() {
				while(true)
				{
					winPoint.setZ(winPoint.getZ()+5);
					winPoint.setY(winPoint.getY()+2);
					try {
						sleep(10L);
					}
					catch (Exception e)
					{

					}
				}
			}
		};
		new Thread() {  // A thread to open the connection to the server.
			public void run() {
				try {
					final PokerClient c = new PokerClient(hubHostName,hubPort);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							connection = c;
							if (c.getID() == 1) { 
								// This is Player #1.  Still have to wait for second player to
								// connect.  Change the message display to reflect that fact.
								messageFromServer.setText("GROUND CONTROL: Waiting for an opponent to connect...");
							}
						}
					});
				}
				catch (final IOException e) {
					// Error while trying to connect to the server.  Tell the
					// user, and end the program.  Use SwingUtilties.invokeLater()
					// because this happens in a thread other than the GUI event thread.
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							dispose();
							JOptionPane.showMessageDialog(null,"Could not connect to "
									+ hubHostName +".\nError:  " + e);
							System.exit(0);
						}
					});
				}
			}
		}.start();

		new Thread() {  //waits for hub to send start message
			public void run() {
				while(!(start))
				{
					try{
						sleep(1000L);
					}
					catch (Exception e){};
					continue;
				}
				new FlyFrame();

			}
		}.start();

	}
	/**
	 * Randomly generates a series of points representing stars.
	 */
	private ArrayList<Dimension> makeStars()
	{
		ArrayList<Dimension> stars=new ArrayList<Dimension>();
		for(int i=0;i<20;i++)
			stars.add(new Dimension((int)(Math.random()*700),(int)(Math.random()*500/2)));
		return stars;
	}
	public void closeSkyFrame()
	{
		WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	}

	// ---------------------- Private inner classes -----------------------------------
	/**
	 * This class is the screen in which the racing takes place.
	 *
	 */
	private class FlyFrame extends JFrame implements KeyListener
	{
		FlyFrame()
		{
			super("I have too much free time.");
			addKeyListener(this);
			setSize(new Dimension(700,500));
			setBackground(Color.BLACK);
			setContentPane(new FlyDisplay());
			setLocation(200,100);
			addWindowListener( new WindowAdapter() {  // A listener to end the program when the user closes the window.
				public void windowClosing(WindowEvent evt) {
					doQuit();
				}
			});
			setVisible(true);
		}

		/**
		 * A nested panel inside FlyFrame.
		 */
		private class FlyDisplay extends JPanel
		{
			Thread repainter;
			boolean one, two, three;
			public FlyDisplay()
			{

				one=false;
				two=false;
				three=true;
				setBackground(Color.BLACK);
				repaint();
				repainter=new Thread() {//repaints every 0.05 sec
					public void run() {
						while(true)
						{
							repaint();
							try {sleep (50L);} catch (Exception e) {};
						}
					}
				};
				new Thread() {//Countdown thread, also plays audio, depending on game length
					public void run() {
						three=true;
						repainter.start();
						try {
							sleep(1000L);
							three=false;
							two=true;
							sleep(1000L);
							two=false;
							one=true;
							sleep(1000L);
							one=false;
							if(host)
								connection.send("starting");
							try
							{
								player = AudioPlayer.player;

								if(numObstacles==30)
									stream = new AudioStream(getClass().getResourceAsStream("jupiter.wav"));
								else if(numObstacles==450)
									stream = new AudioStream(getClass().getResourceAsStream("jupiter.wav"));
								else
									stream = new AudioStream(getClass().getResourceAsStream("jupiter.wav"));
								player.start(stream);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}.start();

			}
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				g.setColor(Color.GREEN);
				if(three || two || one)
				{
					g.setFont(new Font("Courier",Font.PLAIN,20));
					g.drawString("Watch out for space debris!",700/5,500/2+100);
					g.drawString("Warp portals are your friends!",700/5,500/2+121);
				}
				if(three)
				{
					g.setFont(new Font("Arial Bold", Font.ITALIC, 48));
					g.drawString("3", 350, 250);
				}
				else if(two)
				{
					g.setFont(new Font("Arial Bold", Font.ITALIC, 48));
					g.drawString("2", 350, 250);
				}
				else if(one)
				{
					g.setFont(new Font("Arial Bold", Font.ITALIC, 48));
					g.drawString("1", 350, 250);
				}
				else
				{
					if(winner || loser)
					{
						super.paintComponent(g);
						winScene(g);
					}
					else
					{
						//draw stars
						drawStars(g);
						//draw planet
						drawPlanet(g,(double)yourPos.getZ()/obstacles.get(numObstacles-1).getZ());
						//draw obstacles, can call winScene
						drawObstacles(g);
						//draw opponent:
						drawOpponent(g);
						//draw yourself:
						drawSelf(g);
					}

					g.setColor(Color.WHITE);
					g.setFont(new Font("Arial",Font.PLAIN,12));
					g.drawString("Dist. Travelled: "+Integer.toString(yourPos.getZ()), 10, 10);
					g.drawString("Opponent Dist. Travelled: "+Integer.toString(opponentPos.getZ()), 10, 25);
				}
			}
			private void drawPlanet(Graphics g, double i)
			{
				g.setColor(new Color(200,100,0));//planet below
				g.fillOval(-100,(int)((500/2)-(500/2)*i),900,900);
			}
			private void drawStars(Graphics g)
			{
				g.setColor(Color.WHITE);
				for(int i=0;i<stars.size();i++)
					g.drawRect(stars.get(i).width, stars.get(i).height, 1, 1);
			}
			private void drawSelf(Graphics g)
			{
				g.setColor(YOU_OK_COLOR);
				g.fillPolygon(new int[]{yourPos.getX(),yourPos.getX()-2*PLAYER_LENGTH/5,yourPos.getX()-3*PLAYER_LENGTH/5,yourPos.getX()-PLAYER_LENGTH-2,yourPos.getX()-5*PLAYER_LENGTH/7,yourPos.getX()-PLAYER_LENGTH,yourPos.getX()-3*PLAYER_LENGTH/5,yourPos.getX()-2*PLAYER_LENGTH/5,yourPos.getX(),yourPos.getX()-2*PLAYER_LENGTH/7},
						new int[] {yourPos.getY(),yourPos.getY()-2*PLAYER_LENGTH/5,yourPos.getY()-2*PLAYER_LENGTH/5,yourPos.getY(),yourPos.getY()-PLAYER_LENGTH/2,yourPos.getY()-PLAYER_LENGTH,yourPos.getY()-3*PLAYER_LENGTH/5,yourPos.getY()-3*PLAYER_LENGTH/5,yourPos.getY()-PLAYER_LENGTH,yourPos.getY()-PLAYER_LENGTH/2},10);
			}
			private void drawOpponent(Graphics g)
			{
				if(yourPos.getZ()<=opponentPos.getZ() && (opponentPos.getZ()-yourPos.getZ()<RENDER_DISTANCE))
				{
					g.setColor(Color.DARK_GRAY);
					double zScaleFactor=0.95*(RENDER_DISTANCE+yourPos.getZ()-opponentPos.getZ())/RENDER_DISTANCE;
					int[] xCoords=new int[] {opponentPos.getX(),opponentPos.getX()-2*PLAYER_LENGTH/5,opponentPos.getX()-3*PLAYER_LENGTH/5,opponentPos.getX()-PLAYER_LENGTH-2,opponentPos.getX()-5*PLAYER_LENGTH/7,opponentPos.getX()-PLAYER_LENGTH,opponentPos.getX()-3*PLAYER_LENGTH/5,opponentPos.getX()-2*PLAYER_LENGTH/5,opponentPos.getX(),opponentPos.getX()-2*PLAYER_LENGTH/7};
					int[] yCoords=new int[] {opponentPos.getY(),opponentPos.getY()-2*PLAYER_LENGTH/5,opponentPos.getY()-2*PLAYER_LENGTH/5,opponentPos.getY(),opponentPos.getY()-PLAYER_LENGTH/2,opponentPos.getY()-PLAYER_LENGTH,opponentPos.getY()-3*PLAYER_LENGTH/5,opponentPos.getY()-3*PLAYER_LENGTH/5,opponentPos.getY()-PLAYER_LENGTH,opponentPos.getY()-PLAYER_LENGTH/2};
					for(int i=0;i<xCoords.length;i++)
						xCoords[i]=(int)((xCoords[i]-700/2)*zScaleFactor+700/2);
					for(int i=0;i<yCoords.length;i++)
						yCoords[i]=(int)((yCoords[i]-500/2)*zScaleFactor+500/2);
					g.fillPolygon(xCoords,yCoords,10);

				}
			}
			private void winScene(Graphics g)
			{
				g.setColor(new Color(255, 104, 31));
				g.fillRect(0, 500/2, 700, 500/2);
				//repainter.suspend();
				g.setFont(new Font("Arial Bold", Font.ITALIC, 48));
				if(winner)
				{
					g.drawString("WINNER",700/3,500/2);
					g.setColor(Color.WHITE);
				}
				else
				{
					g.drawString("LOSER",700/2,500/2);
					g.setColor(Color.GRAY);
				}
				//draw winner
				double zScaleFactor=((double)(winPoint.getZ())/2000);
				int[] xCoords=new int[] {winPoint.getX(),winPoint.getX()-2*(PLAYER_LENGTH+20)/5,winPoint.getX()-3*(PLAYER_LENGTH+20)/5,winPoint.getX()-(PLAYER_LENGTH+20)-2,winPoint.getX()-5*(PLAYER_LENGTH+20)/7,winPoint.getX()-(PLAYER_LENGTH+20),winPoint.getX()-3*(PLAYER_LENGTH+20)/5,winPoint.getX()-2*(PLAYER_LENGTH+20)/5,winPoint.getX(),winPoint.getX()-2*(PLAYER_LENGTH+20)/7};
				int[] yCoords=new int[] {winPoint.getY(),winPoint.getY()-2*(PLAYER_LENGTH+20)/5,winPoint.getY()-2*(PLAYER_LENGTH+20)/5,winPoint.getY(),winPoint.getY()-(PLAYER_LENGTH+20)/2,winPoint.getY()-(PLAYER_LENGTH+20),winPoint.getY()-3*(PLAYER_LENGTH+20)/5,winPoint.getY()-3*(PLAYER_LENGTH+20)/5,winPoint.getY()-(PLAYER_LENGTH+20),winPoint.getY()-(PLAYER_LENGTH+20)/2};
				for(int i=0;i<xCoords.length;i++)
					xCoords[i]=(int)((xCoords[i]-700/2)*zScaleFactor+700/2);
				for(int i=0;i<yCoords.length;i++)
					yCoords[i]=(int)((yCoords[i]-500/2)*zScaleFactor+500/2);
				g.fillPolygon(xCoords,yCoords,10);
				if(!winThread.isAlive())
					winThread.start();
			}

			private void drawObstacles(Graphics g)
			{
				double zScaleFactor;

				for(int i=numObstacles-1;i>=0;i--)
				{

					if(yourPos.getZ()>obstacles.get(i).getZ())
					{
						if(i==numObstacles-1)
							connection.send("done");

						break;
					}
					zScaleFactor=0.95*(RENDER_DISTANCE+yourPos.getZ()-obstacles.get(i).getZ())/RENDER_DISTANCE;
					g.setColor(new Color(150,0,0));
					g.fillRect((int)(zScaleFactor*(obstacles.get(i).getX()-OBSTACLE_LENGTH)+700/2), (int)(zScaleFactor*(obstacles.get(i).getY()-OBSTACLE_LENGTH)+500/2),(int) (zScaleFactor*OBSTACLE_LENGTH), (int)(zScaleFactor*OBSTACLE_LENGTH));
					if(obstacles.get(i) instanceof Powerup)
					{
						g.setColor(new Color(75,0,75));
						zScaleFactor/=0.95;
						g.fillRect((int)(zScaleFactor*(obstacles.get(i).getX()-OBSTACLE_LENGTH)+700/2), (int)(zScaleFactor*(obstacles.get(i).getY()-OBSTACLE_LENGTH)+500/2),(int) (zScaleFactor*OBSTACLE_LENGTH), (int)(zScaleFactor*OBSTACLE_LENGTH));
						g.setColor(new Color(50,0,100));
						g.fillOval((int)(zScaleFactor*(obstacles.get(i).getX()-OBSTACLE_LENGTH)+700/2), (int)(zScaleFactor*(obstacles.get(i).getY()-OBSTACLE_LENGTH)+500/2),(int) (zScaleFactor*OBSTACLE_LENGTH), (int)(zScaleFactor*OBSTACLE_LENGTH));
					}
					else
					{
						g.setColor(Color.getHSBColor(0.02f,(float)(0.9),(float)zScaleFactor));
						zScaleFactor/=0.95;
						g.fillRect((int)(zScaleFactor*(obstacles.get(i).getX()-OBSTACLE_LENGTH)+700/2), (int)(zScaleFactor*(obstacles.get(i).getY()-OBSTACLE_LENGTH)+500/2),(int) (zScaleFactor*OBSTACLE_LENGTH), (int)(zScaleFactor*OBSTACLE_LENGTH));
					}
					if(intersects(yourPos,obstacles.get(i)))
					{
						if(obstacles.get(i) instanceof Powerup)
						{
							connection.send("powerup");
						}
						else
							connection.send("crashing");
						crashing=true;
					}
					else if(crashing)
						try {
							connection.send("doneCrashing");
							crashing=false;
							g.setColor(YOU_OK_COLOR);
						}
					catch (IllegalStateException e)
					{
						//repainter.interrupt();
					}
				}
			}
		}
		/**
		 * Determines if the shapes represented by two squares intersect.
		 * @param ship the player Point
		 * @param obst the obstacles Point
		 * @return true if intersecting, false if otherwise
		 */
		private boolean intersects(Point ship, Point obst)
		{
			if((ship.getZ()>=obst.getZ()-50 && ship.getZ()<obst.getZ())==false)
				return false;
			Point shipHolder=new Point(ship.getX(),ship.getY(),ship.getZ());

			Point obstLowerRight=new Point(obst.getX()+700/2,obst.getY()+500/2,obst.getZ());
			Point obstUpperLeft=new Point(obstLowerRight.getX()-OBSTACLE_LENGTH,obstLowerRight.getY()-OBSTACLE_LENGTH,obstLowerRight.getZ());
			if(shipHolder.getX()>obstUpperLeft.getX() && shipHolder.getX()<obstLowerRight.getX() && shipHolder.getY()>obstUpperLeft.getY() && shipHolder.getY()<obstLowerRight.getY())
				return true;
			shipHolder=new Point(ship.getX(),ship.getY()-PLAYER_LENGTH,ship.getZ());
			if(shipHolder.getX()>obstUpperLeft.getX() && shipHolder.getX()<obstLowerRight.getX() && shipHolder.getY()>obstUpperLeft.getY() && shipHolder.getY()<obstLowerRight.getY())
				return true;
			shipHolder=new Point(ship.getX()-PLAYER_LENGTH,ship.getY()-PLAYER_LENGTH,ship.getZ());
			if(shipHolder.getX()>obstUpperLeft.getX() && shipHolder.getX()<obstLowerRight.getX() && shipHolder.getY()>obstUpperLeft.getY() && shipHolder.getY()<obstLowerRight.getY())
				return true;
			shipHolder=new Point(ship.getX()-PLAYER_LENGTH,ship.getY(),ship.getZ());
			if(shipHolder.getX()>obstUpperLeft.getX() && shipHolder.getX()<obstLowerRight.getX() && shipHolder.getY()>obstUpperLeft.getY() && shipHolder.getY()<obstLowerRight.getY())
				return true;

			return false;
		}
		@Override
		public void keyPressed(KeyEvent evt) {
			int e=evt.getKeyCode();
			if(e==KeyEvent.VK_DOWN && (yourPos.getY()<500))
			{
				connection.send("down");
			}
			else if(e==KeyEvent.VK_UP && (yourPos.getY()-PLAYER_LENGTH>0))
			{
				connection.send("up");
			}
			else if(e==KeyEvent.VK_RIGHT && (yourPos.getX()<700))
			{
				connection.send("right");
			}
			else if(e==KeyEvent.VK_LEFT && (yourPos.getX()-PLAYER_LENGTH>0))
			{
				connection.send("left");
			}

			repaint();
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
		}

	}


	/**
	 * A PokerClient is a netgame client that handles communication
	 * with the PokerHub.  It is used by the PokerWindow class to
	 * send messages to the hub.  When messages are received from
	 * the hub, it takes an appropriate action.
	 */
	private class PokerClient extends Client {
		/**
		 * Connect to a PokerHub at a specified hostname and port number.
		 */
		public PokerClient(String hubHostName, int hubPort) throws IOException {
			super(hubHostName, hubPort);
		}

		/**
		 * This method is called when a message from the hub is received 
		 * by this client.  If the message is of type PokerGameState,
		 * then the newState() method in the PokerWindow class is called
		 * to handle the change in the state of the game.  If the message
		 * is of type String, it represents a message that is to be
		 * displayed to the user; the string is displayed in the JLabel
		 * messageFromServer.  If the message is of type PokerCard[],
		 * then it is the opponent's hand.  This had is sent when the
		 * game has ended and the player gets to see the opponent's hand.
		 * <p>Note that this method is called from a separate thread, not
		 * from the GUI event thread.  In order to avoid synchronization
		 * issues, this method uses SwingUtilties.invokeLater() to carry 
		 * out its task in the GUI event thread.
		 */
		protected void messageReceived(final Object message) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if(message instanceof SkyGameState)
						newState((SkyGameState)message);
					else if(((String)message).equals("Ready to start the first game!"))
					{
						start=true;
					}
					else if(((String)message).equals("winner"))
						winner=true;
					else if(((String)message).equals("loser"))
						loser=true;
				}
			});
		}

		/**
		 * This method is called when the hub shuts down.  That is a signal
		 * that the opposing player has quit the game.  The user is informed
		 * of this, and the program is terminated.
		 */
		protected void serverShutdown(String message) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(SkyFrame.this,
							"Your opponent has quit.\nThe game is over.");
					System.exit(0);
				}
			});
		}

	} // end nested class PokerClient


	/**
	 * The display class defines a JPanel that is used as the content
	 * pane for the SkyFrame.
	 */
	private class Display extends JPanel {

		final Color silver = new Color(117,117,117);

		/**
		 * The constructor creates labels,  buttons, and a text field and adds
		 * them to the panel.  An action listener of type ButtonHandler is created
		 * and is added to all the buttons and the text field.
		 */
		Display() {
			setLayout(null);  // Layout will be done by hand.
			setPreferredSize(new Dimension(600,300));
			setBackground(Color.BLACK);
			setBorder(BorderFactory.createLineBorder(silver, 3));
			messageFromServer = makeLabel(30,205,500,25,16,silver);
			messageFromServer.setText("GROUND CONTROL: Waiting for connection");
		}

		/**
		 * Utility routine used by constructor to make a label and add it to the
		 * panel.  The label has specified bounds, font size, and color, and its
		 * text is initially empty.
		 */
		JLabel makeLabel(int x, int y, int width, int height, int fontSize, Color color) {
			JLabel label = new JLabel();
			add(label);
			label.setBounds(x,y,width,height);
			label.setOpaque(false);
			label.setForeground(color);
			label.setFont(new Font("Serif", Font.BOLD, fontSize));
			return label;
		}

		/**
		 * The paint component just draws the cards, when appropriate.  The remaining
		 * content of the panel consists of sub-components (labels, buttons, text field).
		 */
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
		}

	} // end nested class Display


	// ------------------- Private member variables and methods ---------------------------


	private PokerClient connection;   // Handles communication with the PokerHub; used to send messages to the hub.

	private Point yourPos;
	private Point opponentPos;
	private boolean host;
	private JLabel messageFromServer;
	boolean start;
	private ArrayList<Point> obstacles;
	private ArrayList<Dimension> stars;
	private boolean receivedObstacles;
	private boolean crashing;
	private boolean winner;
	private boolean loser;
	private int numObstacles;

	private Point winPoint;

	AudioPlayer player;
	AudioStream stream;

	final static private int PLAYER_LENGTH=35;
	final static private int OBSTACLE_LENGTH=400;
	final static private double RENDER_DISTANCE=2001;

	final Color YOU_OK_COLOR=Color.WHITE;
	final Color OPP_OK_COLOR=Color.GRAY;

	private Thread winThread;

	private Display display;          // The content pane of the window, defined by the inner class, Display.

	/**
	 * This method is called when a new SkyGameState is received from the SkyHub.
	 * It changes the GUI and the window's state to match the new game state.  The
	 * new state is also stored in the instance variable named state.
	 */
	private void newState(SkyGameState state) {
		numObstacles=state.numObstacles;
		if(!receivedObstacles)
		{
			this.obstacles=state.obstacles;
			receivedObstacles=true;
		}
		if(host)
		{
			yourPos=state.getHosterPos();
			opponentPos=state.getJoinerPos();
		}
		else
		{
			yourPos=state.getJoinerPos();
			opponentPos=state.getHosterPos();
		}

		repaint();

	} // end newState()


	/**
	 * This method is called when the user clicks the "QUIT"
	 * button or closed the window.  The client disconnects
	 * from the server before terminating the program.  
	 * This will be seen by the Hub, which will inform the 
	 * other player's program (if any), so that that program 
	 * can also terminate.
	 */
	private void doQuit() {
		dispose(); // Close the window.
		if (connection != null) {
			connection.disconnect();
			try { // time for the disconnect message to be sent.
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
			}
		}
		System.exit(0);
	}

} // end class SkyFrame

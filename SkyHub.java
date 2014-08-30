import java.io.IOException;
import java.util.ArrayList;

/**
 * A SkyHub manages a networked game of Hunting Jupiter
 * between two players.  Each player is using a SkyFrame
 * as the GUI for the game.  The Hub keeps track of the full state
 * of the game, and it sends messages of type SkyGameState to
 * each player reflecting what that player needs to know about
 * the game state.  It also sends strings as messages. Finally, when a game ends because one player
 * reached the end, an String is sent to each player
 * ending the game.  
 * <p>The first message is sent when the second player connects.  At 
 * that time, the Hub sends the initial PokerGameState to both players.
 * The game begins after three seconds.
 * (See the reset() and setAutoreset() methods in the Hub class for
 * information about this issue.)
 */

public class SkyHub extends Hub {

	SkyGameState state;
	int increment1;
	int increment2;
	Thread sender;
	boolean obstaclesSent;
	int numObstacles;

	/**
	 * Creates a SkyHub listening on a specified port with a certain number of obstacles.
	 */
	public SkyHub(int port, int numObstacles) throws IOException {
		super(port);
		setAutoreset(true);
		this.numObstacles=numObstacles;
		obstaclesSent=false;
		increment1=50;
		increment2=50;
		state=new SkyGameState(makeObstacles(),new Point(700/3,500/2,0),new Point(2*700/3,500/2,0),numObstacles);
		sender=new Thread() {
			public void run()
			{
				try{sleep(3000L);}catch(Exception e){};
				while(true)
				{
					sendState();
					try{sleep(50L);}catch(Exception e){};
				}
			}
		};
	}

	/**
	 * When the second player connects, this method starts the game by
	 * sending the initial game state to the two players. This method also shuts down the Hub's 
	 * ServerSocket so that no further players can connect.
	 */
	protected void playerConnected(int playerID) {
		if (playerID == 2) {
			shutdownServerSocket();
			sendToAll(state);
			sendToAll("Ready to start the first game!");
		}
	}
	/**
	 * Randomly generates a series of numObstacles obstacles.  The Point generated represents
	 * the lower-right corner.
	 */
	protected ArrayList<Point> makeObstacles()
	{
		ArrayList<Point> points=new ArrayList<Point>();
		for(int i=0;i<numObstacles;i++)
		{
			if(Math.random()<0.95)
				points.add(new Point((int)(Math.random()*700)-700/2+200,(int)(Math.random()*500)-500/2+200,i*10000/15+1));
			else
				points.add(new Powerup((int)(Math.random()*700)-700/2+200,(int)(Math.random()*500)-500/2+200,i*10000/15+1));
		}
		return points;
	}


	/**
	 * If a player disconnects, the game ends.  This method shuts down
	 * the Hub, which will send a signal to the remaining connected player,
	 * if any, to let them know that their opponent has left the game.
	 * The client will respond by terminating that player's program.
	 */
	protected void playerDisconnected(int playerID) {
		shutDownHub();
	}

	/**
	 * This is the method that responds to messages received from the
	 * clients.  It handles all of the action of the game.  When a message
	 * is received, this method will make any changes to the state of
	 * the game that are triggered by the message.
	 */
	protected void messageReceived(int playerID, Object message) {
		if(message.equals("starting"))
			sender.start();
		else if(message.equals("right"))
		{
			if(playerID==1)
				state.hosterPos.setX(state.getHosterPos().getX()+10);
			else
				state.joinerPos.setX(state.getJoinerPos().getX()+10);
		}
		else if(message.equals("left"))
		{
			if(playerID==1)
				state.hosterPos.setX(state.getHosterPos().getX()-10);
			else
				state.joinerPos.setX(state.getJoinerPos().getX()-10);
		}
		else if(message.equals("up"))
		{
			if(playerID==1)
				state.hosterPos.setY(state.getHosterPos().getY()-10);
			else
				state.joinerPos.setY(state.getJoinerPos().getY()-10);
		}
		else if(message.equals("down"))
		{
			if(playerID==1)
				state.hosterPos.setY(state.getHosterPos().getY()+10);
			else
				state.joinerPos.setY(state.getJoinerPos().getY()+10);
		}
		else if(message.equals("crashing"))
		{
			if(playerID==1)
				increment1=1;
			else
				increment2=1;
		}
		else if(message.equals("doneCrashing"))
		{
			if(playerID==1)
				increment1=50;
			else
				increment2=50;
		}
		else if (message.equals("done"))
		{
			increment1=0;
			increment2=0;
			if(playerID==1)
			{
				sendToOne(1,"winner");
				sendToOne(2,"loser");
			}
			else
			{
				sendToOne(2,"winner");
				sendToOne(1,"loser");
			}
		}
		else if (message.equals("powerup"))
		{
			if(playerID==1)
				increment1=500;
			else
				increment2=500;
		}
	}
	// --- The remaining methods are called by messageReceived() to do some of its processing ---

	/**
	 * This method is used by messageReceived() to send state messages to both
	 * players.  
	 */
	private void sendState() {

		if(obstaclesSent)
		{
			state.obstacles=null;
		}
		state.hosterPos.setZ(state.hosterPos.getZ()+increment1);//update z positions
		state.joinerPos.setZ(state.joinerPos.getZ()+increment2);
		sendToAll(state);
		obstaclesSent=true;
	}

}
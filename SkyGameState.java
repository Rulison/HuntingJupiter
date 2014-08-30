import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents the state of a game of Hunting Jupiter.  The full state of a game
 * is kept by a SkyHub.  That hub sends messages of type
 * SkyGameState to each player to update the game state.  Note that the two players receive
 * the same messages.
 */
public class SkyGameState implements Serializable {

	ArrayList<Point> obstacles;
	Point hosterPos;
	Point joinerPos;
	int numObstacles;

	/**
	 * Create a SkyGameState object according to parameters.
	 */
	public SkyGameState(ArrayList<Point> obstacles, Point hosterPos, Point joinerPos) {
		this.obstacles=obstacles;
		this.hosterPos=hosterPos;
		this.joinerPos=joinerPos;
	}
	public SkyGameState(ArrayList<Point> obstacles, Point hosterPos, Point joinerPos, int num) {
		this.obstacles=obstacles;
		this.hosterPos=hosterPos;
		this.joinerPos=joinerPos;
		numObstacles=num;
	}
	public Point getHosterPos()
	{
		return hosterPos;
	}
	public Point getJoinerPos()
	{
		return joinerPos;
	}
	public String toString()
	{
		return "hosterPos: "+hosterPos+" joinerPos: "+joinerPos;
	}


}
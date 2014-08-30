import java.io.Serializable;
/**
 * Represents an item to be drawn in 3D space.
 *
 */
public class Point implements Serializable{

	private int x;
	private int y;
	private int z;
	public Point(int x,int y, int z)
	{
		this.x=x;
		this.y=y;
		this.z=z;
	}
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
	public int getZ()
	{
		return z;
	}
	public void setX(int x)
	{
		this.x=x;
	}
	public void setY(int y)
	{
		this.y=y;
	}
	public void setZ(int z)
	{
		this.z=z;
	}
	public String toString()
	{
		return "X: "+x+" Y: "+y+" Z: "+z;
	}

}

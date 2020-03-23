package Extensions.fake.roboto.model;

public class WalkNode
{
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _stayIterations;
	
	public WalkNode(int x, int y, int z, int stayIterations)
	{
		_x = x;
		_y = y;
		_z = z;
		_stayIterations = stayIterations;
	}
	
	public int getX()
	{
		return _x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public int getZ()
	{
		return _z;
	}
	
	public int getStayIterations()
	{
		return _stayIterations;
	}
}

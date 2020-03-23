package Extensions.RankSystem;

public class Rank
{
	private int _id = 0; // rank id
	private String _name = null; // rank name
	private long _minExp = 0; // for rich this rank
	private int _pointsForKill = 0; // points awarded for kill the player with this rank
	
	private long _rpc = 0; // RPC awarded for kill the player with this rank
	
	private int _nickColor = -1; // nick color, colors will be override in EnterWorld class if the value will be greater than -1
	private int _titleColor = -1; // title color, colors will be override in EnterWorld class if the value will be greater than -1
	
	public int getId()
	{
		return _id;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public long getMinExp()
	{
		return _minExp;
	}
	
	public void setMinPoints(long minExp)
	{
		_minExp = minExp;
	}
	
	public int getPointsForKill()
	{
		return _pointsForKill;
	}
	
	public void setPointsForKill(int pointsForKill)
	{
		_pointsForKill = pointsForKill;
	}
	
	public int getNickColor()
	{
		return _nickColor;
	}
	
	public void setNickColor(int nickColor)
	{
		_nickColor = nickColor;
	}
	
	public int getTitleColor()
	{
		return _titleColor;
	}
	
	public void setTitleColor(int titleColor)
	{
		_titleColor = titleColor;
	}
	
	public long getRpc()
	{
		return _rpc;
	}
	
	public void setRpc(long rpc)
	{
		_rpc = rpc;
	}
}
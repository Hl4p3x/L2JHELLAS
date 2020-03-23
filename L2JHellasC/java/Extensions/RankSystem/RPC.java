package Extensions.RankSystem;

public class RPC
{
	private int _playerId = 0; // player id
	private long _rpcTotal = 0; // total RPC
	private long _rpcCurrent = 0; // current RPC
	
	private byte _dbStatus = DBStatus.NONE; // if NONE it is mean, the values are not changed and the database update is not required.
	
	public RPC()
	{
	}
	
	public RPC(int playerId)
	{
		_playerId = playerId;
	}
	
	public int getPlayerId()
	{
		return _playerId;
	}
	
	public void setPlayerId(int playerId)
	{
		_playerId = playerId;
	}
	
	public long getRpcTotal()
	{
		return _rpcTotal;
	}
	
	public void setRpcTotal(long rpcTotal)
	{
		_rpcTotal = rpcTotal;
	}
	
	public long getRpcCurrent()
	{
		return _rpcCurrent;
	}
	
	public void setRpcCurrent(long rpcCurrent)
	{
		_rpcCurrent = rpcCurrent;
	}
	
	public long decreaseRpcCurrentBy(long value)
	{
		_rpcCurrent -= value;
		
		if (_dbStatus != DBStatus.INSERTED)
			_dbStatus = DBStatus.UPDATED;
		
		return _rpcCurrent;
	}
	
	public void increaseRpcBy(long value)
	{
		_rpcCurrent += value;
		_rpcTotal += value;
		
		if (_dbStatus != DBStatus.INSERTED)
			_dbStatus = DBStatus.UPDATED;
	}
	
	public byte getDbStatus()
	{
		return _dbStatus;
	}
	
	public void setDbStatus(byte dbStatus)
	{
		_dbStatus = dbStatus;
	}
}
package com.l2jhellas.gameserver.enums;

public enum FenceState
{
	HIDDEN(0),
	OPENED(1),
	CLOSED(2),
	CLOSED_HIDDEN(0);
	
	final int _clientId;
	
	private FenceState(int clientId)
	{
		_clientId = clientId;
	}
	
	public int getClientId()
	{
		return _clientId;
	}
	
	public static int getFenceStateCount()
	{
		return values().length;
	}
	
	public boolean isGeodataEnabled()
	{
		return (this == CLOSED_HIDDEN) || (this == CLOSED);
	}
}
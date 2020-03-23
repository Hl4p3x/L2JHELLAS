package com.l2jhellas.gameserver.model;

public class L2NpcWalkerNode
{
	private final int _routeId;
	private String _chatText;
	private final int _moveX, _moveY, _moveZ;
	private final int _delay;
	private final boolean _running;
	
	public L2NpcWalkerNode(int routeId, int moveX, int moveY, int moveZ, boolean running, int delay, String chatText)
	{
		super();
		_routeId = routeId;
		_chatText = chatText;
		
		if (_chatText.trim().isEmpty())
			_chatText = null;
		
		_moveX = moveX;
		_moveY = moveY;
		_moveZ = moveZ;
		_running = running;
		_delay = delay;
	}
	
	public int getRouteId()
	{
		return _routeId;
	}
	
	public String getChatText()
	{
		return _chatText;
	}
	
	public int getMoveX()
	{
		return _moveX;
	}
	
	public int getMoveY()
	{
		return _moveY;
	}
	
	public int getMoveZ()
	{
		return _moveZ;
	}
	
	public int getDelay()
	{
		return _delay;
	}
	
	public boolean getRunning()
	{
		return _running;
	}
}
package com.l2jhellas.gameserver;

public class RestartVoteVariable
{
	public int _voteCountRestart = 0;
	private int _voteCount = 0;
	
	public int getVoteCount(String name)
	{
		if (name == "restart")
			_voteCount = _voteCountRestart;
		
		return _voteCount;
	}
	
	public void increaseVoteCount(String name)
	{
		if (name == "restart")
			_voteCountRestart = _voteCountRestart + 1;
	}
}
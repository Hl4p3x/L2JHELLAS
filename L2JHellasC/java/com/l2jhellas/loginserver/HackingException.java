package com.l2jhellas.loginserver;

public class HackingException extends Exception
{
	
	private static final long serialVersionUID = 4050762693478463029L;
	String _ip;
	private final int _connects;
	
	public HackingException(String ip, int connects)
	{
		_ip = ip;
		_connects = connects;
	}
	
	public String getIP()
	{
		return _ip;
	}
	
	public int getConnects()
	{
		return _connects;
	}
}
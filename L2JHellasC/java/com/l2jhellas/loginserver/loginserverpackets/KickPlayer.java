package com.l2jhellas.loginserver.loginserverpackets;

import com.l2jhellas.loginserver.serverpackets.ServerBasePacket;

public class KickPlayer extends ServerBasePacket
{
	public KickPlayer(String account)
	{
		writeC(0x04);
		writeS(account);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
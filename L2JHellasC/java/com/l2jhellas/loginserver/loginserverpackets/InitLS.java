package com.l2jhellas.loginserver.loginserverpackets;

import com.l2jhellas.loginserver.LoginServer;
import com.l2jhellas.loginserver.serverpackets.ServerBasePacket;

public class InitLS extends ServerBasePacket
{
	// ID 0x00
	// format
	// d proto rev
	// d key size
	// b key
	
	public InitLS(byte[] publickey)
	{
		writeC(0x00);
		writeD(LoginServer.PROTOCOL_REV);
		writeD(publickey.length);
		writeB(publickey);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
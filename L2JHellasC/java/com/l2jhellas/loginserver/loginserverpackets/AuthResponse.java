package com.l2jhellas.loginserver.loginserverpackets;

import com.l2jhellas.loginserver.GameServerTable;
import com.l2jhellas.loginserver.serverpackets.ServerBasePacket;

public class AuthResponse extends ServerBasePacket
{
	
	public AuthResponse(int serverId)
	{
		writeC(0x02);
		writeC(serverId);
		writeS(GameServerTable.getInstance().getServerNameById(serverId));
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
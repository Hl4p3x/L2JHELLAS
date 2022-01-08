package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.controllers.GameTimeController;

public final class ClientSetTime extends L2GameServerPacket
{
	public static final ClientSetTime STATIC_PACKET = new ClientSetTime();
	
	@Override
	protected void writeImpl()
	{
		writeC(0xEC);
		writeD(GameTimeController.getInstance().getGameTime());
		writeD(6);
	}	
}
package com.l2jhellas.gameserver.network.gameserverpackets;

import java.nio.ByteBuffer;

import com.l2jhellas.gameserver.network.L2GameClient;
import com.l2jhellas.gameserver.network.clientpackets.L2GameClientPacket;

public interface CustomPacketHandlerInterface
{
	
	public L2GameClientPacket handlePacket(ByteBuffer data, L2GameClient client);
}

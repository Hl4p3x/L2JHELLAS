package com.l2jhellas.gameserver.network;

import java.util.Set;

import com.l2jhellas.gameserver.network.L2GameClient.GameClientState;
import com.l2jhellas.mmocore.network.ReceivablePacket;

public interface IPacket
{
	public ReceivablePacket<L2GameClient> getPacket();
	
	public int getPacketId();
	
	public Set<GameClientState> getState();
}
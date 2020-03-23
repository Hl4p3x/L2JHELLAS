package com.l2jhellas.gameserver.network.serverpackets;

public abstract class AbstractServerBasePacket extends L2GameServerPacket
{
	@Override
	abstract public void runImpl();
	
	@Override
	abstract protected void writeImpl();
	
	@Override
	abstract public String getType();
}
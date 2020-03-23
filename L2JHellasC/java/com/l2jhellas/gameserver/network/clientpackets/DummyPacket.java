package com.l2jhellas.gameserver.network.clientpackets;

public final class DummyPacket extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	public void runImpl()
	{
		
	}
	
	@Override
	public String getType()
	{
		return "DummyPacket";
	}
}
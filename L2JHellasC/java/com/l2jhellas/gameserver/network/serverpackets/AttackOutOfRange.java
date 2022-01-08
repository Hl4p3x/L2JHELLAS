package com.l2jhellas.gameserver.network.serverpackets;

public class AttackOutOfRange extends L2GameServerPacket
{
	public static final AttackOutOfRange STATIC_PACKET = new AttackOutOfRange();
	
	private AttackOutOfRange()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x8);
	}
}
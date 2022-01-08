package com.l2jhellas.gameserver.network.serverpackets;

public class AttackinCoolTime extends L2GameServerPacket
{
	public static final AttackinCoolTime STATIC_PACKET = new AttackinCoolTime();
	
	private AttackinCoolTime()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x9);
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

public class AttackDeadTarget extends L2GameServerPacket
{
	public static final AttackDeadTarget STATIC_PACKET = new AttackDeadTarget();
	
	private AttackDeadTarget()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xA);
	}
}
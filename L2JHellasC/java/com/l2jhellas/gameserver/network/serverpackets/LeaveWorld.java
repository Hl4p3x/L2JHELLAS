package com.l2jhellas.gameserver.network.serverpackets;

public class LeaveWorld extends L2GameServerPacket
{
	private static final String _S__96_LEAVEWORLD = "[S] 7e LeaveWorld";
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7e);
	}
	
	@Override
	public String getType()
	{
		return _S__96_LEAVEWORLD;
	}
}
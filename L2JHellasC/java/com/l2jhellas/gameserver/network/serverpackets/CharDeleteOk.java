package com.l2jhellas.gameserver.network.serverpackets;

public class CharDeleteOk extends L2GameServerPacket
{
	private static final String _S__33_CHARDELETEOK = "[S] 23 CharDeleteOk";
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x23);
	}
	
	@Override
	public String getType()
	{
		return _S__33_CHARDELETEOK;
	}
}
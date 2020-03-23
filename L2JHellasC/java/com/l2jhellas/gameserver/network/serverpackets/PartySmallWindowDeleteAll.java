package com.l2jhellas.gameserver.network.serverpackets;

public class PartySmallWindowDeleteAll extends L2GameServerPacket
{
	private static final String _S__65_PARTYSMALLWINDOWDELETEALL = "[S] 50 PartySmallWindowDeleteAll";
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x50);
	}
	
	@Override
	public String getType()
	{
		return _S__65_PARTYSMALLWINDOWDELETEALL;
	}
}
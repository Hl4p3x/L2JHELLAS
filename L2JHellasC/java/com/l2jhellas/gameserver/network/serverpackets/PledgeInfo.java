package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Clan;

public class PledgeInfo extends L2GameServerPacket
{
	private static final String _S__9C_PLEDGEINFO = "[S] 9C PledgeInfo";
	private final L2Clan _clan;
	
	public PledgeInfo(L2Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x83);
		writeD(_clan.getClanId());
		writeS(_clan.getName());
		writeS(_clan.getAllyName());
	}
	
	@Override
	public String getType()
	{
		return _S__9C_PLEDGEINFO;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Clan;

public class PledgeStatusChanged extends L2GameServerPacket
{
	private static final String _S__CD_PLEDGESTATUS_CHANGED = "[S] CD PledgeStatusChanged";
	private final L2Clan _clan;
	
	public PledgeStatusChanged(L2Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xcd);
		writeD(_clan.getLeaderId());
		writeD(_clan.getClanId());
		writeD(_clan.getCrestId());
		writeD(_clan.getAllyId());
		writeD(_clan.getAllyCrestId());
		writeD(0);
		writeD(0);
	}
	
	@Override
	public String getType()
	{
		return _S__CD_PLEDGESTATUS_CHANGED;
	}
}
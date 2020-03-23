package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Clan;

public class PledgeShowInfoUpdate extends L2GameServerPacket
{
	private static final String _S__A1_PLEDGESHOWINFOUPDATE = "[S] 88 PledgeShowInfoUpdate";
	private final L2Clan _clan;
	
	public PledgeShowInfoUpdate(L2Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		// ddddddddddSdd
		writeC(0x88);
		writeD(_clan.getClanId());
		writeD(_clan.getCrestId());
		writeD(_clan.getLevel());
		writeD(_clan.hasCastle());
		writeD(_clan.hasHideout());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		
		writeD(0);
		writeD(0);
		
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
		writeD(_clan.isAtWar() ? 1 : 0);
	}
	
	@Override
	public String getType()
	{
		return _S__A1_PLEDGESHOWINFOUPDATE;
	}
}
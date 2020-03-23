package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
	// private static Logger _log = Logger.getLogger(PledgeShowMemberListAll.class.getName());
	private static final String _S__68_PLEDGESHOWMEMBERLISTALL = "[S] 53 PledgeShowMemberListAll";
	
	private final L2Clan _clan;
	private final int _pledgeType;
	private final String _pledgeName;
	
	public PledgeShowMemberListAll(L2Clan clan, int pledgeType)
	{
		_clan = clan;
		_pledgeType = pledgeType;
		
		if (_pledgeType == 0)
		{
			_pledgeName = clan.getName();
		}
		else if (_clan.getSubPledge(_pledgeType) != null)
		{
			_pledgeName = _clan.getSubPledge(_pledgeType).getName();
		}
		else
		{
			_pledgeName = "";
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x53);
		
		writeD((_pledgeType == 0) ? 0 : 1);
		writeD(_clan.getClanId());
		writeD(_pledgeType);
		writeS(_pledgeName);
		writeS(_clan.getSubPledge(_pledgeType) != null ? _clan.getSubPledge(_pledgeType).getLeaderName() : _clan.getLeaderName());
		
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
		writeD(_clan.getSubPledgeMembersCount(_pledgeType));
		
		for (L2ClanMember m : _clan.getMembers())
		{
			if (m.getPledgeType() != _pledgeType)
				continue;
			
			writeS(m.getName());
			writeD(m.getLevel());
			writeD(m.getClassId());
			
			L2PcInstance player = m.getPlayerInstance();
			
			if (player != null)
			{
				writeD(player.getAppearance().getSex().ordinal());
				writeD(player.getRace().ordinal());
			}
			else
			{
				writeD(0x01);
				writeD(0x01);
			}
			
			writeD((m.isOnline()) ? m.getObjectId() : 0);
			writeD((m.getSponsor() != 0 || m.getApprentice() != 0) ? 1 : 0);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__68_PLEDGESHOWMEMBERLISTALL;
	}
}
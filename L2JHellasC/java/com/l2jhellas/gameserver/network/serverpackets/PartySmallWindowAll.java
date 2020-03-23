package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PartySmallWindowAll extends L2GameServerPacket
{
	private static final String _S__63_PARTYSMALLWINDOWALL = "[S] 4e PartySmallWindowAll";
	private final L2Party _party;
	private final L2PcInstance _exclude;
	
	public PartySmallWindowAll(L2PcInstance exclude, L2Party party)
	{
		_exclude = exclude;
		_party = party;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4e);
		writeD(_party.getPartyLeaderOID());
		writeD(_party.getDistributionType().getId());
		writeD(_party.getMemberCount() - 1);
		
		for (L2PcInstance member : _party.getPartyMembers())
		{
			if (member != null && member != _exclude)
			{
				writeD(member.getObjectId());
				writeS(member.getName());
				
				writeD((int) member.getCurrentCp()); // c4
				writeD(member.getMaxCp()); // c4
				
				writeD((int) member.getCurrentHp());
				writeD(member.getMaxHp());
				writeD((int) member.getCurrentMp());
				writeD(member.getMaxMp());
				writeD(member.getLevel());
				writeD(member.getClassId().getId());
				writeD(0);// writeD(0x01); ??
				writeD(member.getRace().ordinal());
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _S__63_PARTYSMALLWINDOWALL;
	}
}
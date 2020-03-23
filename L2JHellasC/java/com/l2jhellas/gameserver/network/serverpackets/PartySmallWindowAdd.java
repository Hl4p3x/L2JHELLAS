package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PartySmallWindowAdd extends L2GameServerPacket
{
	private static final String _S__64_PARTYSMALLWINDOWADD = "[S] 4f PartySmallWindowAdd";
	
	private final L2PcInstance _member;
	private final L2Party _party;
	
	public PartySmallWindowAdd(L2PcInstance member, L2Party party)
	{
		_member = member;
		_party = party;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4f);
		writeD(_party.getPartyLeaderOID()); // c3
		writeD(_party.getDistributionType().getId()); // c3
		writeD(_member.getObjectId());
		writeS(_member.getName());
		writeD((int) _member.getCurrentCp()); // c4
		writeD(_member.getMaxCp()); // c4
		writeD((int) _member.getCurrentHp());
		writeD(_member.getMaxHp());
		writeD((int) _member.getCurrentMp());
		writeD(_member.getMaxMp());
		writeD(_member.getLevel());
		writeD(_member.getClassId().getId());
		writeD(0);// writeD(0x01); ??
		writeD(0);
	}
	
	@Override
	public String getType()
	{
		return _S__64_PARTYSMALLWINDOWADD;
	}
}
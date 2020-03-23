package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.group.party.L2Party;

public class ExMPCCPartyInfoUpdate extends L2GameServerPacket
{
	private final String _name;
	private final int _leaderObjectId;
	private final int _membersCount;
	private final int _mode;

	public ExMPCCPartyInfoUpdate(L2Party party, int mode)
	{
		_name = party.getLeader().getName();
		_leaderObjectId = party.getLeader().getObjectId();
		_membersCount = party.getMemberCount();
		_mode = mode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x5A);
		writeS(_name);
		writeD(_leaderObjectId);
		writeD(_membersCount);
		writeD(_mode);
	}
}
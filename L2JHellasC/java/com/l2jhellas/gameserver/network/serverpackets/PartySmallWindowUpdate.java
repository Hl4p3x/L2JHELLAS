package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PartySmallWindowUpdate extends L2GameServerPacket
{
	private static final String _S__67_PARTYSMALLWINDOWUPDATE = "[S] 52 PartySmallWindowUpdate";
	private final L2PcInstance _member;
	
	public PartySmallWindowUpdate(L2PcInstance member)
	{
		_member = member;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x52);
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
		
	}
	
	@Override
	public String getType()
	{
		return _S__67_PARTYSMALLWINDOWUPDATE;
	}
}
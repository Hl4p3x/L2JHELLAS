package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PartySmallWindowDelete extends L2GameServerPacket
{
	private static final String _S__66_PARTYSMALLWINDOWDELETE = "[S] 51 PartySmallWindowDelete";
	private final L2PcInstance _member;
	
	public PartySmallWindowDelete(L2PcInstance member)
	{
		_member = member;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x51);
		writeD(_member.getObjectId());
		writeS(_member.getName());
	}
	
	@Override
	public String getType()
	{
		return _S__66_PARTYSMALLWINDOWDELETE;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
	private static final String _S__FE_4A_EXMPCCSHOWPARTYMEMBERINFO = "[S] FE:4A ExMPCCShowPartyMemberInfo";
	private final L2Party _party;
	
	public ExMPCCShowPartyMemberInfo(L2Party party)
	{
		_party = party;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4a);
		
		writeD(_party.getMemberCount());
		
		for (L2PcInstance member : _party.getPartyMembers())
		{
			if (member == null)
				continue;
			
			writeS(member.getName());
			writeD(member.getObjectId());
			writeD(member.getClassId().getId());
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_4A_EXMPCCSHOWPARTYMEMBERINFO;
	}
}
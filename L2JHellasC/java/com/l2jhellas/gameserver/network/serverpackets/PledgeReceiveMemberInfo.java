package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2ClanMember;

public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
	private static final String _S__FE_3D_PLEDGERECEIVEMEMBERINFO = "[S] FE:3D PledgeReceiveMemberInfo";
	private final L2ClanMember _member;
	
	public PledgeReceiveMemberInfo(L2ClanMember member)
	{
		_member = member;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3d);
		
		writeD(_member.getPledgeType());
		writeS(_member.getName());
		writeS(_member.getTitle()); // title
		writeD(_member.getPowerGrade()); // power
		
		// clan or subpledge name
		if (_member.getPledgeType() != 0)
		{
			writeS((_member.getClan().getSubPledge(_member.getPledgeType())).getName());
		}
		else
			writeS(_member.getClan().getName());
		
		writeS(_member.getApprenticeOrSponsorName()); // name of this member's apprentice/sponsor
	}
	
	@Override
	public String getType()
	{
		return _S__FE_3D_PLEDGERECEIVEMEMBERINFO;
	}
}
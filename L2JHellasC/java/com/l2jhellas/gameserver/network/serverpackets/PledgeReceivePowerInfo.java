package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2ClanMember;

public class PledgeReceivePowerInfo extends L2GameServerPacket
{
	private static final String _S__FE_3C_PLEDGERECEIVEPOWERINFO = "[S] FE:3D PledgeReceivePowerInfo";
	private final L2ClanMember _member;
	
	public PledgeReceivePowerInfo(L2ClanMember member)
	{
		_member = member;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3c);
		
		writeD(_member.getPowerGrade()); // power grade
		writeS(_member.getName());
		writeD(_member.getClan().getRankPrivs(_member.getPowerGrade())); // privileges
	}
	
	@Override
	public String getType()
	{
		return _S__FE_3C_PLEDGERECEIVEPOWERINFO;
	}
}
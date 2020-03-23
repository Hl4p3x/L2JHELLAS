package com.l2jhellas.gameserver.network.serverpackets;

public class PledgeShowMemberListDeleteAll extends L2GameServerPacket
{
	private static final String _S__9B_PLEDGESHOWMEMBERLISTDELETEALL = "[S] 82 PledgeShowMemberListDeleteAll";
	
	public PledgeShowMemberListDeleteAll()
	{
		
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x82);
	}
	
	@Override
	public String getType()
	{
		return _S__9B_PLEDGESHOWMEMBERLISTDELETEALL;
	}
}
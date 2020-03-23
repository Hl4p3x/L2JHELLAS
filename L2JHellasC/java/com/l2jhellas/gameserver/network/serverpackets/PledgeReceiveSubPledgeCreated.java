package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Clan.SubPledge;

public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket
{
	private static final String _S__FE_3F_PLEDGERECEIVESUBPLEDGECREATED = "[S] FE:3F PledgeReceiveSubPledgeCreated";
	private final SubPledge _subPledge;
	
	public PledgeReceiveSubPledgeCreated(SubPledge subPledge)
	{
		_subPledge = subPledge;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3f);
		
		writeD(0x01);
		writeD(_subPledge.getId());
		writeS(_subPledge.getName());
		writeS(_subPledge.getLeaderName());
	}
	
	@Override
	public String getType()
	{
		return _S__FE_3F_PLEDGERECEIVESUBPLEDGECREATED;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Clan;

public class ManagePledgePower extends L2GameServerPacket
{
	private static final String _S__30_MANAGEPLEDGEPOWER = "[S] 30 ManagePledgePower";
	
	private final int _action;
	private final L2Clan _clan;
	private final int _rank;
	
	public ManagePledgePower(L2Clan clan, int action, int rank)
	{
		_clan = clan;
		_action = action;
		_rank = rank;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x30);
		writeD(_rank);
		writeD(_action);
		writeD(_clan.getRankPrivs(_rank));
	}
	
	@Override
	public String getType()
	{
		return _S__30_MANAGEPLEDGEPOWER;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Summon;

public class PetStatusShow extends L2GameServerPacket
{
	private static final String _S__C9_PETSTATUSSHOW = "[S] B0 PetStatusShow";
	private final int _summonType;
	
	public PetStatusShow(L2Summon summon)
	{
		_summonType = summon.getSummonType();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xB0);
		writeD(_summonType);
	}
	
	@Override
	public String getType()
	{
		return _S__C9_PETSTATUSSHOW;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.enums.player.CafePointType;

public class ExPCCafePointInfo extends L2GameServerPacket
{
	private static final String _S__FE_31_EXPCCAFEPOINTINFO = "[S] FE:31 ExPCCafePointInfo";
	private final int _score, _modify, _periodType, _remainingTime;
	private CafePointType _pointType;
	
	public ExPCCafePointInfo(int score, int modify, CafePointType type)
	{
		_score = score;
		_modify = modify;
		_remainingTime = 0;
		_pointType = type;
		_periodType = 1; // get point time
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x31);
		writeD(_score);
		writeD(_modify);
		writeC(_periodType);
		writeD(_remainingTime);
		writeC(_pointType.ordinal());
	}
	
	@Override
	public String getType()
	{
		return _S__FE_31_EXPCCAFEPOINTINFO;
	}
}
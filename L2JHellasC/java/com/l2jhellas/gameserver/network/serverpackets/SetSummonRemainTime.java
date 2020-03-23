package com.l2jhellas.gameserver.network.serverpackets;

public class SetSummonRemainTime extends L2GameServerPacket
{
	private static final String _S__D1_SET_SUMMON_REMAIN_TIME = "[S] d1 SetSummonRemainTime";
	private final int _maxTime;
	private final int _remainingTime;
	
	public SetSummonRemainTime(int maxTime, int remainingTime)
	{
		_remainingTime = remainingTime;
		_maxTime = maxTime;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xd1);
		writeD(_maxTime);
		writeD(_remainingTime);
	}
	
	@Override
	public String getType()
	{
		return _S__D1_SET_SUMMON_REMAIN_TIME;
	}
}
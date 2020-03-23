package com.l2jhellas.gameserver.network.serverpackets;

public class ShowCalculator extends L2GameServerPacket
{
	private static final String _S__DC_SHOWCALCULATOR = "[S] dc ShowCalculator";
	private final int _calculatorId;
	
	public ShowCalculator(int calculatorId)
	{
		_calculatorId = calculatorId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xdc);
		writeD(_calculatorId);
	}
	
	@Override
	public String getType()
	{
		return _S__DC_SHOWCALCULATOR;
	}
}
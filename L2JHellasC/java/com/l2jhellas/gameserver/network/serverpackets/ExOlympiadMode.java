package com.l2jhellas.gameserver.network.serverpackets;

public class ExOlympiadMode extends L2GameServerPacket
{
	// chc
	private static final String _S__FE_2B_OLYMPIADMODE = "[S] FE:2B ExOlympiadMode";
	private static int _mode;
	
	public ExOlympiadMode(int mode)
	{
		_mode = mode;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2b);
		writeC(_mode);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_2B_OLYMPIADMODE;
	}
}
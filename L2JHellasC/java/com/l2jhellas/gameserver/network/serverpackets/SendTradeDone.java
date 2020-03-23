package com.l2jhellas.gameserver.network.serverpackets;

public class SendTradeDone extends L2GameServerPacket
{
	private static final String _S__32_SENDTRADEDONE = "[S] 22 SendTradeDone";
	private final int _num;
	
	public SendTradeDone(int num)
	{
		_num = num;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x22);
		writeD(_num);
	}
	
	@Override
	public String getType()
	{
		return _S__32_SENDTRADEDONE;
	}
}
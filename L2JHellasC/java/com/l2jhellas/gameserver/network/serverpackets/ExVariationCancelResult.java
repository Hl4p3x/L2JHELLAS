package com.l2jhellas.gameserver.network.serverpackets;

public class ExVariationCancelResult extends L2GameServerPacket
{
	private static final String _S__FE_57_EXVARIATIONCANCELRESULT = "[S] FE:57 ExVariationCancelResult";
	
	private final int _closeWindow;
	private final int _unk1;
	
	public ExVariationCancelResult(int result)
	{
		_closeWindow = 1;
		_unk1 = result;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x57);
		writeD(_closeWindow);
		writeD(_unk1);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_57_EXVARIATIONCANCELRESULT;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

public class ExShowVariationMakeWindow extends L2GameServerPacket
{
	public static final ExShowVariationMakeWindow STATIC_PACKET = new ExShowVariationMakeWindow();
	private static final String _S__FE_50_EXSHOWVARIATIONMAKEWINDOW = "[S] FE:50 ExShowVariationMakeWindow";
	
	public ExShowVariationMakeWindow()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x50);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_50_EXSHOWVARIATIONMAKEWINDOW;
	}
}
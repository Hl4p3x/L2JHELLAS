package com.l2jhellas.gameserver.network.serverpackets;

public class ExShowVariationCancelWindow extends L2GameServerPacket
{
	public static final ExShowVariationCancelWindow STATIC_PACKET = new ExShowVariationCancelWindow();
	private static final String _S__FE_51_EXSHOWVARIATIONCANCELWINDOW = "[S] FE:51 ExShowVariationCancelWindow";
	
	boolean _safety = true;
	
	public ExShowVariationCancelWindow()
	{
		
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x51);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_51_EXSHOWVARIATIONCANCELWINDOW;
	}
}
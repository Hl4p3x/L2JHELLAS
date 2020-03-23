package com.l2jhellas.gameserver.network.serverpackets;

public class ShowPCCafeCouponShowUI extends L2GameServerPacket
{
	private static final String _S__FE_43_SHOWPCCAFECOUPONSHOWUI = "[S] FE:43 ShowPCCafeCouponShowUI";
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x43);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_43_SHOWPCCAFECOUPONSHOWUI;
	}
}
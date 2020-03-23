package com.l2jhellas.gameserver.network.serverpackets;

public class ExShowAdventurerGuideBook extends L2GameServerPacket
{
	private static final String _S__FE_37_EXSHOWADVENTURERGUIDEBOOK = "[S] FE:37 ExShowAdventurerGuideBook";
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x37);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_37_EXSHOWADVENTURERGUIDEBOOK;
	}
}
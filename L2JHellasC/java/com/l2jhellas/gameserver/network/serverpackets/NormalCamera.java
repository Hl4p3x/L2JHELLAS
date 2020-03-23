package com.l2jhellas.gameserver.network.serverpackets;

public class NormalCamera extends L2GameServerPacket
{
	private static final String _S__C8_NORMALCAMERA = "[S] C8 NormalCamera";
	
	public NormalCamera()
	{
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xc8);
	}
	
	@Override
	public String getType()
	{
		return _S__C8_NORMALCAMERA;
	}
}
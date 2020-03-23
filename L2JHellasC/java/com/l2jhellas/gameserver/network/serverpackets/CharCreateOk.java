package com.l2jhellas.gameserver.network.serverpackets;

public class CharCreateOk extends L2GameServerPacket
{
	private static final String _S__25_CHARCREATEOK = "[S] 19 CharCreateOk";
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x19);
		writeD(0x01);
	}
	
	@Override
	public String getType()
	{
		return _S__25_CHARCREATEOK;
	}
}
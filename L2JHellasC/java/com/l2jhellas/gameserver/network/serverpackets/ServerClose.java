package com.l2jhellas.gameserver.network.serverpackets;

public class ServerClose extends L2GameServerPacket
{
	private static final String _S__26_SERVERCLOSE = "[S] 26 ServerClose";
	public static final ServerClose STATIC_PACKET = new ServerClose();
	
	@Override
	protected void writeImpl()
	{
		writeC(0x26);
	}
	
	@Override
	public String getType()
	{
		return _S__26_SERVERCLOSE;
	}
}
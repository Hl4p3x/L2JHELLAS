package com.l2jhellas.gameserver.network.serverpackets;

public final class ActionFailed extends L2GameServerPacket
{
	private static final String _S__35_ACTIONFAILED = "[S] 25 ActionFailed";
	public static final ActionFailed STATIC_PACKET = new ActionFailed();
	
	@Override
	protected void writeImpl()
	{
		writeC(0x25);
	}
	
	@Override
	public String getType()
	{
		return _S__35_ACTIONFAILED;
	}
}
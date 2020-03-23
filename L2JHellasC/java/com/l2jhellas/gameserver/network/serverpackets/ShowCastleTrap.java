package com.l2jhellas.gameserver.network.serverpackets;

public class ShowCastleTrap extends L2GameServerPacket
{
	private static final String _S__CF_ShowCastleTrap = "[S] CF ShowCastleTrap";
	
	private final int _trapId;
	private final int _active;
	
	public ShowCastleTrap(int trapId, int active)
	{
		_trapId = trapId;
		_active = active;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xCF);
		writeD(_trapId);
		writeC(_active);
	}
	
	@Override
	public String getType()
	{
		return _S__CF_ShowCastleTrap;
	}
}
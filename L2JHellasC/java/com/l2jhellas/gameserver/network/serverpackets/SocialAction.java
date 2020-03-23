package com.l2jhellas.gameserver.network.serverpackets;

public class SocialAction extends L2GameServerPacket
{
	private static final String _S__3D_SOCIALACTION = "[S] 2D SocialAction";
	private final int _charObjId;
	private final int _actionId;
	
	public SocialAction(int playerId, int actionId)
	{
		_charObjId = playerId;
		_actionId = actionId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2d);
		writeD(_charObjId);
		writeD(_actionId);
	}
	
	@Override
	public String getType()
	{
		return _S__3D_SOCIALACTION;
	}
}
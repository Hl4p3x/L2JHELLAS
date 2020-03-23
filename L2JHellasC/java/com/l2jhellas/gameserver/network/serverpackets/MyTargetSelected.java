package com.l2jhellas.gameserver.network.serverpackets;

public class MyTargetSelected extends L2GameServerPacket
{
	private static final String _S__BF_MYTARGETSELECTED = "[S] a6 MyTargetSelected";
	private final int _objectId;
	private final int _color;
	
	public MyTargetSelected(int objectId, int color)
	{
		_objectId = objectId;
		_color = color;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xa6);
		writeD(_objectId);
		writeH(_color);
	}
	
	@Override
	public String getType()
	{
		return _S__BF_MYTARGETSELECTED;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

public class TargetSelected extends L2GameServerPacket
{
	private static final String _S__39_TARGETSELECTED = "[S] 29 TargetSelected";
	private final int _objectId;
	private final int _targetObjId;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public TargetSelected(int objectId, int targetId, int x, int y, int z)
	{
		_objectId = objectId;
		_targetObjId = targetId;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x29);
		writeD(_objectId);
		writeD(_targetObjId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
	
	@Override
	public String getType()
	{
		return _S__39_TARGETSELECTED;
	}
}
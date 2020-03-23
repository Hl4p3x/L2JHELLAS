package com.l2jhellas.gameserver.network.serverpackets;

public final class StartRotation extends L2GameServerPacket
{
	private static final String _S__62_STARTROTATION = "[S] 62 StartRotation";
	
	private final int _charObjId;
	private final int _degree;
	private final int _side;
	private final int _speed;
	
	public StartRotation(int objId, int degree, int side, int speed)
	{
		_charObjId = objId;
		_degree = degree;
		_side = side;
		_speed = speed;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x62);
		writeD(_charObjId);
		writeD(_degree);
		writeD(_side);
		writeD(_speed);
	}
	
	@Override
	public String getType()
	{
		return _S__62_STARTROTATION;
	}
}
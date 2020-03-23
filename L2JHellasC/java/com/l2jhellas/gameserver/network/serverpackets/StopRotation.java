package com.l2jhellas.gameserver.network.serverpackets;

public class StopRotation extends L2GameServerPacket
{
	private static final String _S__78_STOPROTATION = "[S] 63 StopRotation";
	private final int _charObjId, _degree, _speed;
	
	public StopRotation(int objid, int degree, int speed)
	{
		_charObjId = objid;
		_degree = degree;
		_speed = speed;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x63);
		writeD(_charObjId);
		writeD(_degree);
		writeD(_speed);
		writeC(0);
	}
	
	@Override
	public String getType()
	{
		return _S__78_STOPROTATION;
	}
}
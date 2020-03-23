package com.l2jhellas.gameserver.network.serverpackets;

public class AutoAttackStop extends L2GameServerPacket
{
	// dh
	private static final String _S__3C_AUTOATTACKSTOP = "[S] 3C AutoAttackStop";
	private final int _targetObjId;
	
	public AutoAttackStop(int targetObjId)
	{
		_targetObjId = targetObjId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2c);
		writeD(_targetObjId);
	}
	
	@Override
	public String getType()
	{
		return _S__3C_AUTOATTACKSTOP;
	}
}
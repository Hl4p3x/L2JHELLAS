package com.l2jhellas.gameserver.network.serverpackets;

public class AutoAttackStart extends L2GameServerPacket
{
	// dh
	private static final String _S__3B_AUTOATTACKSTART = "[S] 2B AutoAttackStart";
	private final int _targetObjId;
	
	public AutoAttackStart(int targetId)
	{
		_targetObjId = targetId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2b);
		writeD(_targetObjId);
	}
	
	@Override
	public String getType()
	{
		return _S__3B_AUTOATTACKSTART;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Object;

public class Revive extends L2GameServerPacket
{
	private static final String _S__0C_REVIVE = "[S] 07 Revive";
	private final int _objectId;
	
	public Revive(L2Object obj)
	{
		_objectId = obj.getObjectId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x07);
		writeD(_objectId);
	}
	
	@Override
	public String getType()
	{
		return _S__0C_REVIVE;
	}
}
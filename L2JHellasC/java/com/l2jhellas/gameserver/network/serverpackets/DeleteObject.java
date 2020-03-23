package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Object;

public class DeleteObject extends L2GameServerPacket
{
	private static final String _S__1E_DELETEOBJECT = "[S] 12 DeleteObject";
	private final int _objectId;
	private final boolean _isSitting;
	
	public DeleteObject(L2Object obj)
	{
		_objectId = obj.getObjectId();
		_isSitting = false;
	}
	
	public DeleteObject(int obj)
	{
		_objectId = obj;
		_isSitting = false;
	}
	
	public DeleteObject(L2Object obj, boolean sit)
	{
		_objectId = obj.getObjectId();
		_isSitting = sit;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x12);
		writeD(_objectId);
		writeD(_isSitting ? 0x00 : 0x01); // 0 stand up and delete, 1 delete
	}
	
	@Override
	public String getType()
	{
		return _S__1E_DELETEOBJECT;
	}
}
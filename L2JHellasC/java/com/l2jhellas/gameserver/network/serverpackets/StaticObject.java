package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2StaticObjectInstance;

public class StaticObject extends L2GameServerPacket
{
	private static final String _S__99_StaticObjectPacket = "[S] 99 StaticObjectPacket";
	private final L2StaticObjectInstance _staticObject;
	
	public StaticObject(L2StaticObjectInstance StaticObject)
	{
		_staticObject = StaticObject;// staticObjectId
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x99);
		writeD(_staticObject.getStaticObjectId());// staticObjectId
		writeD(_staticObject.getObjectId());// objectId
	}
	
	@Override
	public String getType()
	{
		return _S__99_StaticObjectPacket;
	}
}

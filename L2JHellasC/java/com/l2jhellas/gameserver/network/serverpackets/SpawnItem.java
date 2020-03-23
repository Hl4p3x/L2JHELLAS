package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class SpawnItem extends L2GameServerPacket
{
	private static final String _S__15_SPAWNITEM = "[S] 15 SpawnItem";
	private final int _objectId;
	private final int _itemId;
	private final int _x, _y, _z;
	private final int _stackable, _count;
	
	public SpawnItem(L2ItemInstance item)
	{
		_objectId = item.getObjectId();
		_itemId = item.getItemId();
		_x = item.getX();
		_y = item.getY();
		_z = item.getZ();
		_stackable = item.isStackable() ? 0x01 : 0x00;
		_count = item.getCount();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0b);
		writeD(_objectId);
		writeD(_itemId);
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
		// only show item count if it is a stackable item
		writeD(_stackable);
		writeD(_count);
		writeD(0x00); // c2
	}
	
	@Override
	public String getType()
	{
		return _S__15_SPAWNITEM;
	}
}
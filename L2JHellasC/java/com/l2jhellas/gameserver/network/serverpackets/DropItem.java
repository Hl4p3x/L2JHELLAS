package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class DropItem extends L2GameServerPacket
{
	private static final String _S__16_DROPITEM = "[S] 0c DropItem";
	private final L2ItemInstance _item;
	private final int _charObjId;
	
	public DropItem(L2ItemInstance item, int playerObjId)
	{
		_item = item;
		_charObjId = playerObjId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0c);
		writeD(_charObjId);
		writeD(_item.getObjectId());
		writeD(_item.getItemId());
		
		writeD(_item.getX());
		writeD(_item.getY());
		writeD(_item.getZ());
		writeD(_item.isStackable() ? 0x01 : 0x00);
		writeD(_item.getCount());
		
		writeD(1); 
	}
	
	@Override
	public String getType()
	{
		return _S__16_DROPITEM;
	}
}
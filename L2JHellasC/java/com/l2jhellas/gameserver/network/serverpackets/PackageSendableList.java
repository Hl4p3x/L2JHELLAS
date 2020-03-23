package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class PackageSendableList extends L2GameServerPacket
{
	private static final String _S__C3_PACKAGESENDABLELIST = "[S] C3 PackageSendableList";
	private final L2ItemInstance[] _items;
	private final int _playerObjId;
	
	public PackageSendableList(L2ItemInstance[] items, int playerObjId)
	{
		_items = items;
		_playerObjId = playerObjId;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xC3);
		
		writeD(_playerObjId);
		writeD(getClient().getActiveChar().getAdena());
		writeD(_items.length);
		
		for (L2ItemInstance item : _items) 
		{
			if (item == null || item.getItem() == null)
				continue;
			
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			writeH(0x00);
			writeD(item.getObjectId());
		}
	}
	
	@Override
	public String getType()
	{
		return _S__C3_PACKAGESENDABLELIST;
	}
}
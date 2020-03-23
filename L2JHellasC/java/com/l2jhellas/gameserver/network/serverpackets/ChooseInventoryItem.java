package com.l2jhellas.gameserver.network.serverpackets;

public class ChooseInventoryItem extends L2GameServerPacket
{
	private static final String _S__6F_CHOOSEINVENTORYITEM = "[S] 6f ChooseInventoryItem";
	
	private final int _itemId;
	
	public ChooseInventoryItem(int itemId)
	{
		_itemId = itemId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6f);
		writeD(_itemId);
	}
	
	@Override
	public String getType()
	{
		return _S__6F_CHOOSEINVENTORYITEM;
	}
}
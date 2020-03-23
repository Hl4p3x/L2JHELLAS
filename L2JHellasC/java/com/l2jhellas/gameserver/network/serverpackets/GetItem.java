package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class GetItem extends L2GameServerPacket
{
	private static final String _S__17_GETITEM = "[S] 0d GetItem";
	private final L2ItemInstance _item;
	private final int _playerId;
	
	public GetItem(L2ItemInstance item, int playerId)
	{
		_item = item;
		_playerId = playerId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0d);
		writeD(_playerId);
		writeD(_item.getObjectId());
		
		writeD(_item.getX());
		writeD(_item.getY());
		writeD(_item.getZ());
	}
	
	@Override
	public String getType()
	{
		return _S__17_GETITEM;
	}
}
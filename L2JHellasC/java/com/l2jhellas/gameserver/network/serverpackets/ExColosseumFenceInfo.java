package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2FenceInstance;

public class ExColosseumFenceInfo extends L2GameServerPacket
{
	private final int _objectId;
	private final L2FenceInstance _fence;
	
	public ExColosseumFenceInfo(int objectId, L2FenceInstance fence)
	{
		_objectId = objectId;
		_fence = fence;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x09);
		
		writeD(_objectId);
		writeD(_fence.getState().getClientId());
		writeD(_fence.getX());
		writeD(_fence.getY());
		writeD(_fence.getZ());
		writeD(_fence.getWidth());
		writeD(_fence.getLength());
	}
}
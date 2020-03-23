package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.templates.L2Henna;

public class HennaRemoveList extends L2GameServerPacket
{
	private final L2PcInstance _player;
	
	public HennaRemoveList(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe5);
		writeD(_player.getAdena());
		writeD(_player.getHennaEmptySlots());
		writeD(Math.abs(_player.getHennaEmptySlots() - 3));
		
		for (L2Henna henna : _player.getHennaList())
		{
			if (henna != null)
			{
				writeD(henna.getSymbolId());
				writeD(henna.getDyeId());
				writeD(L2Henna.getAmountDyeRequire() / 2);
				writeD(henna.getPrice() / 5);
				writeD(0x01);
			}
		}
	}
}
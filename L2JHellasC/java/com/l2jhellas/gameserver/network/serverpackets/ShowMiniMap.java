package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.SevenSigns;

public class ShowMiniMap extends L2GameServerPacket
{
	private static final String _S__B6_SHOWMINIMAP = "[S] 9d ShowMiniMap";
	private final int _mapId;
	
	public ShowMiniMap(int mapId)
	{
		_mapId = mapId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9d);
		writeD(_mapId);
		writeD(SevenSigns.getInstance().getCurrentPeriod());
	}
	
	@Override
	public String getType()
	{
		return _S__B6_SHOWMINIMAP;
	}
}
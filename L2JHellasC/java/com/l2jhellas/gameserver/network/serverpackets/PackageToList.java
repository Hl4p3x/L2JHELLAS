package com.l2jhellas.gameserver.network.serverpackets;

import java.util.Map;

public class PackageToList extends L2GameServerPacket
{
	private static final String _S__C2_PACKAGETOLIST = "[S] C2 PackageToList";
	private final Map<Integer, String> _players;
	
	// Lecter : i put a char list here, but i'm unsure these really are Pc. I duno how freight work tho...
	public PackageToList(Map<Integer, String> players)
	{
		_players = players;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xC2);
		writeD(_players.size());
		
		for (Map.Entry<Integer, String> player : _players.entrySet())
		{
			writeD(player.getKey());// id
			writeS(player.getValue());
		}
	}
	
	@Override
	public String getType()
	{
		return _S__C2_PACKAGETOLIST;
	}
}
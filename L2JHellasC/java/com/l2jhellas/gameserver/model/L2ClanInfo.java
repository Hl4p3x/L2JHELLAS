package com.l2jhellas.gameserver.model;

public class L2ClanInfo
{
	private final L2Clan _clan;
	private final int _total;
	private final int _online;
	
	public L2ClanInfo(final L2Clan clan)
	{
		_clan = clan;
		_total = clan.getMembersCount();
		_online = clan.getOnlineMembersCount();
	}
	
	public L2Clan getClan()
	{
		return _clan;
	}
	
	public int getTotal()
	{
		return _total;
	}
	
	public int getOnline()
	{
		return _online;
	}
}
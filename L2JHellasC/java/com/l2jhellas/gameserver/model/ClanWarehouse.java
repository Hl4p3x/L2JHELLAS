package com.l2jhellas.gameserver.model;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class ClanWarehouse extends Warehouse
{
	private final L2Clan _clan;
	
	public ClanWarehouse(L2Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	public int getOwnerId()
	{
		return _clan.getClanId();
	}
	
	@Override
	public L2PcInstance getOwner()
	{
		return _clan.getLeader().getPlayerInstance();
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.CLANWH;
	}
	
	public static String getLocationId()
	{
		return "0";
	}
	
	public static int getLocationId(boolean dummy)
	{
		return 0;
	}
	
	public void setLocationId(L2PcInstance dummy)
	{
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return (_items.size() + slots <= Config.WAREHOUSE_SLOTS_CLAN);
	}
}
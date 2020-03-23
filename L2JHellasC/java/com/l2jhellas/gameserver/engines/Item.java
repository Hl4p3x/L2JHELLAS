package com.l2jhellas.gameserver.engines;

import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.templates.StatsSet;

public class Item
{
	public int id;
	public Enum<?> type;
	public String name;
	public StatsSet set;
	public int currentLevel;
	public L2Item item;
}
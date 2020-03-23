package com.l2jhellas.gameserver.model;

import java.util.ArrayList;

import com.l2jhellas.Config;
import com.l2jhellas.util.Rnd;

public class L2DropCategory
{
	private final ArrayList<L2DropData> _drops;
	private int _categoryChance; // a sum of chances for calculating if an item will be dropped from this category
	private int _categoryBalancedChance; // sum for balancing drop selection inside categories in high rate servers
	private final int _categoryType;
	
	public L2DropCategory(int categoryType)
	{
		_categoryType = categoryType;
		_drops = new ArrayList<>(0);
		_categoryChance = 0;
		_categoryBalancedChance = 0;
	}
	
	public void addDropData(L2DropData drop, boolean raid)
	{
		if (drop.isQuestDrop())
		{
			// if (_questDrops == null)
			// _questDrops = new ArrayList<L2DropData>(0);
			// _questDrops.add(drop);
		}
		else
		{
			_drops.add(drop);
			_categoryChance += drop.getChance();
			// for drop selection inside a category: max 100 % chance for getting an item, scaling all values to that.
			_categoryBalancedChance += Math.min((drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS)), L2DropData.MAX_CHANCE);
		}
	}
	
	public ArrayList<L2DropData> getAllDrops()
	{
		return _drops;
	}
	
	public void clearAllDrops()
	{
		_drops.clear();
	}
	
	public boolean isSweep()
	{
		return (getCategoryType() == -1);
	}
	
	// this returns the chance for the category to be visited in order to check if
	// drops might come from it. Category -1 (spoil) must always be visited
	// (but may return 0 or many drops)
	public int getCategoryChance()
	{
		return getCategoryType() >= 0 ? _categoryChance : L2DropData.MAX_CHANCE;
	}
	
	public int getCategoryBalancedChance()
	{

		return getCategoryType() >= 0 ? _categoryBalancedChance : L2DropData.MAX_CHANCE;
	}
	
	public int getCategoryType()
	{
		return _categoryType;
	}
	
	public synchronized L2DropData dropSeedAllowedDropsOnly()
	{
		ArrayList<L2DropData> drops = new ArrayList<>();
		int subCatChance = 0;
		for (L2DropData drop : getAllDrops())
		{
			if ((drop.getItemId() == 57) || (drop.getItemId() == 6360) || (drop.getItemId() == 6361) || (drop.getItemId() == 6362))
			{
				drops.add(drop);
				subCatChance += drop.getChance();
			}
		}
		
		// among the results choose one.
		int randomIndex = Rnd.get(subCatChance);
		int sum = 0;
		for (L2DropData drop : drops)
		{
			sum += drop.getChance();
			
			if (sum > randomIndex) // drop this item and exit the function
			{
				drops.clear();
				drops = null;
				return drop;
			}
		}
		// since it is still within category, only drop one of the acceptable drops from the results.
		return null;
	}
	
	public synchronized L2DropData dropOne(boolean raid)
	{
		int randomIndex = Rnd.get(getCategoryBalancedChance());
		int sum = 0;
		for (L2DropData drop : getAllDrops())
		{
			sum += Math.min((drop.getChance() * (raid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS)), L2DropData.MAX_CHANCE);
			if (sum >= randomIndex) // drop this item and exit the function
				return drop;
		}
		return null;
	}
}
package com.l2jhellas.gameserver.templates;

import com.l2jhellas.gameserver.datatables.xml.HennaData;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class L2Henna
{
	private final int symbolId;
	private final int dye;
	private final int price;
	private final int statINT;
	private final int statSTR;
	private final int statCON;
	private final int statMEN;
	private final int statDEX;
	private final int statWIT;
	
	public L2Henna(StatsSet set)
	{
		symbolId = set.getInteger("symbol_id");
		dye = set.getInteger("dye");
		price = set.getInteger("price");
		statINT = set.getInteger("INT");
		statSTR = set.getInteger("STR");
		statCON = set.getInteger("CON");
		statMEN = set.getInteger("MEN");
		statDEX = set.getInteger("DEX");
		statWIT = set.getInteger("WIT");
	}
	
	public int getSymbolId()
	{
		return symbolId;
	}
	
	public int getDyeId()
	{
		return dye;
	}
	
	public int getPrice()
	{
		return price;
	}
	
	public static final int getAmountDyeRequire()
	{
		return 10;
	}
	
	public int getStatINT()
	{
		return statINT;
	}
	
	public int getStatSTR()
	{
		return statSTR;
	}
	
	public int getStatCON()
	{
		return statCON;
	}
	
	public int getStatMEN()
	{
		return statMEN;
	}
	
	public int getStatDEX()
	{
		return statDEX;
	}
	
	public int getStatWIT()
	{
		return statWIT;
	}
	
	public boolean isForThisClass(L2PcInstance player)
	{
		for (L2Henna henna : HennaData.getInstance().getAvailableHenna(player.getClassId().getId()))
			if (henna.equals(this))
				return true;
		
		return false;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof L2Henna && symbolId == ((L2Henna) obj).symbolId && dye == ((L2Henna) obj).dye;
	}
}
package com.l2jhellas.gameserver.datatables.xml;

import java.util.HashMap;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.model.L2SummonItem;

public class SummonItemsData implements DocumentParser
{
	protected static final Logger _log = Logger.getLogger(SummonItemsData.class.getName());
	
	private final HashMap<Integer, L2SummonItem> _summonitems = new HashMap<>();
	
	public SummonItemsData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_summonitems.clear();
		parseDatapackFile("data/xml/summon_items.xml");
		_log.info("SummonItems Loaded: " + _summonitems.size() + " items.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		int itemID = 0, npcID = 0;
		byte summonType = 0;
		
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equalsIgnoreCase("list"))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equalsIgnoreCase("summon_item"))
					{
						itemID = Integer.valueOf(d.getAttributes().getNamedItem("itemID").getNodeValue());
						npcID = Integer.valueOf(d.getAttributes().getNamedItem("npcID").getNodeValue());
						summonType = Byte.valueOf(d.getAttributes().getNamedItem("summonType").getNodeValue());
						L2SummonItem summonitem = new L2SummonItem(itemID, npcID, summonType);
						_summonitems.put(itemID, summonitem);
					}
				}
			}
		}
	}
	
	public L2SummonItem getSummonItem(int itemId)
	{
		return _summonitems.get(itemId);
	}
	
	public int[] itemIDs()
	{
		int size = _summonitems.size();
		int[] result = new int[size];
		int i = 0;
		
		for (L2SummonItem si : _summonitems.values())
		{
			result[i] = si.getItemId();
			i++;
		}
		return result;
	}
	
	public static SummonItemsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SummonItemsData _instance = new SummonItemsData();
	}
}
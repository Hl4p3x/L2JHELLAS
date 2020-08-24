package com.l2jhellas.gameserver.datatables.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.PackRoot;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.holder.IntIntHolder;
import com.l2jhellas.gameserver.model.actor.item.Extractable.ExtractableItem;
import com.l2jhellas.gameserver.model.actor.item.Extractable.ExtractableProductItem;
import com.l2jhellas.gameserver.templates.StatsSet;

public class ExtractableItemData implements DocumentParser
{
	protected static Logger _log = Logger.getLogger(ExtractableItemData.class.getName());

	private final Map<Integer, ExtractableItem> _items = new HashMap<>();

	protected ExtractableItemData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_items.clear();
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/ExtractableItems.xml"));		
		_log.info("ExtractableItemData: Loaded " + _items.size() + " extractable items.");
	}
		
	@Override
	public void parseDocument(Document doc)
	{	
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equalsIgnoreCase("list"))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equalsIgnoreCase("item"))
					{
						int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
						final List<ExtractableProductItem> extractables = new ArrayList<>();
						final List<IntIntHolder> items = new ArrayList<>();
						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							if ("extract".equalsIgnoreCase(b.getNodeName()))
							{
								final StatsSet set = new StatsSet();
								final NamedNodeMap attrs = b.getAttributes();
								
								for (int i = 0; i < attrs.getLength(); i++)
								{
									final Node attr = attrs.item(i);
									set.set(attr.getNodeName(), attr.getNodeValue());
								}

								double chance = set.getFloat("chance");
					
								items.add(new IntIntHolder(set.getInteger("id"), set.getInteger("quantity")));
								extractables.add(new ExtractableProductItem(items, chance));													
							}
						}
						_items.put(id, new ExtractableItem(id, extractables));
					}
				}
			}
		}
	}
	
	public ExtractableItem getExtractableItem(int itemId)
	{
		return _items.get(itemId);
	}
	
	public int[] getAllItemIds()
	{
		int index = 0;
		final int[] ids = new int[_items.size()];
		for (ExtractableItem extractable : _items.values())
			ids[index++] = extractable.getItemId();
		return ids;
	}
	
	public static ExtractableItemData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ExtractableItemData INSTANCE = new ExtractableItemData();
	}
}
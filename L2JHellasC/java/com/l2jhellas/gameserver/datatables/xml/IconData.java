package com.l2jhellas.gameserver.datatables.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.PackRoot;
import com.l2jhellas.gameserver.engines.DocumentParser;

public class IconData implements DocumentParser
{
	protected static Logger _log = Logger.getLogger(IconData.class.getName());
	
	private final static Map<Integer, String> _items = new HashMap<>();
	
	private String _ID = "id", _ICON = "icon";

	protected IconData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_items.clear();
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/icons.xml"));
		_log.info("IconData: Loaded " + _items.size() + " icons.");
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
						int id = Integer.valueOf(d.getAttributes().getNamedItem(_ID).getNodeValue());
						String icon = String.valueOf(d.getAttributes().getNamedItem(_ICON).getNodeValue());					
						_items.put(id, icon);
					}
				}
			}
		}
	}

	public static String getIconByItemId(int itemId)
	{
		return _items.get(itemId);
	}

	public static IconData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final IconData INSTANCE = new IconData();
	}
}
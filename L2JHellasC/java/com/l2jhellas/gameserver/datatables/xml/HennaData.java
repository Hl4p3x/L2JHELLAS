package com.l2jhellas.gameserver.datatables.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.templates.L2Henna;
import com.l2jhellas.gameserver.templates.StatsSet;

public class HennaData implements DocumentParser
{
	private static Logger _log = Logger.getLogger(HennaData.class.getName());
	
	private final Map<Integer, L2Henna> _henna = new HashMap<>();
	private final Map<Integer, List<L2Henna>> _hennaTrees = new HashMap<>();
	
	protected HennaData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_henna.clear();
		_hennaTrees.clear();
		parseDatapackFile("data/xml/henna.xml");
		_log.config("HennaTable: Loaded " + _henna.size() + " templates.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		final Node n = doc.getFirstChild();
		
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			if (!d.getNodeName().equalsIgnoreCase("henna"))
				continue;
			
			final StatsSet hennaDat = new StatsSet();
			final Integer id = Integer.valueOf(d.getAttributes().getNamedItem("symbol_id").getNodeValue());
			
			hennaDat.set("symbol_id", id);
			
			hennaDat.set("dye", Integer.valueOf(d.getAttributes().getNamedItem("dye_id").getNodeValue()));
			hennaDat.set("price", Integer.valueOf(d.getAttributes().getNamedItem("price").getNodeValue()));
			
			hennaDat.set("INT", Integer.valueOf(d.getAttributes().getNamedItem("INT").getNodeValue()));
			hennaDat.set("STR", Integer.valueOf(d.getAttributes().getNamedItem("STR").getNodeValue()));
			hennaDat.set("CON", Integer.valueOf(d.getAttributes().getNamedItem("CON").getNodeValue()));
			hennaDat.set("MEN", Integer.valueOf(d.getAttributes().getNamedItem("MEN").getNodeValue()));
			hennaDat.set("DEX", Integer.valueOf(d.getAttributes().getNamedItem("DEX").getNodeValue()));
			hennaDat.set("WIT", Integer.valueOf(d.getAttributes().getNamedItem("WIT").getNodeValue()));
			final String[] classes = d.getAttributes().getNamedItem("classes").getNodeValue().split(",");
			
			final L2Henna template = new L2Henna(hennaDat);
			_henna.put(id, template);
			
			for (String clas : classes)
			{
				final Integer classId = Integer.valueOf(clas);
				if (!_hennaTrees.containsKey(classId))
				{
					List<L2Henna> list = new ArrayList<>();
					list.add(template);
					_hennaTrees.put(classId, list);
				}
				else
					_hennaTrees.get(classId).add(template);
			}
		}
	}
	
	public L2Henna getTemplate(int id)
	{
		return _henna.get(id);
	}
	
	public List<L2Henna> getAvailableHenna(int classId)
	{
		final List<L2Henna> henna = _hennaTrees.get(classId);
		if (henna == null)
			return Collections.emptyList();
		
		return henna;
	}
	
	public static HennaData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final HennaData _instance = new HennaData();
	}
}
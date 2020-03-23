package com.l2jhellas.gameserver.datatables.xml;

import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.actor.instance.L2StaticObjectInstance;

public class StaticObjData implements DocumentParser
{
	private static Logger _log = Logger.getLogger(StaticObjData.class.getName());
	L2StaticObjectInstance obj = null;
	
	protected StaticObjData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/xml/static_objects.xml");
		_log.info(StaticObjData.class.getSimpleName() + ": Loaded.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		Node n = doc.getFirstChild();
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			if (d.getNodeName().equalsIgnoreCase("staticobject"))
			{
				NamedNodeMap node = d.getAttributes();
				
				obj = new L2StaticObjectInstance(IdFactory.getInstance().getNextId());
				obj.setType(Integer.valueOf(node.getNamedItem("type").getNodeValue()));
				obj.setStaticObjectId(Integer.valueOf(node.getNamedItem("id").getNodeValue()));
				obj.setXYZ(Integer.valueOf(node.getNamedItem("x").getNodeValue()), Integer.valueOf(node.getNamedItem("y").getNodeValue()), Integer.valueOf(node.getNamedItem("z").getNodeValue()));
				obj.setMap(node.getNamedItem("texture").getNodeValue(), Integer.valueOf(node.getNamedItem("map_x").getNodeValue()), Integer.valueOf(node.getNamedItem("map_y").getNodeValue()));
				obj.spawnMe();
			}
		}
	}
	
	public static StaticObjData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final StaticObjData _instance = new StaticObjData();
	}
}
package com.l2jhellas.gameserver.datatables.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.PackRoot;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.model.L2ArmorSet;

public class ArmorSetsData implements DocumentParser
{
	private static Logger _log = Logger.getLogger(ArmorSetsData.class.getName());
	
	private final Map<Integer, L2ArmorSet> _armorSets = new HashMap<>();
	private final Map<Integer, ArmorDummy> _cusArmorSets = new HashMap<>();
	
	protected ArmorSetsData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_armorSets.clear();
		_cusArmorSets.clear();
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/armor_sets.xml"));
		_log.info(ArmorSetsData.class.getSimpleName() + ": Loaded " + _armorSets.size() + " armor sets.");
		_log.info(ArmorSetsData.class.getSimpleName() + ": Loaded " + _cusArmorSets.size() + " custom armor sets.");
		
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
					if (d.getNodeName().equalsIgnoreCase("armorset"))
					{
						int chest = Integer.valueOf(d.getAttributes().getNamedItem("chest").getNodeValue());
						int legs = Integer.valueOf(d.getAttributes().getNamedItem("legs").getNodeValue());
						int head = Integer.valueOf(d.getAttributes().getNamedItem("head").getNodeValue());
						int gloves = Integer.valueOf(d.getAttributes().getNamedItem("gloves").getNodeValue());
						int feet = Integer.valueOf(d.getAttributes().getNamedItem("feet").getNodeValue());
						int skill_id = Integer.valueOf(d.getAttributes().getNamedItem("skill_id").getNodeValue());
						int shield = Integer.valueOf(d.getAttributes().getNamedItem("shield").getNodeValue());
						int shield_skill_id = Integer.valueOf(d.getAttributes().getNamedItem("shield_skill_id").getNodeValue());
						int enchant6skill = Integer.valueOf(d.getAttributes().getNamedItem("enchant6skill").getNodeValue());
						
						_armorSets.put(chest, new L2ArmorSet(chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill));
						_cusArmorSets.put(chest, new ArmorDummy(chest, legs, head, gloves, feet, skill_id, shield));
					}
				}
			}
		}
	}
	
	public boolean setExists(int chestId)
	{
		return _armorSets.containsKey(chestId);
	}
	
	public L2ArmorSet getSet(int chestId)
	{
		return _armorSets.get(chestId);
	}
	
	public ArmorDummy getCusArmorSets(int id)
	{
		return _cusArmorSets.get(id);
	}
	
	public class ArmorDummy
	{
		private final int _chest;
		private final int _legs;
		private final int _head;
		private final int _gloves;
		private final int _feet;
		private final int _skill_id;
		private final int _shield;
		
		public ArmorDummy(int chest, int legs, int head, int gloves, int feet, int skill_id, int shield)
		{
			_chest = chest;
			_legs = legs;
			_head = head;
			_gloves = gloves;
			_feet = feet;
			_skill_id = skill_id;
			_shield = shield;
		}
		
		public int getChest()
		{
			return _chest;
		}
		
		public int getLegs()
		{
			return _legs;
		}
		
		public int getHead()
		{
			return _head;
		}
		
		public int getGloves()
		{
			return _gloves;
		}
		
		public int getFeet()
		{
			return _feet;
		}
		
		public int getSkill_id()
		{
			return _skill_id;
		}
		
		public int getShield()
		{
			return _shield;
		}
	}
	
	public static ArmorSetsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ArmorSetsData _instance = new ArmorSetsData();
	}
}
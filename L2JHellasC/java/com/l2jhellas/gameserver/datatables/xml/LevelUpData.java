package com.l2jhellas.gameserver.datatables.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.model.L2LvlupData;

public class LevelUpData implements DocumentParser
{
	private static final Logger _log = Logger.getLogger(LevelUpData.class.getName());
	
	private static final String CLASS_LVL = "class_lvl", CLASS_ID = "classid";
	private static final String MP_MOD = "mpmod", MP_ADD = "mpadd", MP_BASE = "mpbase";
	private static final String HP_MOD = "hpmod", HP_ADD = "hpadd", HP_BASE = "hpbase";
	private static final String CP_MOD = "cpmod", CP_ADD = "cpadd", CP_BASE = "cpbase";
	
	private final Map<Integer, L2LvlupData> _lvlTable = new HashMap<>();
	
	public void reload()
	{
		load();
	}
	
	protected LevelUpData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_lvlTable.clear();
		parseDatapackFile("data/xml/lvl_up_data.xml");
		_log.info(LevelUpData.class.getSimpleName() + ": LevelUpData: Loaded " + _lvlTable.size() + " character level up templates.");
		
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		L2LvlupData lvlDat;
		
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equalsIgnoreCase("list"))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equalsIgnoreCase("lvlup"))
					{
						lvlDat = new L2LvlupData();
						int CLASS1_ID = Integer.valueOf(d.getAttributes().getNamedItem(CLASS_ID).getNodeValue());
						int CLASS1_LVL = Integer.valueOf(d.getAttributes().getNamedItem(CLASS_LVL).getNodeValue());
						float HP_BASE1 = Float.valueOf(d.getAttributes().getNamedItem(HP_BASE).getNodeValue());
						float HP_ADD1 = Float.valueOf(d.getAttributes().getNamedItem(HP_ADD).getNodeValue());
						float HP_MOD1 = Float.valueOf(d.getAttributes().getNamedItem(HP_MOD).getNodeValue());
						float CP_BASE1 = Float.valueOf(d.getAttributes().getNamedItem(CP_BASE).getNodeValue());
						float CP_ADD1 = Float.valueOf(d.getAttributes().getNamedItem(CP_ADD).getNodeValue());
						float CP_MOD1 = Float.valueOf(d.getAttributes().getNamedItem(CP_MOD).getNodeValue());
						float MP_BASE1 = Float.valueOf(d.getAttributes().getNamedItem(MP_BASE).getNodeValue());
						float MP_ADD1 = Float.valueOf(d.getAttributes().getNamedItem(MP_ADD).getNodeValue());
						float MP_MOD1 = Float.valueOf(d.getAttributes().getNamedItem(MP_MOD).getNodeValue());
						
						lvlDat.setClassid(CLASS1_ID);
						lvlDat.setClassLvl(CLASS1_LVL);
						lvlDat.setClassHpBase(HP_BASE1);
						lvlDat.setClassHpAdd(HP_ADD1);
						lvlDat.setClassHpModifier(HP_MOD1);
						lvlDat.setClassCpBase(CP_BASE1);
						lvlDat.setClassCpAdd(CP_ADD1);
						lvlDat.setClassCpModifier(CP_MOD1);
						lvlDat.setClassMpBase(MP_BASE1);
						lvlDat.setClassMpAdd(MP_ADD1);
						lvlDat.setClassMpModifier(MP_MOD1);
						
						_lvlTable.put(new Integer(lvlDat.getClassid()), lvlDat);
					}
				}
			}
		}
	}
	
	public L2LvlupData getTemplate(int classId)
	{
		return _lvlTable.get(classId);
	}
	
	public L2LvlupData getTemplate(ClassId classId)
	{
		return _lvlTable.get(classId.getId());
	}
	
	public static LevelUpData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final LevelUpData _instance = new LevelUpData();
	}
}
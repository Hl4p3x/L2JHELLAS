package com.l2jhellas.gameserver.datatables.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.model.L2Skill;

public class SkillSpellbookData implements DocumentParser
{
	protected static final Logger _log = Logger.getLogger(FishTable.class.getName());
	
	private static Map<Integer, Integer> _skillSpellbooks = new HashMap<>();
	
	protected SkillSpellbookData()
	{
		if (!Config.SP_BOOK_NEEDED)
			return;
		
		load();
	}
	
	@Override
	public void load()
	{
		_skillSpellbooks.clear();
		parseDatapackFile("data/xml/skill_spellbooks.xml");
		_log.info("SkillSpellbookTable: Loaded " + _skillSpellbooks.size() + " spellbooks.");
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
					if (d.getNodeName().equalsIgnoreCase("skill_spellbook"))
					{
						_skillSpellbooks.put(Integer.valueOf(d.getAttributes().getNamedItem("skill_id").getNodeValue()), Integer.valueOf(d.getAttributes().getNamedItem("item_id").getNodeValue()));
					}
				}
			}
		}
	}
	
	public int getBookForSkill(int skillId, int level)
	{
		if (skillId == L2Skill.SKILL_DIVINE_INSPIRATION && level != -1)
		{
			switch (level)
			{
				case 1:
					return 8618; // Ancient Book - Divine Inspiration (Modern Language Version)
				case 2:
					return 8619; // Ancient Book - Divine Inspiration (Original Language Version)
				case 3:
					return 8620; // Ancient Book - Divine Inspiration (Manuscript)
				case 4:
					return 8621; // Ancient Book - Divine Inspiration (Original Version)
				default:
					return -1;
			}
		}
		
		if (!_skillSpellbooks.containsKey(skillId))
			return -1;
		
		return _skillSpellbooks.get(skillId);
	}
	
	public int getBookForSkill(L2Skill skill)
	{
		return getBookForSkill(skill.getId(), -1);
	}
	
	public int getBookForSkill(L2Skill skill, int level)
	{
		return getBookForSkill(skill.getId(), level);
	}
	
	public static SkillSpellbookData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillSpellbookData _instance = new SkillSpellbookData();
	}
}
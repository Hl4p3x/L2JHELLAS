package com.l2jhellas.gameserver.datatables.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

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
		forEach(doc, "list", listNode -> forEach(listNode, "book", bookNode ->
		{
			final NamedNodeMap attrs = bookNode.getAttributes();
			_skillSpellbooks.put(parseInteger(attrs, "skillId"), parseInteger(attrs, "itemId"));
		}));
	}
	
	public int getBookForSkill(int skillId, int level)
	{
		if (!Config.SP_BOOK_NEEDED)
			return 0;
		
		if (skillId == L2Skill.SKILL_DIVINE_INSPIRATION)
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
					return 0;
			}
		}
		
		if (level != 1)
			return 0;
				
		if (!_skillSpellbooks.containsKey(skillId))
			return 0;
		
		return _skillSpellbooks.get(skillId);
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
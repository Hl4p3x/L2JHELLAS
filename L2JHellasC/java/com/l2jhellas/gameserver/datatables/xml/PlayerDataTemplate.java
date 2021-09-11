package com.l2jhellas.gameserver.datatables.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import com.PackRoot;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.holder.GeneralSkillNode;
import com.l2jhellas.gameserver.holder.ItemTemplateHolder;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.templates.L2PcTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;

public class PlayerDataTemplate implements DocumentParser
{
	protected static final Logger _log = Logger.getLogger(PlayerDataTemplate.class.getName());

	private final Map<Integer, L2PcTemplate> _templates = new HashMap<>();
	
	protected PlayerDataTemplate()
	{
		load();
	}
	
	@Override
	public void load()
	{		
		parseDirectory(new File(PackRoot.DATAPACK_ROOT, "data/xml/classes"));
		_log.info(PlayerDataTemplate.class.getSimpleName() + ": Loaded " + _templates.size() + " character templates.");
		
		for (L2PcTemplate template : _templates.values())
		{
			final ClassId parentClassId = template.getClassId().getParent();
			if (parentClassId != null)
				template.getSkills().addAll(_templates.get(parentClassId.getId()).getSkills());
		}
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "class", classNode ->
		{
			final StatsSet set = new StatsSet();
			forEach(classNode, "set", setNode -> set.putAll(parseAttributes(setNode)));
			forEach(classNode, "items", itemsNode ->
			{
				final List<ItemTemplateHolder> items = new ArrayList<>();
				forEach(itemsNode, "item", itemNode -> items.add(new ItemTemplateHolder(parseAttributes(itemNode))));
				set.set("items", items);
			});
			forEach(classNode, "skills", skillsNode ->
			{
				final List<GeneralSkillNode> skills = new ArrayList<>();
				forEach(skillsNode, "skill", skillNode -> skills.add(new GeneralSkillNode(parseAttributes(skillNode))));
				set.set("skills", skills);
			});
			forEach(classNode, "spawns", spawnsNode ->
			{
				final List<Location> locs = new ArrayList<>();
				forEach(spawnsNode, "spawn", spawnNode -> locs.add(new Location(parseAttributes(spawnNode))));
				set.set("spawnLocations", locs);
			});
			_templates.put(set.getInteger("id"), new L2PcTemplate(set));
		}));
	}
	
	public L2PcTemplate getTemplate(ClassId classId)
	{
		return _templates.get(classId.getId());
	}
	
	public L2PcTemplate getTemplate(int classId)
	{
		return _templates.get(classId);
	}
	
	public final String getClassNameById(int classId)
	{
		final L2PcTemplate template = _templates.get(classId);
		return (template != null) ? template.getClassName() : "Invalid class";
	}
	
	public static PlayerDataTemplate getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PlayerDataTemplate INSTANCE = new PlayerDataTemplate();
	}
}
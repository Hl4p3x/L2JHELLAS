package com.l2jhellas.gameserver.datatables.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import com.PackRoot;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.holder.ClanSkillNode;
import com.l2jhellas.gameserver.holder.EnchantSkillNode;
import com.l2jhellas.gameserver.holder.FishingSkillNode;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.SkillTable;


public class SkillTreeData implements DocumentParser
{
	protected static final Logger _log = Logger.getLogger(SkillTreeData.class.getName());

	private final List<FishingSkillNode> _fishingSkills = new LinkedList<>();
	private final List<ClanSkillNode> _clanSkills = new LinkedList<>();
	private final List<EnchantSkillNode> _enchantSkills = new LinkedList<>();
	
	protected SkillTreeData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDirectory(new File(PackRoot.DATAPACK_ROOT, "data/xml/skillstrees"));
		_log.info(SkillTreeData.class.getSimpleName() + ": Loaded " + _fishingSkills.size() + " fishing skills.");		
		_log.info(SkillTreeData.class.getSimpleName() + ": Loaded " + _clanSkills.size() + " clan skills.");		
		_log.info(SkillTreeData.class.getSimpleName() + ": Loaded " + _enchantSkills.size() + " enchant skills.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		forEach(doc, "list", listNode ->
		{
			forEach(listNode, "clanSkill", clanSkillNode -> _clanSkills.add(new ClanSkillNode(parseAttributes(clanSkillNode))));
			forEach(listNode, "fishingSkill", fishingSkillNode -> _fishingSkills.add(new FishingSkillNode(parseAttributes(fishingSkillNode))));
			forEach(listNode, "enchantSkill", enchantSkillNode -> _enchantSkills.add(new EnchantSkillNode(parseAttributes(enchantSkillNode))));
		});
	}

	public List<FishingSkillNode> getFishingSkillsFor(L2PcInstance player)
	{
		final List<FishingSkillNode> result = new ArrayList<>();
		
		_fishingSkills.stream().filter(s -> s.getMinLvl() <= player.getLevel() && (!s.isDwarven() || (player.hasDwarvenCraft() && s.isDwarven()))).forEach(s ->
		{
			if (player.getSkillLevel(s.getId()) == s.getValue() - 1)
				result.add(s);
		});
		
		return result;
	}

	public FishingSkillNode getFishingSkillFor(L2PcInstance player, int skillId, int skillLevel)
	{
		// We first retrieve skill. If it doesn't exist for this id and level, return null.
		final FishingSkillNode fsn = _fishingSkills.stream().filter(s -> s.getId() == skillId && s.getValue() == skillLevel && (!s.isDwarven() || (player.hasDwarvenCraft() && s.isDwarven()))).findFirst().orElse(null);
		if (fsn == null)
			return null;
		
		// Integrity check ; we check if minimum template skill node is ok for player level.
		if (fsn.getMinLvl() > player.getLevel())
			return null;
		
		// We find current known player skill level, if any. If the level is respected, we return the skill.
		if (player.getSkillLevel(skillId) == fsn.getValue() - 1)
			return fsn;
		
		return null;
	}

	public int getRequiredLevelForNextFishingSkill(L2PcInstance player)
	{
		return _fishingSkills.stream().filter(s -> s.getMinLvl() > player.getLevel() && (!s.isDwarven() || (player.hasDwarvenCraft() && s.isDwarven()))).min((s1, s2) -> Integer.compare(s1.getMinLvl(), s2.getMinLvl())).map(s -> s.getMinLvl()).orElse(0);
	}

	public List<ClanSkillNode> getClanSkillsFor(L2PcInstance player)
	{
		final L2Clan clan = player.getClan();
		if (clan == null)
			return Collections.emptyList();
		
		final List<ClanSkillNode> result = new ArrayList<>();
		
		_clanSkills.stream().filter(s -> s.getMinLvl() <= clan.getLevel()).forEach(s ->
		{
			final L2Skill clanSkill = clan.getClanSkills().get(s.getId());
			if ((clanSkill == null && s.getValue() == 1) || (clanSkill != null && clanSkill.getLevel() == s.getValue() - 1))
				result.add(s);
		});
		
		return result;
	}

	public ClanSkillNode getClanSkillFor(L2PcInstance player, int skillId, int skillLevel)
	{
		final L2Clan clan = player.getClan();
		if (clan == null)
			return null;
		
		final ClanSkillNode csn = _clanSkills.stream().filter(s -> s.getId() == skillId && s.getValue() == skillLevel).findFirst().orElse(null);
		if (csn == null)
			return null;
		
		if (csn.getMinLvl() > clan.getLevel())
			return null;
		
		final L2Skill clanSkill = clan.getClanSkills().get(skillId);
		if ((clanSkill == null && csn.getValue() == 1) || (clanSkill != null && clanSkill.getLevel() == csn.getValue() - 1))
			return csn;
		
		return null;
	}
	
	public boolean isClanSkill(int skillId, int skillLevel)
	{
		return _clanSkills.stream().filter(s -> s.getId() == skillId && s.getValue() == skillLevel).findFirst() != null;
	}
	
	public List<EnchantSkillNode> getEnchantSkillsFor(L2PcInstance player)
	{
		final List<EnchantSkillNode> result = new ArrayList<>();
		
		for (EnchantSkillNode esn : _enchantSkills)
		{
			final L2Skill skill = player.getSkill(esn.getId());
			if (skill != null && ((skill.getLevel() == SkillTable.getInstance().getMaxLevel(skill.getId(),0) && (esn.getValue() == 101 || esn.getValue() == 141)) || (skill.getLevel() == esn.getValue() - 1)))
				result.add(esn);
		}
		return result;
	}

	public EnchantSkillNode getEnchantSkillFor(L2PcInstance player, int skillId, int skillLevel)
	{
		// We first retrieve skill. If it doesn't exist for this id and level, return null.
		final EnchantSkillNode esn = _enchantSkills.stream().filter(s -> s.getId() == skillId && s.getValue() == skillLevel).findFirst().orElse(null);
		if (esn == null)
			return null;
		
		// We now test player current skill level.
		final int currentSkillLevel = player.getSkillLevel(skillId);
		if ((currentSkillLevel == SkillTable.getInstance().getMaxLevel(skillId,0) && (skillLevel == 101 || skillLevel == 141)) || (currentSkillLevel == skillLevel - 1))
			return esn;
		
		return null;
	}
	
	public static SkillTreeData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillTreeData INSTANCE = new SkillTreeData();
	}
}
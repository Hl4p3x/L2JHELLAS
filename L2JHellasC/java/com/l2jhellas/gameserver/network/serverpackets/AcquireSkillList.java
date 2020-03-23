package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class AcquireSkillList extends L2GameServerPacket
{
	public enum skillType
	{
		Usual,
		Fishing,
		Clan
	}
	
	private static final String _S__A3_ACQUIRESKILLLIST = "[S] 8a AcquireSkillList";
	
	private final List<Skill> _skills;
	private final skillType _fishingSkills;
	
	private class Skill
	{
		public int id;
		public int nextLevel;
		public int maxLevel;
		public int spCost;
		public int requirements;
		
		public Skill(int pId, int pNextLevel, int pMaxLevel, int pSpCost, int pRequirements)
		{
			id = pId;
			nextLevel = pNextLevel;
			maxLevel = pMaxLevel;
			spCost = pSpCost;
			requirements = pRequirements;
		}
	}
	
	public AcquireSkillList(skillType type)
	{
		_skills = new ArrayList<>();
		_fishingSkills = type;
	}
	
	public void addSkill(int id, int nextLevel, int maxLevel, int spCost, int requirements)
	{
		_skills.add(new Skill(id, nextLevel, maxLevel, spCost, requirements));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x8a);
		writeD(_fishingSkills.ordinal()); // c4 : C5 : 0: usuall 1: fishing 2: clans
		writeD(_skills.size());
		
		for (Skill temp : _skills)
		{
			writeD(temp.id);
			writeD(temp.nextLevel);
			writeD(temp.maxLevel);
			writeD(temp.spCost);
			writeD(temp.requirements);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__A3_ACQUIRESKILLLIST;
	}
}
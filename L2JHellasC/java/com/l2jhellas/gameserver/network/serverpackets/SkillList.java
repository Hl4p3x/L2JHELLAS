package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class SkillList extends L2GameServerPacket
{
	private static final String _S__6D_SKILLLIST = "[S] 58 SkillList";
	private final List<Skill> _skills;
	
	class Skill
	{
		public int id;
		public int level;
		public boolean passive;
		public boolean disabled;
		
		Skill(int pId, int pLevel, boolean pPassive, boolean pDisabled)
		{
			id = pId;
			level = pLevel;
			passive = pPassive;
			disabled = pDisabled;
		}
	}
	
	public SkillList()
	{
		_skills = new ArrayList<>();
	}
	
	public void addSkill(int id, int level, boolean passive, boolean disabled)
	{
		_skills.add(new Skill(id, level, passive, disabled));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x58);
		writeD(_skills.size());
		
		for (Skill temp : _skills)
		{
			writeD(temp.passive ? 1 : 0);
			writeD(temp.level);
			writeD(temp.id);
			writeC(temp.disabled ? 1 : 0);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__6D_SKILLLIST;
	}
}
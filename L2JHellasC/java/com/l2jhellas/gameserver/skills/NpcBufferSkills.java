package com.l2jhellas.gameserver.skills;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.holder.ItemHolder;
import com.l2jhellas.gameserver.holder.SkillHolder;

public class NpcBufferSkills
{
	private final int _npcId;
	private final Map<Integer, NpcBufferData> _skills = new HashMap<>();
	
	public NpcBufferSkills(int npcId) 
	{
		_npcId = npcId;
	}
	
	public void addSkill(int skillId, int skillLevel, int skillFeeId, int skillFeeAmount)
	{
		_skills.put(skillId, new NpcBufferData(skillId, skillLevel, skillFeeId, skillFeeAmount));
	}
	
	public NpcBufferData getSkillInfo(int skillId) 
	{
		return _skills.get(skillId);
	}
	
	public Map<Integer, NpcBufferData> getSkills() 
	{
		return _skills;
	}
	
	public int getNpcId() 
	{
		return _npcId;
	}
	
	public class NpcBufferData
	{
		private final SkillHolder _skill;
		private final ItemHolder _fee;
		
		protected NpcBufferData(int skillId, int skillLevel, int feeId, int feeAmount)
		{
			_skill = new SkillHolder(skillId, skillLevel);
			_fee = new ItemHolder(feeId, feeAmount);
		}
		
		public SkillHolder getSkill() 
		{
			return _skill;
		}
		
		public ItemHolder getFee() 
		{
			return _fee;
		}
	}
}
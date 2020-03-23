package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.SkillTable;

public final class EffectBestowSkill extends L2Effect
{
	public EffectBestowSkill(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	@Override
	public boolean onStart()
	{
		final L2Skill tempSkill = SkillTable.getInstance().getInfo(getSkill().getTriggeredId(), getSkill().getTriggeredLevel());
		if (tempSkill == null)
			return false;
		
		getEffected().addChanceSkill(tempSkill);
		return super.onStart();
	}
	
	@Override
	public void onExit()
	{
		if (getInUse() && getCount() == 0)
		{
			final L2Skill tempSkill = SkillTable.getInstance().getInfo(getSkill().getTriggeredId(), getSkill().getTriggeredLevel());
			getEffected().removeChanceSkill(tempSkill.getId());
		}
		
		super.onExit();
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
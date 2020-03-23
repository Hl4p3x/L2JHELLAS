package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.SkillTable;

public final class EffectForce extends L2Effect
{
	public int forces;
	
	public EffectForce(Env env, EffectTemplate template)
	{
		super(env, template);
		forces = getSkill().getLevel();
	}
	
	@Override
	public boolean onActionTime()
	{
		return true;
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	public void increaseForce()
	{
		if (forces < 3)
		{
			forces++;
			updateBuff();
		}
	}
	
	public void decreaseForce()
	{
		forces--;
		if (forces < 1)
			exit();
		else
			updateBuff();
	}
	
	private void updateBuff()
	{
		exit();
		SkillTable.getInstance().getInfo(getSkill().getId(), forces).getEffects(getEffector(), getEffected());
	}
}
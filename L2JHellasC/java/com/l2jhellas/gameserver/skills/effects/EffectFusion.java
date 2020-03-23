package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.SkillTable;

public final class EffectFusion extends L2Effect
{
	public int _effect;
	public int _maxEffect;
	
	public EffectFusion(Env env, EffectTemplate template)
	{
		super(env, template);
		_effect = 1;
		_maxEffect = getSkill().getLevel();
	}
	
	@Override
	public boolean onActionTime()
	{
		return true;
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.FUSION;
	}
	
	public void increaseEffect()
	{
		if (_effect < _maxEffect)
		{
			_effect++;
			updateBuff();
		}
	}
	
	public void decreaseForce()
	{
		_effect--;
		if (_effect < 1)
			exit();
		else
			updateBuff();
	}
	
	private void updateBuff()
	{
		exit();
		SkillTable.getInstance().getInfo(getSkill().getId(), _effect).getEffects(getEffector(), getEffected());
	}
}
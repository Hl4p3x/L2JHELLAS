package com.l2jhellas.gameserver.holder;

import com.l2jhellas.gameserver.model.L2Skill;

public final class EffectHolder extends IntIntHolder
{
	private final int _duration;
	
	public EffectHolder(L2Skill skill, int duration)
	{
		super(skill.getId(), skill.getLevel());
		
		_duration = duration;
	}
	
	public int getDuration()
	{
		return _duration;
	}
}
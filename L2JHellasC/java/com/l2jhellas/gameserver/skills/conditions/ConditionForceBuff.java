package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.effects.EffectForce;

public final class ConditionForceBuff extends Condition
{
	private static final short BATTLE_FORCE = 5104;
	private static final short SPELL_FORCE = 5105;
	
	private final byte[] _forces;
	
	public ConditionForceBuff(byte[] forces)
	{
		_forces = forces;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (_forces[0] > 0)
		{
			L2Effect force = env.player.getFirstEffect(BATTLE_FORCE);
			if (force == null || ((EffectForce) force).forces < _forces[0])
				return false;
		}
		
		if (_forces[1] > 0)
		{
			L2Effect force = env.player.getFirstEffect(SPELL_FORCE);
			if (force == null || ((EffectForce) force).forces < _forces[1])
				return false;
		}
		
		return true;
	}
}
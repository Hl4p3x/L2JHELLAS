package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.skills.Env;

final class EffectSignetNoise extends EffectSignet
{
	public EffectSignetNoise(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SIGNET_GROUND;
	}
	
	@Override
	public boolean onActionTime()
	{
		for (L2Character character : zone.getCharactersInZone())
		{
			for (L2Effect effect : character.getAllEffects())
			{
				if (effect.getSkill().isDance())
					effect.exit();
			}
		}
		
		return true;
	}
}
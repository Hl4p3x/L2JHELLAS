package com.l2jhellas.gameserver.skills.conditions;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.effects.EffectSeed;

public class ConditionElementSeed extends Condition
{
	private static final Logger _log = Logger.getLogger(ConditionElementSeed.class.getName());
	private static int[] seedSkills =
	{
		1285,
		1286,
		1287,
		426,
		427
	};
	private final int[] _requiredSeeds;
	
	public ConditionElementSeed(int[] seeds)
	{
		_requiredSeeds = seeds;
		if (Config.DEBUG)
			_log.config(ConditionElementSeed.class.getName() + ": Required seeds: " + _requiredSeeds[0] + ", " + _requiredSeeds[1] + ", " + _requiredSeeds[2] + ", " + _requiredSeeds[3] + ", " + _requiredSeeds[4]);
	}
	
	ConditionElementSeed(int fire, int water, int wind, int battle, int spell, int various, int any)
	{
		_requiredSeeds = new int[7];
		_requiredSeeds[0] = fire;
		_requiredSeeds[1] = water;
		_requiredSeeds[2] = wind;
		_requiredSeeds[3] = battle;
		_requiredSeeds[4] = spell;
		_requiredSeeds[5] = various;
		_requiredSeeds[6] = any;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		int[] Seeds = new int[5];
		for (int i = 0; i < Seeds.length; i++)
		{
			Seeds[i] = (env.player.getFirstEffect(seedSkills[i]) instanceof EffectSeed ? ((EffectSeed) env.player.getFirstEffect(seedSkills[i])).getPower() : 0);
			if (Seeds[i] >= _requiredSeeds[i])
				Seeds[i] -= _requiredSeeds[i];
			else
				return false;
		}
		
		if (Config.DEBUG)
			_log.config(ConditionElementSeed.class.getName() + ": Seeds: " + Seeds[0] + ", " + Seeds[1] + ", " + Seeds[2]);
		
		if (_requiredSeeds[5] > 0)
		{
			int count = 0;
			for (int i = 0; i < Seeds.length && count < _requiredSeeds[5]; i++)
			{
				if (Seeds[i] > 0)
				{
					Seeds[i]--;
					count++;
				}
			}
			if (count < _requiredSeeds[5])
				return false;
		}
		
		if (_requiredSeeds[6] > 0)
		{
			int count = 0;
			for (int i = 0; i < Seeds.length && count < _requiredSeeds[6]; i++)
			{
				count += Seeds[i];
			}
			if (count < _requiredSeeds[6])
				return false;
		}
		return true;
	}
}
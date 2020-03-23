package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;

public class ConditionPlayerRace extends Condition
{
	
	private final ClassRace _race;
	
	public ConditionPlayerRace(ClassRace race)
	{
		_race = race;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
			return false;
		
		return ((L2PcInstance) env.player).getRace() == _race;
	}
}
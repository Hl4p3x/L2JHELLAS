package com.l2jhellas.gameserver.skills.conditions;

import java.util.ArrayList;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.skills.Env;

public class ConditionTargetRaceId extends Condition
{
	private final ArrayList<Integer> _raceIds;
	
	public ConditionTargetRaceId(ArrayList<Integer> array)
	{
		_raceIds = array;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.target instanceof L2Npc))
			return false;
		
		return (_raceIds.contains(((L2Npc) env.target).getTemplate().race.ordinal()));
	}
}
package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;

public class ConditionPlayerState extends Condition
{
	public enum CheckPlayerState
	{
		RESTING,
		MOVING,
		RUNNING,
		FLYING,
		BEHIND,
		FRONT,
		SIDE
	}
	
	private final CheckPlayerState _check;
	private final boolean _required;
	
	public ConditionPlayerState(CheckPlayerState check, boolean required)
	{
		_check = check;
		_required = required;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		switch (_check)
		{
			case RESTING:
				if (env.player instanceof L2PcInstance)
				{
					return ((L2PcInstance) env.player).isSitting() == _required;
				}
				return !_required;
			case MOVING:
				return env.player.isMoving() == _required;
			case RUNNING:
				return env.player.isMoving() == _required && env.player.isRunning() == _required;
			case FLYING:
				return env.player.isFlying() == _required;
			case BEHIND:
				return env.player.isBehindOfTarget() == _required;
			case FRONT:
				return env.player.isFrontOfTarget() == _required;
			case SIDE:
				return env.player.isSideOfTarget() == _required;
		}
		return !_required;
	}
}
package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.skills.Env;

public class ConditionGameTime extends Condition
{
	public enum CheckGameTime
	{
		NIGHT
	}
	
	private final CheckGameTime _check;
	private final boolean _required;
	
	public ConditionGameTime(CheckGameTime check, boolean required)
	{
		_check = check;
		_required = required;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		switch (_check)
		{
			case NIGHT:
				return GameTimeController.getInstance().isNight() == _required;
		}
		return !_required;
	}
}
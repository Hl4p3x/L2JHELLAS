package Extensions.AchievmentsEngine.conditions;

import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class Levelup extends Condition
{
	public Levelup(Object value)
	{
		super(value);
		setName("Level");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
			return false;
		int val = Integer.parseInt(getValue().toString());
		
		if (player.getLevel() >= val)
			return true;
		return false;
	}
}
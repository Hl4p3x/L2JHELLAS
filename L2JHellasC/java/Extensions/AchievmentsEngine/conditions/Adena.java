package Extensions.AchievmentsEngine.conditions;

import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class Adena extends Condition
{
	public Adena(Object value)
	{
		super(value);
		setName("Adena");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
			return false;
		
		long val = Integer.parseInt(getValue().toString());
		
		if (player.getInventory().getAdena() >= val)
			return true;
		
		return false;
	}
}
package Extensions.AchievmentsEngine.conditions;

import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class Pvp extends Condition
{
	public Pvp(Object value)
	{
		super(value);
		setName("PvP Count");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
			return false;
		
		int val = Integer.parseInt(getValue().toString());
		
		if (player.getPvpKills() >= val)
			return true;
		
		return false;
	}
}
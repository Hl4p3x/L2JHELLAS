package Extensions.AchievmentsEngine.conditions;

import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class OnlineTime extends Condition
{
	public OnlineTime(Object value)
	{
		super(value);
		setName("Online Time");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
			return false;
		
		int val = Integer.parseInt(getValue().toString());
		
		if (player.getOnlineTime() >= val * 24 * 60 * 60 * 1000)
		{
			return true;
		}
		return false;
	}
}
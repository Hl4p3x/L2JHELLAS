package Extensions.AchievmentsEngine.conditions;

import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class events extends Condition
{
	public events(Object value)
	{
		super(value);
		setName("Events played");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
			return false;
		
		Integer.parseInt(getValue().toString());
		
		// if (EventStats.getInstance().getEvents(player.getObjectId()) >= val)
		// return true;
		
		return false;
	}
}
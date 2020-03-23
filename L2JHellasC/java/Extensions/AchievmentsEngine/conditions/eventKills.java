package Extensions.AchievmentsEngine.conditions;

import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class eventKills extends Condition
{
	public eventKills(Object value)
	{
		super(value);
		setName("Event Kills");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
			return false;
		
		Integer.parseInt(getValue().toString());
		
		// if (EventStats.getInstance().getEventKills(player.getObjectId()) >= val)
		// return true;
		
		return false;
	}
}
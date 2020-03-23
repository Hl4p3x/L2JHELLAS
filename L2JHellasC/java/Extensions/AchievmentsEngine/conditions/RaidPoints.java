package Extensions.AchievmentsEngine.conditions;

import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.instancemanager.RaidBossPointsManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RaidPoints extends Condition
{
	public RaidPoints(Object value)
	{
		super(value);
		setName("Raid Points");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
			return false;
		
		int val = Integer.parseInt(getValue().toString());
		
		RaidBossPointsManager.getInstance();
		if (RaidBossPointsManager.getInstance().getPointsByOwnerId(player.getObjectId()) >= val)
			return true;
		return false;
	}
}
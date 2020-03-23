package Extensions.AchievmentsEngine.conditions;

import java.util.Map;

import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.instancemanager.RaidBossPointsManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RaidKill extends Condition
{
	public RaidKill(Object value)
	{
		super(value);
		setName("Raid Kill");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
			return false;
		
		int val = Integer.parseInt(getValue().toString());
		
		Map<Integer, Integer> list = RaidBossPointsManager.getInstance().getList(player);
		if (list != null)
		{
			for (int bid : list.keySet())
			{
				if (bid == val)
				{
					if (RaidBossPointsManager.getInstance().getList(player).get(bid) > 0)
						return true;
				}
			}
		}
		return false;
	}
}
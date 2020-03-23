package Extensions.AchievmentsEngine.conditions;

import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class Hero extends Condition
{
	public Hero(Object value)
	{
		super(value);
		setName("Hero");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
			return false;
		
		if (player.isHero())
			return true;
		
		return false;
	}
}
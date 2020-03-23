package Extensions.AchievmentsEngine.conditions;

import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class Mage extends Condition
{
	public Mage(Object value)
	{
		super(value);
		setName("Be Mage");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
			return false;
		
		if (player.isMageClass())
			return true;
		
		return false;
	}
}
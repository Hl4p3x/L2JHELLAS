package Extensions.AchievmentsEngine.conditions;

import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Hero;
import com.l2jhellas.gameserver.model.entity.olympiad.Olympiad;
import com.l2jhellas.gameserver.templates.StatsSet;

public class HeroCount extends Condition
{
	public HeroCount(Object value)
	{
		super(value);
		setName("Hero Count");
	}
	
	@Override
	public boolean meetConditionRequirements(L2PcInstance player)
	{
		if (getValue() == null)
			return false;
		
		int val = Integer.parseInt(getValue().toString());
		for (int hero : Hero.getInstance().getHeroes().keySet())
		{
			if (hero == player.getObjectId())
			{
				StatsSet sts = Hero.getInstance().getHeroes().get(hero);
				if (sts.getString(Olympiad.CHAR_NAME).equals(player.getName()))
				{
					if (sts.getInteger(Hero.COUNT) >= val)
						return true;
				}
			}
		}
		return false;
	}
}
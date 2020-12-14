package Extensions.fake.roboto.ai.addon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import Extensions.fake.roboto.FakePlayer;
import Extensions.fake.roboto.ai.CombatAI;
import Extensions.fake.roboto.model.HealingSpell;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;

public interface IHealer
{
	
	default void tryTargetingLowestHpTargetInRadius(FakePlayer player, Class<? extends L2Character> L2CharacterClass, int radius)
	{
		if (player.getTarget() == null)
		{
			List<L2Character> targets = new ArrayList<>();
			
			L2World.getInstance().forEachVisibleObjectInRange(player, L2CharacterClass, radius, target ->
			{
				if (target.isDead() || target.isPlayer() && !target.getActingPlayer().getAppearance().isVisible() || !target.isVisible())
					return;
				
				targets.add(target);
			});
			
			List<L2Character> sortedTargets = targets.stream().sorted((x1, x2) -> Double.compare(x1.getCurrentHp(), x2.getCurrentHp())).collect(Collectors.toList());
			
			if (!sortedTargets.isEmpty())
			{
				L2Character target = sortedTargets.get(0);
				player.setTarget(target);
			}
		}
		else
		{
			if (((L2Character) player.getTarget()).isDead() || !player.isInsideRadius(player.getTarget(), radius, false, false))
				player.setTarget(null);
		}
	}
	
	default void tryHealingTarget(FakePlayer player)
	{
		
		if (player.getTarget() != null)
		{
			L2Character target = (L2Character) player.getTarget();
			if (player.getFakeAi() instanceof CombatAI)
			{
				HealingSpell skill = ((CombatAI) player.getFakeAi()).getRandomAvaiableHealingSpellForTarget();
				if (skill != null)
				{
					switch (skill.getCondition())
					{
						case LESSHPPERCENT:
							double currentHpPercentage = Math.round(100.0 / target.getMaxHp() * target.getCurrentHp());
							if (currentHpPercentage <= skill.getConditionValue())
							{
								player.getFakeAi().castSpell(player.getSkill(skill.getSkillId()));
							}
							break;
						default:
							break;
					}
					
				}
			}
		}
	}
}

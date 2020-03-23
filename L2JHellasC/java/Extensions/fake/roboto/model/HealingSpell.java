package Extensions.fake.roboto.model;

import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;

public class HealingSpell extends BotSkill
{
	
	private final L2SkillTargetType _targetType;
	
	public HealingSpell(int skillId, L2SkillTargetType targetType, SpellUsageCondition condition, int conditionValue, int priority)
	{
		super(skillId, condition, conditionValue, priority);
		_targetType = targetType;
	}
	
	public HealingSpell(int skillId, L2SkillTargetType targetType, int conditionValue, int priority)
	{
		super(skillId, SpellUsageCondition.LESSHPPERCENT, conditionValue, priority);
		_targetType = targetType;
	}
	
	public L2SkillTargetType getTargetType()
	{
		return _targetType;
	}
}
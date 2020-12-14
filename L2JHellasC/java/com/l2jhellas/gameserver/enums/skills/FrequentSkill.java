package com.l2jhellas.gameserver.enums.skills;

import com.l2jhellas.gameserver.holder.SkillHolder;
import com.l2jhellas.gameserver.model.L2Skill;

public enum FrequentSkill
{
	LUCKY(194, 1),
	SEAL_OF_RULER(246, 1),
	BUILD_HEADQUARTERS(247, 1),
	STRIDER_SIEGE_ASSAULT(325, 1),
	DWARVEN_CRAFT(1321, 1),
	COMMON_CRAFT(1322, 1),
	LARGE_FIREWORK(2025, 1),
	SPECIAL_TREE_RECOVERY_BONUS(2139, 1),
		
	ANTHARAS_JUMP(4106, 1),
	ANTHARAS_TAIL(4107, 1),
	ANTHARAS_FEAR(4108, 1),
	ANTHARAS_DEBUFF(4109, 1),
	ANTHARAS_MOUTH(4110, 1),
	ANTHARAS_BREATH(4111, 1),
	ANTHARAS_NORMAL_ATTACK(4112, 1),
	ANTHARAS_NORMAL_ATTACK_EX(4113, 1),
	ANTHARAS_SHORT_FEAR(5092, 1),
	ANTHARAS_METEOR(5093, 1),
		
	RAID_CURSE(4215, 1),
	WYVERN_BREATH(4289, 1),
	ARENA_CP_RECOVERY(4380, 1),
	RAID_CURSE2(4515, 1),
	RAID_ANTI_STRIDER_SLOW(4258, 1),
	VARKA_KETRA_PETRIFICATION(4578, 1),
	FAKE_PETRIFICATION(4616, 1),
	THE_VICTOR_OF_WAR(5074, 1),
	THE_VANQUISHED_OF_WAR(5075, 1),
	BLESSING_OF_PROTECTION(5182, 1),
	FIREWORK(5965, 1);
		
	private final SkillHolder _holder;

	private FrequentSkill(int id, int level)
	{
		_holder = new SkillHolder(id, level);
	}
		
	public L2Skill getSkill()
	{
		return _holder.getSkill();
	}
}
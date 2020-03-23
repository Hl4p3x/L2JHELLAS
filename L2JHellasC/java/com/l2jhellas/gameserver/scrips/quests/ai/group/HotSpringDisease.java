package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.Rnd;

public class HotSpringDisease extends AbstractNpcAI
{
	// Diseases
	private static final int MALARIA = 4554;
	
	// Chance
	private static final int DISEASE_CHANCE = 1;
	
	// Monsters
	private static final int[][] MONSTERS_DISEASES =
	{
		{
			21314,
			21316,
			21317,
			21319,
			21321,
			21322
		},
		{
			4551,
			4552,
			4553,
			4552,
			4551,
			4553
		}
	};
	
	public HotSpringDisease()
	{
		super(HotSpringDisease.class.getSimpleName(), "ai/group");
		registerMobs(MONSTERS_DISEASES[0], QuestEventType.ON_ATTACK_ACT);
	}
	
	@Override
	public String onAttackAct(L2Npc npc, L2PcInstance victim)
	{
		for (int i = 0; i < 6; i++)
		{
			if (MONSTERS_DISEASES[0][i] != npc.getNpcId())
				continue;
			
			tryToApplyEffect(npc, victim, MALARIA);
			tryToApplyEffect(npc, victim, MONSTERS_DISEASES[1][i]);
		}
		return super.onAttackAct(npc, victim);
	}
	
	private static void tryToApplyEffect(L2Npc npc, L2PcInstance victim, int skillId)
	{
		if (Rnd.get(100) < DISEASE_CHANCE)
		{
			int level = 1;
			
			L2Effect[] effects = victim.getAllEffects();
			if (effects.length != 0)
			{
				for (L2Effect e : effects)
				{
					if (e.getSkill().getId() != skillId)
						continue;
					
					level += e.getSkill().getLevel();
					e.exit();
				}
			}
			
			if (level > 10)
				level = 10;
			
			SkillTable.getInstance().getInfo(skillId, level).getEffects(npc, victim);
		}
	}
	
	public static void main(String[] args)
	{
		new HotSpringDisease();
	}
}
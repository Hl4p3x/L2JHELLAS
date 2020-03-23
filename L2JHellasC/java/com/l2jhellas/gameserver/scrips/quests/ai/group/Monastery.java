package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.enums.Sex;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.Util;

public class Monastery extends AbstractNpcAI
{
	private static final int[] mobs1 =
	{
		22124,
		22125,
		22126,
		22127,
		22129
	};
	
	private static final int[] mobs2 =
	{
		22134,
		22135
	};
	
	public Monastery()
	{
		super(Monastery.class.getSimpleName(), "ai/group");
		
		registerMobs(mobs1, QuestEventType.ON_AGGRO_RANGE_ENTER, QuestEventType.ON_SPAWN, QuestEventType.ON_SPELL_FINISHED);
		registerMobs(mobs2, QuestEventType.ON_SKILL_SEE);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (!npc.isInCombat())
		{
			if (player.getActiveWeaponInstance() != null)
			{
				npc.setTarget(player);
				npc.broadcastNpcSay(((player.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + player.getName() + ", move your weapon away!");
				
				switch (npc.getNpcId())
				{
					case 22124:
					case 22126:
						npc.doCast(SkillTable.getInstance().getInfo(4589, 8));
						break;
					
					default:
						attack(((L2Attackable) npc), player);
						break;
				}
			}
			else if (((L2Attackable) npc).getMostHated() == null)
				return null;
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.getSkillType() == L2SkillType.AGGDAMAGE && targets.length != 0)
		{
			for (L2Object obj : targets)
			{
				if (obj.equals(npc))
				{
					npc.broadcastNpcSay(((caster.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + caster.getName() + ", move your weapon away!");
					attack(((L2Attackable) npc), caster);
					break;
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		for (L2PcInstance target : L2World.getInstance().getVisibleObjects(npc, L2PcInstance.class))
		{
			if (!target.isDead() && GeoEngine.canSeeTarget(npc, target, false) && Util.checkIfInRange(npc.getAggroRange(), npc, target, true))
			{
				if (target.getActiveWeaponInstance() != null && !npc.isInCombat() && npc.getTarget() == null)
				{
					npc.setTarget(target);
					npc.broadcastNpcSay(((target.getAppearance().getSex() == Sex.FEMALE) ? "Sister " : "Brother ") + target.getName() + ", move your weapon away!");
					
					switch (npc.getNpcId())
					{
						case 22124:
						case 22126:
						case 22127:
							npc.doCast(SkillTable.getInstance().getInfo(4589, 8));
							break;
						
						default:
							attack(((L2Attackable) npc), target);
							break;
					}
				}
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (skill.getId() == 4589)
			attack(((L2Attackable) npc), player);
		
		return super.onSpellFinished(npc, player, skill);
	}
	
	public static void main(String[] args)
	{
		new Monastery();
	}
}
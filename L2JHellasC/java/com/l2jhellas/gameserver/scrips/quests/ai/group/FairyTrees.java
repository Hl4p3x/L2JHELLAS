package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.Rnd;

public class FairyTrees extends AbstractNpcAI
{
	private static final int[] mobs =
	{
		27185,
		27186,
		27187,
		27188
	};
	
	public FairyTrees()
	{
		super("fairy_trees", "ai");
		registerMobs(mobs);
		super.addSpawnId(27189);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (contains(mobs, npcId))
		{
			for (int i = 0; i < 20; i++)
			{
				L2Attackable newNpc = (L2Attackable) addSpawn(27189, npc.getX(), npc.getY(), npc.getZ(), 0, false, 30000, false);
				L2Character originalKiller = isPet ? killer.getPet() : killer;
				newNpc.setRunning();
				newNpc.addDamageHate(originalKiller, 0, 999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalKiller);
				if (Rnd.get(1, 2) == 1)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(4243, 1);
					if (skill != null && originalKiller != null)
						skill.getEffects(newNpc, originalKiller);
				}
			}
		}
		
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new FairyTrees();
	}
}
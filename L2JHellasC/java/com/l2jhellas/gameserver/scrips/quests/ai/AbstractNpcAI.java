package com.l2jhellas.gameserver.scrips.quests.ai;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.skills.FrequentSkill;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public abstract class AbstractNpcAI extends Quest
{
	public AbstractNpcAI(String name, String descr)
	{
		super(-1, name, descr);
	}
	
	public void registerMobs(int[] mobs, QuestEventType... types)
	{
		for (int id : mobs)
		{
			for (QuestEventType type : types)
				addEventId(id, type);
		}
	}
	
	public static void attack(L2Attackable npc, L2Playable playable, int aggro)
	{
		npc.setIsRunning(true);
		npc.addDamageHate(playable, 0, (aggro <= 0) ? 999 : aggro);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, playable);
	}
	
	public static L2PcInstance getRandomPlayer(L2Npc npc)
	{
		List<L2PcInstance> result = new ArrayList<>();
		
		for (L2PcInstance player : L2World.getInstance().getVisibleObjects(npc, L2PcInstance.class))
		{
			if (player.isDead())
				continue;
			
			if (player.isGM() && !player.getAppearance().isVisible())
				continue;
			
			result.add(player);
		}
		
		return (result.isEmpty()) ? null : result.get(Rnd.get(result.size()));
	}
	
	public static int getPlayersCountInRadius(int range, L2Character npc, boolean invisible)
	{
		int count = 0;
		for (L2PcInstance player : L2World.getInstance().getVisibleObjects(npc, L2PcInstance.class))
		{
			if (player.isDead())
				continue;
			
			if (!invisible && !player.getAppearance().isVisible())
				continue;
			
			if (Util.checkIfInRange(range, npc, player, true))
				count++;
		}
		return count;
	}
	
	public static int[] getPlayersCountInPositions(int range, L2Character npc, boolean invisible)
	{
		int frontCount = 0;
		int backCount = 0;
		int sideCount = 0;
		
		for (L2PcInstance player : L2World.getInstance().getVisibleObjects(npc, L2PcInstance.class))
		{
			if (player.isDead())
				continue;
			
			if (!invisible && !player.getAppearance().isVisible())
				continue;
			
			if (!Util.checkIfInRange(range, npc, player, true))
				continue;
			
			if (player.isFrontOf(npc))
				frontCount++;
			else if (player.isBehindOf(npc))
				backCount++;
			else
				sideCount++;
		}
		
		int[] array =
		{
			frontCount,
			backCount,
			sideCount
		};
		return array;
	}
	
	public static <T> boolean contains(T[] array, T obj)
	{
		for (T element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean contains(int[] array, int obj)
	{
		for (int element : array)
		{
			if (element == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	public static void attack(L2Attackable npc, L2Playable playable)
	{
		attack(npc, playable, 0);
	}
	
	public static int getRandom(int max)
	{
		return Rnd.get(max);
	}
	
	public static int getRandom(int min, int max)
	{
		return Rnd.get(min, max);
	}
	
	protected static boolean testCursesOnAttack(L2Npc npc,L2Character attacker)
	{
		return testCursesOnAttack(npc, attacker, npc.getNpcId());
	}
	
	protected static boolean testCursesOnAttack(L2Npc npc,L2Character attacker, int npcId)
	{
		if (Config.RAID_DISABLE_CURSE)
			return false;
		
		if (attacker.getLevel() - npc.getLevel() > 8)
		{
			final L2Skill curse = FrequentSkill.RAID_CURSE2.getSkill();
			if (attacker.getFirstEffect(curse) == null)
			{
				npc.broadcastPacket(new MagicSkillUse(npc, attacker, curse.getId(), curse.getLevel(), 300, 0));
				curse.getEffects(npc, attacker);
				
				((L2Attackable) npc).stopHating(attacker);
				return true;
			}
		}
		
		if (npc.getNpcId() == npcId && attacker.isPlayer() && ((L2PcInstance) attacker).isMounted())
		{
			final L2Skill curse = FrequentSkill.RAID_ANTI_STRIDER_SLOW.getSkill();
			if (attacker.getFirstEffect(curse) == null)
			{
				npc.broadcastPacket(new MagicSkillUse(npc, attacker, curse.getId(), curse.getLevel(), 300, 0));
				curse.getEffects(npc, attacker);
			}
		}
		return false;
	}
}
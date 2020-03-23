package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public final class PlainsOfDion extends AbstractNpcAI
{
	private static final int MONSTERS[] =
	{
		21104, // Delu Lizardman Supplier
		21105, // Delu Lizardman Special Agent
		21107, // Delu Lizardman Commander
	};
	
	private static final String[] MONSTERS_MSG =
	{
		"$s1! How dare you interrupt our fight! Hey guys, help!",
		"$s1! Hey! We're having a duel here!",
		"The duel is over! Attack!",
		"Foul! Kill the coward!",
		"How dare you interrupt a sacred duel! You must be taught a lesson!"
	};
	
	private static final String[] MONSTERS_ASSIST_MSG =
	{
		"Die, you coward!",
		"Kill the coward!",
		"What are you looking at?"
	};
	
	public PlainsOfDion()
	{
		super(PlainsOfDion.class.getSimpleName(), "ai/group");
		addAttackId(MONSTERS);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (npc.isScriptValue(0))
		{
			npc.broadcastNpcSay(MONSTERS_MSG[Rnd.get(5)].replace("$s1", player.getName()));
			
			for (L2MonsterInstance obj : L2World.getInstance().getVisibleObjects(npc, L2MonsterInstance.class, 300))
			{
				if (Util.contains(MONSTERS, obj.getNpcId()) && !obj.isAttackingNow() && !obj.isDead() && GeoEngine.canSeeTarget(npc, obj, false))
				{
					attack(obj, player);
					obj.broadcastNpcSay(MONSTERS_ASSIST_MSG[Rnd.get(3)]);
				}
			}
			npc.setScriptValue(1);
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new PlainsOfDion();
	}
}
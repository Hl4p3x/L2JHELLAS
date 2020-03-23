package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.util.Util;

public class FleeingNPCs extends AbstractNpcAI
{
	private final int[] _npcId =
	{
		20002, // Rabbit
		20432 // Elpy
	};
	
	public FleeingNPCs()
	{
		super(FleeingNPCs.class.getSimpleName(), "ai/group");
		registerMobs(_npcId, QuestEventType.ON_ATTACK);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{	
		npc.disableCoreAI(true);
		npc.setRunning();	
		
		if (!npc.isMoving() && !npc.isDead() && !npc.isMovementDisabled() && npc.getMoveSpeed() > 0)
		{
		  final L2Summon summon = isPet ? attacker.getPet() : null;
		  final L2Character attackerLoc = summon == null ? attacker : summon;
		  final double angle = Math.toRadians(Util.calculateAngleFrom(attackerLoc, npc));
		  final int posX = (int) (npc.getX() + (490 * Math.cos(angle)));
		  final int posY = (int) (npc.getY() + (490 * Math.sin(angle)));
		  final int posZ = npc.getZ();			  
		  
		  if (GeoEngine.canMoveToCoord(npc.getX(), npc.getY(), npc.getZ(),posX, posY,posZ))
			  npc.getAI().moveTo(posX, posY,posZ);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new FleeingNPCs();
	}
}
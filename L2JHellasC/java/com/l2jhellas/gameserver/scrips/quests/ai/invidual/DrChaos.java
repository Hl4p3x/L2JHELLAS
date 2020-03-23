package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.datatables.sql.SpawnTable;
import com.l2jhellas.gameserver.enums.sound.Music;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.network.serverpackets.NpcSay;
import com.l2jhellas.gameserver.network.serverpackets.PlaySound;
import com.l2jhellas.gameserver.network.serverpackets.SocialAction;
import com.l2jhellas.gameserver.network.serverpackets.SpecialCamera;

public class DrChaos extends Quest
{
	
	private static final int DOCTER_CHAOS = 32033;
	private static final int STRANGE_MACHINE = 32032;
	private static final int CHAOS_GOLEM = 25512;
	private static boolean _IsGolemSpawned;
	
	public DrChaos()
	{
		super(-1, "Doctor Chaos", "ai");
		
		addFirstTalkId(32033);
		_IsGolemSpawned = false;
	}
	
	public L2Npc FindTemplate(int npcId)
	{
		L2Spawn spawn = SpawnTable.getInstance().getSpawn(npcId);
		return spawn != null ? spawn.getLastSpawn() : null;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		// if (event.equalsIgnoreCase("1"))
		if (event.equalsIgnoreCase("Chat"))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Fools! Why haven't you fled yet? Prepare you learn a lesson!"));
			startQuestTimer("1", 3000, npc, player, false);
		}
		else if (event.equalsIgnoreCase("1"))
		{
			L2Npc machine_instance = FindTemplate(STRANGE_MACHINE);
			if (machine_instance != null)
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, machine_instance);
				machine_instance.broadcastPacket(new SpecialCamera(machine_instance.getObjectId(), 1, -200, 15, 10000, 20000));
			}
			else
				// print "Dr Chaos AI: problem finding Strange Machine (npcid = "+STRANGE_MACHINE+"). Error: not spawned!"
				startQuestTimer("2", 2000, npc, player, false);
			startQuestTimer("3", 10000, npc, player, false);
		}
		else if (event.equalsIgnoreCase("2"))
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3), 1200);
		else if (event.equalsIgnoreCase("3"))
		{
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1, -150, 10, 3000, 20000));
			startQuestTimer("4", 2500, npc, player, false);
		}
		else if (event.equalsIgnoreCase("4"))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(96055, -110759, -3312, 0));
			startQuestTimer("5", 2000, npc, player, false);
		}
		else if (event.equalsIgnoreCase("5"))
		{
			player.teleToLocation(94832, -112624, -3304);
			npc.teleToLocation(-113091, -243942, -15536);
			if (!_IsGolemSpawned)
			{
				L2Npc golem = addSpawn(CHAOS_GOLEM, 94640, -112496, -3336, 0, false, 0, false);
				_IsGolemSpawned = true;
				startQuestTimer("6", 1000, golem, player, false);
				
				PlaySound _snd = Music.RM03_A.getPacket();
				player.sendPacket(_snd);
			}
		}
		else if (event.equalsIgnoreCase("6"))
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 30, -200, 20, 6000, 8000));
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == DOCTER_CHAOS)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "How dare you trespass into my territory? Have you no fear?"));
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(96323, -110914, -3328, 0));
			startQuestTimer("Chat", 3000, npc, player, false);
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new DrChaos();
	}
}
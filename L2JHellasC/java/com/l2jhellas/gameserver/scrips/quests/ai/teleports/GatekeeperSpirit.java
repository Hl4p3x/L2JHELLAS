package com.l2jhellas.gameserver.scrips.quests.ai.teleports;

import com.l2jhellas.gameserver.SevenSigns;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;

public class GatekeeperSpirit extends Quest
{
	private final static int EnterGk = 31111;
	private final static int ExitGk = 31112;
	private final static int Lilith = 25283;
	private final static int Anakim = 25286;
	
	public GatekeeperSpirit()
	{
		super(-1, "GatekeeperSpirit", "teleports");
		
		addStartNpc(EnterGk);
		addFirstTalkId(EnterGk);
		addTalkId(EnterGk);
		
		addKillId(Lilith, Anakim);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("spawn_exitgk_lilith"))
		{
			// exit_necropolis_boss_lilith
			addSpawn(ExitGk, 184446, -10112, -5488, 0, false, 900000, false);
		}
		else if (event.equalsIgnoreCase("spawn_exitgk_anakim"))
		{
			// exit_necropolis_boss_anakim
			addSpawn(ExitGk, 184466, -13106, -5488, 0, false, 900000, false);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
		int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
		int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		
		if (playerCabal == sealAvariceOwner && playerCabal == compWinner)
		{
			switch (sealAvariceOwner)
			{
				case SevenSigns.CABAL_DAWN:
					return "dawn.htm";
					
				case SevenSigns.CABAL_DUSK:
					return "dusk.htm";
			}
		}
		
		npc.showChatWindow(player);
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case Lilith:
				if (getQuestTimer("spawn_exitgk_lilith", null, null) == null)
					startQuestTimer("spawn_exitgk_lilith", 10000, null, null, false);
				break;
			
			case Anakim:
				if (getQuestTimer("spawn_exitgk_lilith", null, null) == null)
					startQuestTimer("spawn_exitgk_anakim", 10000, null, null, false);
				break;
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new GatekeeperSpirit();
	}
}
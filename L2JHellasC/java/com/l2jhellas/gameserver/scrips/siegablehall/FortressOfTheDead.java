package com.l2jhellas.gameserver.scrips.siegablehall;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.scrips.siegable.ClanHallSiegeEngine;

public final class FortressOfTheDead extends ClanHallSiegeEngine
{
	private static final int LIDIA = 35629;
	private static final int ALFRED = 35630;
	private static final int GISELLE = 35631;
	
	private static final String LIDIA_SPAWN_SHOUT = "Hmm, those who are not of the bloodline are coming this way to take over the castle?!  Humph!  The bitter grudges of the dead.  You must not make light of their power!";
	private static final String ALFRED_SPAWN_SHOUT = "Heh Heh... I see that the feast has begun! Be wary! The curse of the Hellmann family has poisoned this land!";
	private static final String GISELLE_SPAWN_SHOUT = "Arise, my faithful servants! You, my people who have inherited the blood.  It is the calling of my daughter.  The feast of blood will now begin!";
	
	private static final String ALFRED_GISELLE_DEATH_SHOUT = "Aargh...!  If I die, then the magic force field of blood will...!";
	private static final String LIDIA_DEATH_SHOUT = "Grarr! For the next 2 minutes or so, the game arena are will be cleaned. Throw any items you don't need to the floor now.";
	
	private final Map<Integer, Integer> _damageToLidia = new ConcurrentHashMap<>();
	
	public FortressOfTheDead()
	{
		super("FortressOfTheDead" , "siegablehall", FORTRESS_OF_DEAD);
		
		addAttackId(LIDIA);
		addKillId(LIDIA, ALFRED, GISELLE);
		addSpawnId(LIDIA, ALFRED, GISELLE);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (!_hall.isInSiege() || !(attacker instanceof L2Playable))
			return null;
		
		final L2Clan clan = attacker.getActingPlayer().getClan();
		if (clan != null && getAttackerClan(clan) != null)
			_damageToLidia.merge(clan.getClanId(), damage, Integer::sum);
		
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (!_hall.isInSiege())
			return null;
		
		final int npcId = npc.getNpcId();
		
		if (npcId == ALFRED || npcId == GISELLE)
			broadcastNpcSay(npc, ChatType.SHOUT.ordinal(), ALFRED_GISELLE_DEATH_SHOUT);
		else if (npcId == LIDIA)
		{
			broadcastNpcSay(npc, ChatType.SHOUT.ordinal(), LIDIA_DEATH_SHOUT);
			
			_missionAccomplished = true;
			
			cancelSiegeTask();
			endSiege();
		}
		
		return super.onKill(npc, player , isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getNpcId() == LIDIA)
			broadcastNpcSay(npc, ChatType.SHOUT.ordinal(), LIDIA_SPAWN_SHOUT);
		else if (npc.getNpcId() == ALFRED)
			broadcastNpcSay(npc, ChatType.SHOUT.ordinal(), ALFRED_SPAWN_SHOUT);
		else if (npc.getNpcId() == GISELLE)
			broadcastNpcSay(npc, ChatType.SHOUT.ordinal(), GISELLE_SPAWN_SHOUT);
		
		return super.onSpawn(npc);
	}
	
	@Override
	public L2Clan getWinner()
	{
		// If none did damages, simply return null.
		if (_damageToLidia.isEmpty())
			return null;
		
		// Retrieve clanId who did the biggest amount of damage.		
		int clanId = 0;
		long counter = 0;
		for (Entry<Integer, Integer> e : _damageToLidia.entrySet())
		{
			long dam = e.getValue();
			if (dam > counter) 
			{
				clanId = e.getKey();
				counter = dam;
			}
		}
		
		// Clear the Map for future usage.
		_damageToLidia.clear();
		
		// Return the Clan winner.
		return ClanTable.getInstance().getClan(clanId);
	}
	
	@Override
	public void startSiege()
	{
		// Siege must start at night
		final int hoursLeft = (GameTimeController.getInstance().getGameTime() / 60) % 24;
		if (hoursLeft < 0 || hoursLeft > 6)
		{
			cancelSiegeTask();
			
			long scheduleTime = (24 - hoursLeft) * 600000L;
			_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(this::startSiege, scheduleTime);
		}
		else
			super.startSiege();
	}
	
	public static void main(String[] args)
	{
		new FortressOfTheDead();
	}
}

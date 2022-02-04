package com.l2jhellas.gameserver.scrips.siegablehall;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.spawn.L2Spawn;
import com.l2jhellas.gameserver.scrips.siegable.ClanHallSiegeEngine;

public final class FortressOfResistance extends ClanHallSiegeEngine
{
	private static final int MESSENGER = 35382;
	private static final int BLOODY_LORD_NURKA = 35375;

	private final Map<Integer, Integer> _damageToNurka = new ConcurrentHashMap<>();
	
	private L2Spawn _nurka;
	
	public FortressOfResistance()
	{
		super("FortressOfResistance" , "siegablehall", FORTRESS_RESSISTANCE);
		
		addFirstTalkId(MESSENGER);
		addKillId(BLOODY_LORD_NURKA);
		addAttackId(BLOODY_LORD_NURKA);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return getHtmlText("partisan_ordery_brakel001.htm").replace("%nextSiege%", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_hall.getSiegeDate().getTime()));
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (!_hall.isInSiege() || !(attacker instanceof L2Playable))
			return null;
		
		final L2Clan clan = attacker.getActingPlayer().getClan();
		if (clan != null)
			_damageToNurka.merge(clan.getClanId(), damage, Integer::sum);
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (!_hall.isInSiege())
			return null;
		
		_missionAccomplished = true;
		
		npc.getSpawn().stopRespawn();
		npc.deleteMe();
		
		cancelSiegeTask();
		endSiege();
		return super.onKill(npc, player , isPet);
	}
	
	@Override
	public L2Clan getWinner()
	{
		// If none did damages, simply return null.
		if (_damageToNurka.isEmpty())
			return null;
		
		// Retrieve clanId who did the biggest amount of damage.		
		int clanId = 0;
		long counter = 0;
		for (Entry<Integer, Integer> e : _damageToNurka.entrySet())
		{
			long dam = e.getValue();
			if (dam > counter) 
			{
				clanId = e.getKey();
				counter = dam;
			}
		}
		
		// Clear the Map for future usage.
		_damageToNurka.clear();
		
		// Return the Clan winner.
		return ClanTable.getInstance().getClan(clanId);
	}
	
	@Override
	public void onSiegeStarts()
	{
		_nurka.doSpawn();
	}
	
	public static void main(String[] args)
	{
		new FortressOfResistance();
	}
}
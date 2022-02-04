package com.l2jhellas.gameserver.scrips.siegablehall;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.scrips.siegable.ClanHallSiegeEngine;
import com.l2jhellas.gameserver.skills.SkillTable;

public final class DevastatedCastle extends ClanHallSiegeEngine
{
	private static final int GUSTAV = 35410;
	private static final int MIKHAIL = 35409;
	private static final int DIETRICH = 35408;
	
	private static final String MIKHAIL_SPAWN_SHOUT = "Glory to Aden, the Kingdom of the Lion! Glory to Sir Gustav, our immortal lord!";
	private static final String DIETRICH_SPAWN_SHOUT = "Soldiers of Gustav, go forth and destroy the invaders!";
	private static final String GUSTAV_ATTACK_SHOUT = "This is unbelievable! Have I really been defeated? I shall return and take your head!";
	
	private final Map<Integer, Integer> _damageToGustav = new ConcurrentHashMap<>();
	
	public DevastatedCastle()
	{
		super("DevastatedCastle" , "siegablehall", DEVASTATED_CASTLE);
		
		addKillId(GUSTAV);
		addSpawnId(MIKHAIL, DIETRICH);
		addAttackId(GUSTAV);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getNpcId() == MIKHAIL)
			broadcastNpcSay(npc, ChatType.SHOUT.ordinal(), MIKHAIL_SPAWN_SHOUT);
		else if (npc.getNpcId() == DIETRICH)
			broadcastNpcSay(npc, ChatType.SHOUT.ordinal(), DIETRICH_SPAWN_SHOUT);
		
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (!_hall.isInSiege() || !(attacker instanceof L2Playable))
			return null;
		
		final L2Clan clan = attacker.getActingPlayer().getClan();
		if (clan != null && getAttackerClan(clan) != null)
			_damageToGustav.merge(clan.getClanId(), damage, Integer::sum);
		
		if ((npc.getCurrentHp() < npc.getMaxHp() / 12) && npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST)
		{
			broadcastNpcSay(npc, ChatType.GENERAL.ordinal(), GUSTAV_ATTACK_SHOUT);
			
			L2Object target = npc.getTarget();
			npc.setTarget(npc);
			npc.doCast(SkillTable.getInstance().getInfo(4235, 1));
			npc.setTarget(target);			
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (!_hall.isInSiege())
			return null;
		
		_missionAccomplished = true;
		
		if (npc.getNpcId() == GUSTAV)
		{
			cancelSiegeTask();
			endSiege();
		}
		
		return super.onKill(npc, player , isPet);
	}
	
	@Override
	public L2Clan getWinner()
	{
		// If none did damages, simply return null.
		if (_damageToGustav.isEmpty())
			return null;
		
		// Retrieve clanId who did the biggest amount of damage.		
		int counter = 0;
		int clanId = 0;
		for (Entry<Integer, Integer> e : _damageToGustav.entrySet())
		{
			final int damage = e.getValue();
			if (damage > counter)
			{
				counter = damage;
				clanId = e.getKey();
			}
		}
		
		_damageToGustav.clear();
		
		// Return the Clan winner.
		return ClanTable.getInstance().getClan(clanId);
	}
	
	public static void main(String[] args)
	{
		new DevastatedCastle();
	}
}
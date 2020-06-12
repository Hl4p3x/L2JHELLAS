package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.sound.Music;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.Rnd;

public class Core extends AbstractNpcAI
{
	private static final int CORE = 29006;
	private static final int DEATH_KNIGHT = 29007;
	private static final int DOOM_WRAITH = 29008;
	private static final int SUSCEPTOR = 29011;
	
	private static final byte ALIVE = 0; // Core is spawned.
	private static final byte DEAD = 1; // Core has been killed.
	
	List<L2Attackable> _minions = new CopyOnWriteArrayList<>();
	
	public Core()
	{
		super(Core.class.getSimpleName(), "ai/individual");
		
		addAttackId(CORE);
		addKillId(CORE, DEATH_KNIGHT, DOOM_WRAITH, SUSCEPTOR);
		
		final StatsSet info = GrandBossManager.getStatsSet(CORE);
		final int status = GrandBossManager.getInstance().getBossStatus(CORE);
		if (status == DEAD)
		{
			// load the unlock date and time for Core from DB
			final long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
			{
				// The time has not yet expired. Mark Core as currently locked (dead).
				startQuestTimer("core_unlock", temp, null, null, false);
			}
			else
			{
				// The time has expired while the server was offline. Spawn Core.
				final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0, false);
				GrandBossManager.setBossStatus(CORE, ALIVE);
				spawnBoss(core);
			}
		}
		else
		{
			final int loc_x = info.getInteger("loc_x");
			final int loc_y = info.getInteger("loc_y");
			final int loc_z = info.getInteger("loc_z");
			final int heading = info.getInteger("heading");
			final int hp = info.getInteger("currentHP");
			final int mp = info.getInteger("currentMP");
			
			final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, loc_x, loc_y, loc_z, heading, false, 0, false);
			core.setCurrentHpMp(hp, mp);
			spawnBoss(core);
		}
	}
	
	public void spawnBoss(L2GrandBossInstance npc)
	{
		GrandBossManager.addBoss(npc);
		npc.broadcastPacket(Music.BS01_A_10000.getPacket());
		
		// Spawn minions
		L2Attackable mob;
		for (int i = 0; i < 5; i++)
		{
			int x = 16800 + i * 360;
			mob = (L2Attackable) addSpawn(DEATH_KNIGHT, x, 110000, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			mob.setIsRaidMinion(true);
			_minions.add(mob);
			mob = (L2Attackable) addSpawn(DEATH_KNIGHT, x, 109000, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			mob.setIsRaidMinion(true);
			_minions.add(mob);
			int x2 = 16800 + i * 600;
			mob = (L2Attackable) addSpawn(DOOM_WRAITH, x2, 109300, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			mob.setIsRaidMinion(true);
			_minions.add(mob);
		}
		
		for (int i = 0; i < 4; i++)
		{
			int x = 16800 + i * 450;
			mob = (L2Attackable) addSpawn(SUSCEPTOR, x, 110300, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			mob.setIsRaidMinion(true);
			_minions.add(mob);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("core_unlock"))
		{
			final L2GrandBossInstance core = (L2GrandBossInstance) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0, false);
			GrandBossManager.setBossStatus(CORE, ALIVE);
			spawnBoss(core);
		}
		else if (event.equalsIgnoreCase("spawn_minion"))
		{
			final L2Attackable mob = (L2Attackable) addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false);
			mob.setIsRaidMinion(true);
			_minions.add(mob);
		}
		else if (event.equalsIgnoreCase("despawn_minions"))
		{
			_minions.forEach(L2Npc::deleteMe);
			_minions.clear();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (attacker.isPlayable())
		{
			if (npc.isScriptValue(1))
			{
				if (Rnd.get(100) == 0)
					npc.broadcastNpcSay("Removing intruders.");
			}
			else
			{
				npc.setScriptValue(1);
				npc.broadcastNpcSay(
						"A non-permitted target has been discovered.");
				npc.broadcastNpcSay("Starting intruder removal system.");
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == CORE)
		{
			npc.broadcastPacket(Music.BS02_D_10000.getPacket());
			npc.broadcastNpcSay("A fatal error has occurred.");
			npc.broadcastNpcSay("System is being shut down...");
			npc.broadcastNpcSay("......");
			
			addSpawn(31842, 16502, 110165, -6394, 0, false, 900000, false);
			addSpawn(31842, 18948, 110166, -6397, 0, false, 900000, false);
			GrandBossManager.setBossStatus(CORE, DEAD);
			
			long respawnTime = (long) Config.Interval_Of_Core_Spawn + Rnd.get(-Config.Random_Of_Core_Spawn, Config.Random_Of_Core_Spawn);

			startQuestTimer("core_unlock", respawnTime, null, null, false);
			
			final StatsSet info = GrandBossManager.getStatsSet(CORE);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.setStatsSet(CORE, info);
			startQuestTimer("despawn_minions", 20000, null, null, false);
			cancelQuestTimers("spawn_minion");
		}
		else if (GrandBossManager.getInstance().getBossStatus(CORE) == ALIVE && _minions != null && _minions.contains(npc))
		{
			_minions.remove(npc);
			startQuestTimer("spawn_minion", 60000, npc, null, false);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Core();
	}
}
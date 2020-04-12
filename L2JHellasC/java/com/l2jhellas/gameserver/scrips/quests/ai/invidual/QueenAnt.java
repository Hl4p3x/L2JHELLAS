package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.network.serverpackets.SocialAction;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.Rnd;

public class QueenAnt extends AbstractNpcAI
{	
	private static L2BossZone _Zone = GrandBossManager.getZoneById(110012);
	
	private static final int QUEEN = 29001;
	private static final int LARVA = 29002;
	private static final int NURSE = 29003;
	private static final int GUARD = 29004;
	private static final int ROYAL = 29005;
	
	// QUEEN Status Tracking :
	private static final byte ALIVE = 0; // Queen Ant is spawned.
	private static final byte DEAD = 1; // Queen Ant has been killed.

	private static List<L2Attackable> _Minions = new ArrayList<>();
	
	int[] mobs ={QUEEN,LARVA,NURSE,GUARD,ROYAL};
	
	public QueenAnt()
	{
		super("Queen Ant", "ai/individual");

		addSpawnId(mobs);
		addKillId(mobs);
		addAggroRangeEnterId(mobs);
		addFactionCallId(NURSE);
		
		StatsSet info = GrandBossManager.getStatsSet(QUEEN);
		int status = GrandBossManager.getInstance().getBossStatus(QUEEN);
		if (status == DEAD)
		{
			long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			if (temp > 0)
				startQuestTimer("queen_unlock", temp, null, null, false);
			else
			{
				GrandBossManager.setBossStatus(QUEEN, ALIVE);
				L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0, false);
				spawnBoss(queen);
			}
		}
		else
		{
			int loc_x = info.getInteger("loc_x");
			int loc_y = info.getInteger("loc_y");
			int loc_z = info.getInteger("loc_z");
			int heading = info.getInteger("heading");
			int hp = info.getInteger("currentHP");
			int mp = info.getInteger("currentMP");
			if (!_Zone.isInsideZone(loc_x, loc_y, loc_z))
			{
				loc_x = -21610;
				loc_y = 181594;
				loc_z = -5734;
			}
			L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, loc_x, loc_y, loc_z, heading, false, 0, false);
			queen.setCurrentHpMp(hp, mp);
			spawnBoss(queen);
		}
	}
	
	public void spawnBoss(L2GrandBossInstance npc)
	{
		if (Rnd.get(100) < 33)
			_Zone.movePlayersTo(-19480, 187344, -5600);
		else if (Rnd.get(100) < 50)
			_Zone.movePlayersTo(-17928, 180912, -5520);
		else
			_Zone.movePlayersTo(-23808, 182368, -5600);
		
		GrandBossManager.addBoss(npc);
		
		startQuestTimer("action", 10000, npc, null, true);
		// Spawn minions
		addSpawn(LARVA, -21600, 179482, -5846, Rnd.get(360), false, 0, false).setIsRaidMinion(true);
		addSpawn(NURSE, -22000, 179482, -5846, 0, false, 0, false).setIsRaidMinion(true);
		addSpawn(NURSE, -21200, 179482, -5846, 0, false, 0, false).setIsRaidMinion(true);
		int radius = 400;
		for (int i = 0; i < 6; i++)
		{
			int x = (int) (radius * Math.cos(i * 1.407)); // 1.407~2pi/6
			int y = (int) (radius * Math.sin(i * 1.407));
			addSpawn(NURSE, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0, false).setIsRaidMinion(true);
		}
		for (int i = 0; i < 8; i++)
		{
			int x = (int) (radius * Math.cos(i * .7854)); // .7854~2pi/8
			int y = (int) (radius * Math.sin(i * .7854));
			L2Npc mob = addSpawn(ROYAL, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0, false);
			mob.setIsRaidMinion(true);
			_Minions.add((L2Attackable) mob);
		}
		startQuestTimer("check_royal__Zone", 120000, npc, null, true);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("action") && npc != null)
		{
			if (Rnd.get(3) == 0)
			{
				if (Rnd.get(2) == 0)
				{
					npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
				}
				else
				{
					npc.broadcastPacket(new SocialAction(npc.getObjectId(), 4));
				}
			}
		}
		else if (event.equalsIgnoreCase("queen_unlock"))
		{
			L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0, false);
			GrandBossManager.setBossStatus(QUEEN, ALIVE);
			spawnBoss(queen);
		}
		else if (event.equalsIgnoreCase("check_royal__Zone") && npc != null)
		{
			for (int i = 0; i < _Minions.size(); i++)
			{
				L2Attackable mob = _Minions.get(i);
				if (mob != null && !_Zone.isInsideZone(mob))
				{
					mob.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
				}
			}
		}
		else if (event.equalsIgnoreCase("despawn_royals"))
		{
			for (int i = 0; i < _Minions.size(); i++)
			{
				L2Attackable mob = _Minions.get(i);
				if (mob != null)
				{
					mob.decayMe();
				}
			}
			_Minions.clear();
		}
		else if (event.equalsIgnoreCase("spawn_royal"))
		{
			L2Npc mob = addSpawn(ROYAL, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false);
			mob.setIsRaidMinion(true);
			_Minions.add((L2Attackable) mob);
		}
		else if (event.equalsIgnoreCase("spawn_nurse"))
		{
			addSpawn(NURSE, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false).setIsRaidMinion(true);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		if (caller == null || npc == null)
			return super.onFactionCall(npc, caller, attacker, isPet);
		int npcId = npc.getNpcId();
		int callerId = caller.getNpcId();
		if (npcId == NURSE)
		{
			if (callerId == LARVA)
			{
				npc.setTarget(caller);
				npc.doCast(SkillTable.getInstance().getInfo(4020, 1));
				npc.doCast(SkillTable.getInstance().getInfo(4024, 1));
				return null;
			}
			else if (callerId == QUEEN)
			{
				if (npc.getTarget() != null && npc.getTarget() instanceof L2Npc)
				{
					if (((L2Npc) npc.getTarget()).getNpcId() == LARVA)
					{
						return null;
					}
				}
				npc.setTarget(caller);
				npc.doCast(SkillTable.getInstance().getInfo(4020, 1));
				return null;
			}
		}
		return super.onFactionCall(npc, caller, attacker, isPet);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == NURSE)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == QUEEN)
		{
			GrandBossManager.setBossStatus(QUEEN, DEAD);
			// time is 36hour +/- 17hour
			long respawnTime = Config.Interval_Of_QueenAnt_Spawn + Rnd.get(Config.Random_Of_QueenAnt_Spawn);
			startQuestTimer("queen_unlock", respawnTime, null, null, false);
			cancelQuestTimer("action", npc, null);
			// also save the respawn time so that the info is maintained past reboots
			StatsSet info = GrandBossManager.getStatsSet(QUEEN);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.setStatsSet(QUEEN, info);
			startQuestTimer("despawn_royals", 20000, null, null, false);
			cancelQuestTimers("spawn_minion");
		}
		else if (GrandBossManager.getInstance().getBossStatus(QUEEN) == ALIVE)
		{
			if (npcId == ROYAL)
			{
				_Minions.remove(npc);
				startQuestTimer("spawn_royal", (280 + Rnd.get(40)) * 1000, npc, null, false);
			}
			else if (npcId == NURSE)
			{
				startQuestTimer("spawn_nurse", 10000, npc, null, false);
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new QueenAnt();
	}
}
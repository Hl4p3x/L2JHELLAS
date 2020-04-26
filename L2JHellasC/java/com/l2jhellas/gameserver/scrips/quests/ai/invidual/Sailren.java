package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.SocialAction;
import com.l2jhellas.gameserver.network.serverpackets.SpecialCamera;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.Rnd;

public class Sailren extends AbstractNpcAI
{
	private static final L2BossZone _nest = GrandBossManager.getZoneById(110015);
	
	public static final int SAILREN = 29065;
	
	public static final byte DORMANT = 0; // No one has entered yet. Entry is unlocked.
	public static final byte FIGHTING = 1; // A group entered in the nest. Entry is locked.
	public static final byte DEAD = 2; // Sailren has been killed. Entry is locked.
	
	private static final int VELOCIRAPTOR = 22223;
	private static final int PTEROSAUR = 22199;
	private static final int TREX = 22217;
	
	private static final int DUMMY = 32110;
	private static final int CUBE = 32107;
	
	private static final long INTERVAL_CHECK = 600000L; // 10 minutes
	
	private static final Location SAILREN_LOC = new Location(27549, -6638, -2008);
	
	private final List<L2Npc> _mobs = new CopyOnWriteArrayList<>();
	private static long _timeTracker;
	
	public Sailren()
	{
		super(Sailren.class.getSimpleName(), "ai/individual");
		
		addAttackId(VELOCIRAPTOR, PTEROSAUR, TREX, SAILREN);
		addKillId(VELOCIRAPTOR, PTEROSAUR, TREX, SAILREN);
		
		final StatsSet info = GrandBossManager.getStatsSet(SAILREN);
		
		switch (GrandBossManager.getInstance().getBossStatus(SAILREN))
		{
			case DEAD: // Launch the timer to set DORMANT, or set DORMANT directly if timer expired while offline.
				final long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
				if (temp > 0)
					startQuestTimer("unlock", temp, null, null, false);
				else
					GrandBossManager.setBossStatus(SAILREN, DORMANT);
				break;
			
			case FIGHTING:
				final int loc_x = info.getInteger("loc_x");
				final int loc_y = info.getInteger("loc_y");
				final int loc_z = info.getInteger("loc_z");
				final int heading = info.getInteger("heading");
				final int hp = info.getInteger("currentHP");
				final int mp = info.getInteger("currentMP");
				
				final L2Npc sailren = addSpawn(SAILREN, loc_x, loc_y, loc_z, heading, false, 0, false);
				GrandBossManager.addBoss((L2GrandBossInstance) sailren);
				_mobs.add(sailren);
				
				sailren.setCurrentHpMp(hp, mp);
				sailren.setRunning();
				
				// Don't need to edit _timeTracker, as it's initialized to 0.
				startQuestTimer("inactivity", INTERVAL_CHECK, null, null, true);
				break;
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("beginning"))
		{
			_timeTracker = 0;
			
			for (int i = 0; i < 3; i++)
			{
				final L2Npc temp = addSpawn(VELOCIRAPTOR, SAILREN_LOC, true, 0, false);
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				temp.setRunning();
				_mobs.add(temp);
			}
			startQuestTimer("inactivity", INTERVAL_CHECK, null, null, true);
		}
		else if (event.equalsIgnoreCase("spawn"))
		{
			// Dummy spawn used to cast the skill. Despawned after 26sec.
			final L2Npc temp = addSpawn(DUMMY, SAILREN_LOC, false, 26000, false);
			
			// Cast skill every 2,5sec.
			_nest.broadcastPacket(new MagicSkillUse(npc, npc, 5090, 1, 2500, 0));
			startQuestTimer("skill", 2500, temp, null, true);
			
			// Cinematic, meanwhile.
			_nest.broadcastPacket(new SpecialCamera(temp.getObjectId(), 60, 110, 30, 4000, 4000, 0, 65, 1, 0)); // 4sec
			
			startQuestTimer("camera_0", 3900, temp, null, false); // 3sec
			startQuestTimer("camera_1", 6800, temp, null, false); // 3sec
			startQuestTimer("camera_2", 9700, temp, null, false); // 3sec
			startQuestTimer("camera_3", 12600, temp, null, false); // 3sec
			startQuestTimer("camera_4", 15500, temp, null, false); // 3sec
			startQuestTimer("camera_5", 18400, temp, null, false); // 7sec
		}
		else if (event.equalsIgnoreCase("skill"))
			_nest.broadcastPacket(new MagicSkillUse(npc, npc, 5090, 1, 2500, 0));
		else if (event.equalsIgnoreCase("camera_0"))
			_nest.broadcastPacket(new SpecialCamera(npc.getObjectId(), 100, 180, 30, 3000, 3000, 0, 50, 1, 0));
		else if (event.equalsIgnoreCase("camera_1"))
			_nest.broadcastPacket(new SpecialCamera(npc.getObjectId(), 150, 270, 25, 3000, 3000, 0, 30, 1, 0));
		else if (event.equalsIgnoreCase("camera_2"))
			_nest.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 360, 20, 3000, 3000, 10, 15, 1, 0));
		else if (event.equalsIgnoreCase("camera_3"))
			_nest.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 450, 10, 3000, 3000, 0, 10, 1, 0));
		else if (event.equalsIgnoreCase("camera_4"))
		{
			_nest.broadcastPacket(new SpecialCamera(npc.getObjectId(), 160, 560, 0, 3000, 3000, 0, 10, 1, 0));
			
			final L2Npc temp = addSpawn(SAILREN, SAILREN_LOC, false, 0, false);
			GrandBossManager.addBoss((L2GrandBossInstance) temp);
			_mobs.add(temp);
			
			// Stop skill task.
			cancelQuestTimers("skill");
			_nest.broadcastPacket(new MagicSkillUse(npc, npc, 5091, 1, 2500, 0));
			
			temp.broadcastPacket(new SocialAction(temp.getObjectId(), 2));
		}
		else if (event.equalsIgnoreCase("camera_5"))
			_nest.broadcastPacket(new SpecialCamera(npc.getObjectId(), 70, 560, 0, 500, 7000, -15, 10, 1, 0));
		else if (event.equalsIgnoreCase("unlock"))
			GrandBossManager.setBossStatus(SAILREN, DORMANT);
		else if (event.equalsIgnoreCase("inactivity"))
		{
			// 10 minutes without any attack activity leads to a reset.
			if ((System.currentTimeMillis() - _timeTracker) >= INTERVAL_CHECK)
			{
				// Set it dormant.
				GrandBossManager.setBossStatus(SAILREN, DORMANT);
				
				// Delete all monsters and clean the list.
				if (!_mobs.isEmpty())
				{
					for (L2Npc mob : _mobs)
						mob.deleteMe();
					
					_mobs.clear();
				}
				
				// Oust all players from area.
				_nest.oustAllPlayers();
				
				// Cancel inactivity task.
				cancelQuestTimers("inactivity");
			}
		}
		else if (event.equalsIgnoreCase("oust"))
		{
			// Oust all players from area.
			_nest.oustAllPlayers();
		}
		
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (!_mobs.contains(npc) || !_nest.getAllowedPlayers().contains(killer.getObjectId()))
			return null;
		
		switch (npc.getNpcId())
		{
			case VELOCIRAPTOR:
				// Once the 3 Velociraptors are dead, spawn a Pterosaur.
				if (_mobs.remove(npc) && _mobs.isEmpty())
				{
					final L2Npc temp = addSpawn(PTEROSAUR, SAILREN_LOC, false, 0, false);
					temp.setRunning();
					temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
					_mobs.add(temp);
				}
				break;
			
			case PTEROSAUR:
				// Pterosaur is dead, spawn a Trex.
				if (_mobs.remove(npc))
				{
					final L2Npc temp = addSpawn(TREX, SAILREN_LOC, false, 0, false);
					temp.setRunning();
					temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
					temp.broadcastNpcSay("?");
					_mobs.add(temp);
				}
				break;
			
			case TREX:
				// Trex is dead, wait 5min and spawn Sailren.
				if (_mobs.remove(npc))
				{
					startQuestTimer("spawn", 5 * 60000, npc, killer, false);
				}
				break;
			
			case SAILREN:
				if (_mobs.remove(npc))
				{
					// Set Sailren as dead.
					GrandBossManager.setBossStatus(SAILREN, DEAD);
					
					// Spawn the Teleport Cube for 10min.
					addSpawn(CUBE, npc, false, INTERVAL_CHECK, false);
					
					// Cancel inactivity task.
					cancelQuestTimers("inactivity");
					
					long respawnTime = (long) Config.Interval_Of_Sailren_Spawn + Rnd.get(-Config.Random_Of_Sailren_Spawn, Config.Random_Of_Sailren_Spawn);
					
					startQuestTimer("oust", INTERVAL_CHECK, null, null, false);
					startQuestTimer("unlock", respawnTime, null, null, false);
					
					// Save the respawn time so that the info is maintained past reboots.
					final StatsSet info = GrandBossManager.getStatsSet(SAILREN);
					info.set("respawn_time", System.currentTimeMillis() + respawnTime);
					GrandBossManager.setStatsSet(SAILREN, info);
				}
				break;
		}
		
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (!_mobs.contains(npc) || !_nest.getAllowedPlayers().contains(attacker.getObjectId()))
			return null;
		
		// Actualize _timeTracker.
		_timeTracker = System.currentTimeMillis();
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Sailren();
	}
}
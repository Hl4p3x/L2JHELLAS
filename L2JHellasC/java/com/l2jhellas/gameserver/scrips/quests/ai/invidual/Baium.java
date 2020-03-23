package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.sound.Music;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.network.serverpackets.Earthquake;
import com.l2jhellas.gameserver.network.serverpackets.SocialAction;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public class Baium extends AbstractNpcAI
{
	private static L2BossZone _baiumLair = GrandBossManager.getZoneById(110002);
	
	private static final int STONE_BAIUM = 29025;
	private static final int LIVE_BAIUM = 29020;
	private static final int ARCHANGEL = 29021;
	
	// Baium status tracking
	public static final byte ASLEEP = 0; // baium is in the stone version, waiting to be woken up. Entry is unlocked.
	public static final byte AWAKE = 1; // baium is awake and fighting. Entry is locked.
	public static final byte DEAD = 2; // baium has been killed and has not yet spawned. Entry is locked.
	
	// Archangels spawns
	private final static int ANGEL_LOCATION[][] =
	{
		{
			114239,
			17168,
			10080,
			63544
		},
		{
			115780,
			15564,
			10080,
			13620
		},
		{
			114880,
			16236,
			10080,
			5400
		},
		{
			115168,
			17200,
			10080,
			0
		},
		{
			115792,
			16608,
			10080,
			0
		},
	};
	
	private L2Character _actualVictim;
	private long _LastAttackVsBaiumTime = 0;
	private final List<L2Npc> _Minions = new ArrayList<>(5);
	
	public Baium()
	{
		super(Baium.class.getSimpleName(), "ai/individual");
		
		int[] mob =
		{
			LIVE_BAIUM
		};
		
		// Quest NPC starter initialization
		addStartNpc(STONE_BAIUM);
		addTalkId(STONE_BAIUM);
		
		// Baium onAttack, onKill, onSpawn
		registerMobs(mob, QuestEventType.ON_ATTACK, QuestEventType.ON_KILL, QuestEventType.ON_SPAWN);
		
		final StatsSet info = GrandBossManager.getStatsSet(LIVE_BAIUM);
		final int status = GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM);
		
		if (status == DEAD)
		{
			// load the unlock date and time for baium from DB
			long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
			{
				// The time has not yet expired. Mark Baium as currently locked (dead).
				startQuestTimer("baium_unlock", temp, null, null, false);
			}
			else
			{
				// The time has expired while the server was offline. Spawn the stone-baium as ASLEEP.
				addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0, false);
				GrandBossManager.setBossStatus(LIVE_BAIUM, ASLEEP);
			}
		}
		else if (status == AWAKE)
		{
			final int loc_x = info.getInteger("loc_x");
			final int loc_y = info.getInteger("loc_y");
			final int loc_z = info.getInteger("loc_z");
			final int heading = info.getInteger("heading");
			final int hp = info.getInteger("currentHP");
			final int mp = info.getInteger("currentMP");
			
			final L2Npc baium = addSpawn(LIVE_BAIUM, loc_x, loc_y, loc_z, heading, false, 0, false);
			GrandBossManager.addBoss((L2GrandBossInstance) baium);
			
			baium.setCurrentHpMp(hp, mp);
			baium.setRunning();
			
			// start monitoring baium's inactivity
			_LastAttackVsBaiumTime = System.currentTimeMillis();
			startQuestTimer("baium_despawn", 60000, baium, null, true);
			startQuestTimer("skill_range", 2000, baium, null, true);
			
			// Spawns angels
			for (int[] element : ANGEL_LOCATION)
			{
				L2Npc angel = addSpawn(ARCHANGEL, element[0], element[1], element[2], element[3], false, 0, true);
				((L2Attackable) angel).setIsRaidMinion(true);
				angel.setRunning();
				_Minions.add(angel);
			}
			
			// Angels AI
			startQuestTimer("angels_aggro_reconsider", 5000, null, null, true);
		}
		else
			addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0, false);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (npc != null && npc.getNpcId() == LIVE_BAIUM)
		{
			if (event.equalsIgnoreCase("skill_range"))
			{
				callSkillAI(npc);
			}
			else if (event.equalsIgnoreCase("baium_neck"))
			{
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3), 1200);
			}
			else if (event.equalsIgnoreCase("sacrifice_waker"))
			{
				if (player != null)
				{
					// If player is far of Baium, teleport him back.
					if (!Util.checkIfInShortRadius(300, player, npc, true))
						player.teleToLocation(115929, 17349, 10077, false);
					
					// 60% to die.
					if (Rnd.get(100) < 60)
						player.doDie(npc);
				}
			}
			else if (event.equalsIgnoreCase("baium_roar"))
			{
				// Roar animation
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), 1), 1200);
				
				// Spawn angels
				for (int[] element : ANGEL_LOCATION)
				{
					L2Npc angel = addSpawn(ARCHANGEL, element[0], element[1], element[2], element[3], false, 0, true);
					((L2Attackable) angel).setIsRaidMinion(true);
					angel.setRunning();
					_Minions.add(angel);
				}
				
				// Angels AI
				startQuestTimer("angels_aggro_reconsider", 5000, null, null, true);
			}
			else if (event.equalsIgnoreCase("baium_move"))
			{
				npc.setIsInvul(false);
				npc.setRunning();
				
				// Start monitoring baium's inactivity and activate the AI
				_LastAttackVsBaiumTime = System.currentTimeMillis();
				
				startQuestTimer("baium_despawn", 60000, npc, null, true);
				startQuestTimer("skill_range", 2000, npc, null, true);
			}
			// despawn the live baium after 30 minutes of inactivity
			// also check if the players are cheating, having pulled Baium outside his zone...
			else if (event.equalsIgnoreCase("baium_despawn"))
			{
				if (_LastAttackVsBaiumTime + 1800000 < System.currentTimeMillis())
				{
					// despawn the live-baium
					npc.deleteMe();
					
					// Unspawn angels
					for (L2Npc minion : _Minions)
					{
						if (minion != null)
						{
							minion.getSpawn().stopRespawn();
							minion.deleteMe();
						}
					}
					_Minions.clear();
					
					addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0, false); // spawn stone-baium
					GrandBossManager.setBossStatus(LIVE_BAIUM, ASLEEP); // Baium isn't awaken anymore
					_baiumLair.oustAllPlayers();
					cancelQuestTimer("baium_despawn", npc, null);
				}
				else if ((_LastAttackVsBaiumTime + 300000 < System.currentTimeMillis()) && (npc.getCurrentHp() / npc.getMaxHp() < 0.75))
				{
					npc.setTarget(npc);
					npc.doCast(SkillTable.getInstance().getInfo(4135, 1));
				}
				else if (!_baiumLair.isInsideZone(npc))
					npc.teleToLocation(116033, 17447, 10104, false);
			}
		}
		else if (event.equalsIgnoreCase("baium_unlock"))
		{
			GrandBossManager.setBossStatus(LIVE_BAIUM, ASLEEP);
			addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0, false);
		}
		else if (event.equalsIgnoreCase("angels_aggro_reconsider"))
		{
			boolean updateTarget = false; // Update or no the target
			
			for (L2Npc minion : _Minions)
			{
				L2Attackable angel = ((L2Attackable) minion);
				if (angel == null)
					continue;
				
				L2Character victim = angel.getMostHated();
				
				if (Rnd.get(100) < 10) // Chaos time
					updateTarget = true;
				else
				{
					if (victim != null) // Target is a unarmed player ; clean aggro.
					{
						if (victim instanceof L2PcInstance && victim.getActiveWeaponInstance() == null)
						{
							angel.stopHating(victim); // Clean the aggro number of previous victim.
							updateTarget = true;
						}
					}
					else
						// No target currently.
						updateTarget = true;
				}
				
				if (updateTarget)
				{
					L2Character newVictim = getRandomTarget(minion);
					if (newVictim != null && victim != newVictim)
					{
						angel.addDamageHate(newVictim, 0, 10000);
						angel.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, newVictim);
					}
				}
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		String htmltext = "";
		
		if (_baiumLair == null)
		{
			_baiumLair = GrandBossManager.getZone(113100, 14500, 10077);
			
			if (_baiumLair == null)
				return "<html><body>Angelic Vortex:<br>You may not enter!</body></html>";
		}
		if (npcId == STONE_BAIUM && GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM) == ASLEEP)
		{
			GrandBossManager.setBossStatus(LIVE_BAIUM, AWAKE);
			
			final L2Npc baium = addSpawn(LIVE_BAIUM, npc, false, 0, false);
			baium.setIsInvul(true);
			
			GrandBossManager.addBoss((L2GrandBossInstance) baium);
			
			// First animation
			baium.broadcastPacket(new SocialAction(baium.getObjectId(), 2), 1200);
			baium.broadcastPacket(new Earthquake(baium.getX(), baium.getY(), baium.getZ(), 40, 10));
			
			// Second animation, waker sacrifice, followed by angels spawn, third animation and finally movement.
			startQuestTimer("baium_neck", 13000, baium, null, false);
			startQuestTimer("sacrifice_waker", 24000, baium, player, false);
			startQuestTimer("baium_roar", 28000, baium, null, false);
			startQuestTimer("baium_move", 35000, baium, null, false);
			
			// Delete the statue.
			npc.deleteMe();
		}
		return htmltext;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.disableCoreAI(true);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (!_baiumLair.isInsideZone(attacker))
		{
			attacker.doDie(attacker);
			return null;
		}
		
		if (npc.isInvul())
			return null;
		
		if (npc.getNpcId() == LIVE_BAIUM)
		{
			if (attacker.getMountType() == 1)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(4258, 1);
				if (attacker.getFirstEffect(skill) == null)
				{
					npc.setTarget(attacker);
					npc.doCast(skill);
				}
			}
			// update a variable with the last action against baium
			_LastAttackVsBaiumTime = System.currentTimeMillis();
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		cancelQuestTimer("baium_despawn", npc, null);
		npc.broadcastPacket(Music.BS01_D_10000.getPacket());
		
		// spawn the "Teleportation Cubic" for 15 minutes (to allow players to exit the lair)
		addSpawn(29055, 115203, 16620, 10078, 0, false, 900000, false);
		
		long respawnTime = (long) Config.Interval_Of_Baium_Spawn + Rnd.get(-Config.Random_Of_Baium_Spawn, Config.Random_Of_Baium_Spawn);
		respawnTime *= 3600000;
		
		GrandBossManager.setBossStatus(LIVE_BAIUM, DEAD);
		startQuestTimer("baium_unlock", respawnTime, null, null, false);
		
		StatsSet info = GrandBossManager.getStatsSet(LIVE_BAIUM);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		GrandBossManager.setStatsSet(LIVE_BAIUM, info);
		
		// Unspawn angels.
		for (L2Npc minion : _Minions)
		{
			if (minion != null)
			{
				minion.getSpawn().stopRespawn();
				minion.deleteMe();
			}
		}
		_Minions.clear();
		
		// Clean Baium AI
		cancelQuestTimer("skill_range", npc, null);
		
		// Clean angels AI
		cancelQuestTimer("angels_aggro_reconsider", null, null);
		
		return super.onKill(npc, killer, isPet);
	}
	
	private L2Character getRandomTarget(L2Npc npc)
	{
		int npcId = npc.getNpcId();
		List<L2Character> result = new ArrayList<>();
		
		for (L2Character obj : L2World.getInstance().getVisibleObjects(npc, L2Attackable.class))
		{
			if (obj instanceof L2PcInstance)
			{
				if (obj.isDead() || !(GeoEngine.canSeeTarget(npc, obj, false)))
					continue;
				
				if (((L2PcInstance) obj).isGM() && ((L2PcInstance) obj).getAppearance().getInvisible())
					continue;
				
				if (npcId == ARCHANGEL)
					continue;
				
				if (npcId == ARCHANGEL && ((L2PcInstance) obj).getActiveWeaponInstance() == null)
					continue;
				
				result.add(obj);
			}
			// Case of Archangels, they can hit Baium.
			else if (obj instanceof L2GrandBossInstance && npcId == ARCHANGEL)
				result.add(obj);
		}
		
		// If there's no players available, Baium and Angels are hitting each other.
		if (result.isEmpty())
		{
			if (npcId == LIVE_BAIUM) // Case of Baium. Angels should never be without target.
			{
				for (L2Npc minion : _Minions)
					if (minion != null)
						result.add(minion);
			}
		}
		
		return (result.isEmpty()) ? null : result.get(Rnd.get(result.size()));
	}
	
	private void callSkillAI(L2Npc npc)
	{
		if (npc.isInvul() || npc.isCastingNow())
			return;
		
		// Pickup a target if no or dead victim. If Baium was hitting an angel, 50% luck he reconsiders his target. 10% luck he decides to reconsiders his target.
		if (_actualVictim == null || _actualVictim.isDead() || (_actualVictim instanceof L2MonsterInstance && Rnd.get(10) < 5) || Rnd.get(10) == 0)
			_actualVictim = getRandomTarget(npc);
		
		// If result is null, return directly.
		if (_actualVictim == null)
			return;
		
		final L2Skill skill = SkillTable.getInstance().getInfo(getRandomSkill(npc), 1);
		
		// Adapt the skill range, because Baium is fat.
		if (Util.checkIfInRange(skill.getCastRange() + npc.getCollisionRadius(), npc, _actualVictim, true))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			npc.setTarget(skill.getId() == 4135 ? npc : _actualVictim);
			npc.doCast(skill);
		}
		else
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _actualVictim, null);
	}
	
	private static int getRandomSkill(L2Npc npc)
	{
		// Baium's selfheal. It happens exceptionaly.
		if (npc.getCurrentHp() / npc.getMaxHp() < 0.1)
		{
			if (Rnd.get(10000) == 777) // His lucky day.
				return 4135;
		}
		
		int skill = 4127; // Default attack if nothing is possible.
		final int chance = Rnd.get(100); // Remember, it's 0 to 99, not 1 to 100.
		
		// If Baium feels surrounded or see 2+ angels, he unleashes his wrath upon heads :).
		
		if (getPlayersCountInRadius(600, npc, false) >= 20 || L2World.getInstance().getVisibleObjects(npc, L2MonsterInstance.class, 600).size() >= 2)
		{
			if (chance < 25)
				skill = 4130;
			else if (chance >= 25 && chance < 50)
				skill = 4131;
			else if (chance >= 50 && chance < 75)
				skill = 4128;
			else if (chance >= 75 && chance < 100)
				skill = 4129;
		}
		else
		{
			if (npc.getCurrentHp() / npc.getMaxHp() > 0.75)
			{
				if (chance < 10)
					skill = 4128;
				else if (chance >= 10 && chance < 20)
					skill = 4129;
			}
			else if (npc.getCurrentHp() / npc.getMaxHp() > 0.5)
			{
				if (chance < 10)
					skill = 4131;
				else if (chance >= 10 && chance < 20)
					skill = 4128;
				else if (chance >= 20 && chance < 30)
					skill = 4129;
			}
			else if (npc.getCurrentHp() / npc.getMaxHp() > 0.25)
			{
				if (chance < 10)
					skill = 4130;
				else if (chance >= 10 && chance < 20)
					skill = 4131;
				else if (chance >= 20 && chance < 30)
					skill = 4128;
				else if (chance >= 30 && chance < 40)
					skill = 4129;
			}
			else
			{
				if (chance < 10)
					skill = 4130;
				else if (chance >= 10 && chance < 20)
					skill = 4131;
				else if (chance >= 20 && chance < 30)
					skill = 4128;
				else if (chance >= 30 && chance < 40)
					skill = 4129;
			}
		}
		return skill;
	}
	
	public static void main(String[] args)
	{
		new Baium();
	}
}
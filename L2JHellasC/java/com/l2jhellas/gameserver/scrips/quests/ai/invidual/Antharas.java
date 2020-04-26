package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.skills.FrequentSkill;
import com.l2jhellas.gameserver.enums.sound.Music;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.network.serverpackets.SocialAction;
import com.l2jhellas.gameserver.network.serverpackets.SpecialCamera;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.MathUtil;
import com.l2jhellas.util.Rnd;

public class Antharas extends AbstractNpcAI
{
	private static L2BossZone _Zone = GrandBossManager.getZoneById(110001);

	public static final int ANTHARAS = 29019;
	private static final int[] ANTHARAS_IDS ={29066,29067,29068};
	
	public static final byte DORMANT = 0; // No one has entered yet. Entry is unlocked.
	public static final byte WAITING = 1; // Someone has entered, triggering a 30 minute window for additional people to enter. Entry is unlocked.
	public static final byte FIGHTING = 2; // Antharas is engaged in battle, annihilating his foes. Entry is locked.
	public static final byte DEAD = 3; // Antharas has been killed. Entry is locked.
	
	private long _action = 0; 
	private L2PcInstance _actualVictim;
	private final List<L2Npc> _monsters = new CopyOnWriteArrayList<>();
	
	private int _antharasId; 
	private L2Skill _skillRegen; 
	private int _minionTimer;
	
	public Antharas()
	{
		super("antharas", "ai");
		int[] mob = {ANTHARAS,29066, 29067, 29068, 29069, 29070, 29071, 29072, 29073, 29074, 29075, 29076};
		
		registerMobs(mob, QuestEventType.ON_ATTACK, QuestEventType.ON_KILL, QuestEventType.ON_SPAWN);
		
		final StatsSet info = GrandBossManager.getStatsSet(ANTHARAS);	
		switch (GrandBossManager.getInstance().getBossStatus(ANTHARAS))
		{
			case DEAD:
				long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
				if (temp > 0)
					startQuestTimer("antharas_unlock", null, null, temp);
				else
					GrandBossManager.setBossStatus(ANTHARAS, DORMANT);
				break;
			
			case WAITING:
				startQuestTimer("beginning", null, null, Config.Antharas_Wait_Time);
				break;
			
			case FIGHTING:
				final int loc_x = info.getInteger("loc_x");
				final int loc_y = info.getInteger("loc_y");
				final int loc_z = info.getInteger("loc_z");
				final int heading = info.getInteger("heading");
				final int hp = info.getInteger("currentHP");
				final int mp = info.getInteger("currentMP");
				
				checkAntharas();	
				final L2Npc antharas = addSpawn(_antharasId, loc_x, loc_y, loc_z, heading, false, 0, false);
				GrandBossManager.addBoss(ANTHARAS, (L2GrandBossInstance) antharas);			
				antharas.setCurrentHpMp(hp, mp);
				antharas.setRunning();		
				_action = System.currentTimeMillis();
				
				startQuestTimer("regen_task", antharas, null, 60000,true);
				startQuestTimer("skill_task", antharas, null, 2000,true);
				startQuestTimer("spawn_minions", antharas, null, _minionTimer,true);
				break;
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("beginning"))
		{
			checkAntharas();
			
			final L2Npc antharas = addSpawn(_antharasId, 181323, 114850, -7623, 32542, false, 0, false);
			GrandBossManager.addBoss(ANTHARAS, (L2GrandBossInstance) antharas);
			antharas.setIsInvul(true);
			
			startQuestTimer("spawn_1", antharas, null, 16);
			startQuestTimer("spawn_2", antharas, null, 3016);
			startQuestTimer("spawn_3", antharas, null, 13016);
			startQuestTimer("spawn_4", antharas, null, 13216);
			startQuestTimer("spawn_5", antharas, null, 24016);
			startQuestTimer("spawn_6", antharas, null, 25916);
		}
		if (event.equalsIgnoreCase("regen_task"))
		{
			if (_action + 1800000 < System.currentTimeMillis())
			{
				GrandBossManager.setBossStatus(ANTHARAS, DORMANT);
				_Zone.oustAllPlayers();
				
				CancelTimers(npc);
				
				npc.deleteMe();
				return null;
			}
			_skillRegen.getEffects(npc, npc);
		}
		else if (event.equalsIgnoreCase("spawn_1"))
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 13, -19, 0, 20000, 0, 0, 1, 0));
		else if (event.equalsIgnoreCase("spawn_2"))
		{
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 13, 0, 6000, 20000, 0, 0, 1, 0));
		}
		else if (event.equalsIgnoreCase("spawn_3"))
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 3700, 0, -3, 0, 10000, 0, 0, 1, 0));
		else if (event.equalsIgnoreCase("spawn_4"))
		{
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 2));
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 0, -3, 22000, 30000, 0, 0, 1, 0));
		}
		else if (event.equalsIgnoreCase("spawn_5"))
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 0, -3, 300, 7000, 0, 0, 1, 0));
		else if (event.equalsIgnoreCase("spawn_6"))
		{
			_action = System.currentTimeMillis();
			
			GrandBossManager.setBossStatus(ANTHARAS, FIGHTING);
			npc.setIsInvul(false);
			npc.setRunning();
			
			startQuestTimer("regen_task", npc, null, 60000,true);
			startQuestTimer("skill_task", npc, null, 2000,true);
			startQuestTimer("spawn_minions", npc, null, _minionTimer,true);
		}
		else if (event.equalsIgnoreCase("skill_task"))
			callSkill(npc);
		else if (event.equalsIgnoreCase("spawn_minions"))
		{
			boolean isBehemoth = Rnd.get(100) < 60;
			int mobNumber = isBehemoth ? 2 : 3;
			
			for (int i = 0; i < mobNumber; i++)
			{
				if (_monsters.size() > 9)
					break;
				
				final int npcId = isBehemoth ? 29069 : Rnd.get(29070, 29076);
				final L2Npc dragon = addSpawn(npcId, npc.getX() + Rnd.get(-200, 200), npc.getY() + Rnd.get(-200, 200), npc.getZ(), 0, false, 0, true);
				((L2MonsterInstance) dragon).setIsRaidMinion(true);
				
				_monsters.add(dragon);
				
				final L2PcInstance victim = getRandomPlayer(dragon);
				if (victim != null)
					attack(((L2Attackable) dragon), victim);
				
				if (!isBehemoth)
					startQuestTimer("self_destruct", dragon, null, (_minionTimer / 3));
			}
		}
		else if (event.equalsIgnoreCase("self_destruct"))
		{
			L2Skill skill;
			switch (npc.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = SkillTable.getInstance().getInfo(5097, 1);
					break;
				default:
					skill = SkillTable.getInstance().getInfo(5094, 1);
			}
			npc.doCast(skill);
		}
		else if (event.equalsIgnoreCase("die_1"))
		{
			addSpawn(31859, 177615, 114941, -7709, 0, false, 900000, false);
			startQuestTimer("remove_players", null, null, 900000);
		}
		else if (event.equalsIgnoreCase("antharas_unlock"))
			GrandBossManager.setBossStatus(ANTHARAS, DORMANT);
		else if (event.equalsIgnoreCase("remove_players"))
			_Zone.oustAllPlayers();
				
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.isInvul())
			return null;
		
		if (attacker.isPlayable())
		{
			if (testCursesOnAttack(npc, attacker))
				return null;
			
			_action = System.currentTimeMillis();
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	private void checkAntharas()
	{
		final int playersNumber = _Zone.getAllowedPlayers().size();
		if (playersNumber < 45)
		{
			_antharasId = ANTHARAS_IDS[0];
			_skillRegen = SkillTable.getInstance().getInfo(4239, 1);
			_minionTimer = 180000;
		}
		else if (playersNumber < 63)
		{
			_antharasId = ANTHARAS_IDS[1];
			_skillRegen = SkillTable.getInstance().getInfo(4240, 1);
			_minionTimer = 150000;
		}
		else
		{
			_antharasId = ANTHARAS_IDS[2];
			_skillRegen = SkillTable.getInstance().getInfo(4241, 1);
			_minionTimer = 120000;
		}
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == _antharasId)
		{
			CancelTimers(npc);
			
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1200, 20, -10, 10000, 13000, 0, 0, 0, 0));
			_Zone.broadcastPacket(Music.BS01_D_10000.getPacket());
			startQuestTimer("die_1", null, null, 8000);
			
			GrandBossManager.setBossStatus(ANTHARAS, DEAD);
			
			long respawnTime = (Config.Interval_Of_Antharas_Spawn + Rnd.get(Config.Random_Of_Antharas_Spawn));
			startQuestTimer("antharas_unlock", null, null, respawnTime);
			
			StatsSet info = GrandBossManager.getStatsSet(ANTHARAS);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.setStatsSet(ANTHARAS, info);
		}
		else
		{
			cancelQuestTimers("self_destruct");
			_monsters.remove(npc);
		}	
	
		return super.onKill(npc, killer, isPet);
	}
	
	private void callSkill(L2Npc npc)
	{
		if (npc.isInvul() || npc.isCastingNow())
			return;
		
		if (_actualVictim == null || _actualVictim.isDead() || !npc.isInSurroundingRegion(_actualVictim) || Rnd.get(10) == 0)
			_actualVictim = getRandomPlayer(npc);
		
		if (_actualVictim == null)
			return;
		
		final L2Skill skill = getRandomSkill(npc);
		
		if (MathUtil.checkIfInRange((skill.getCastRange() < 600) ? 600 : skill.getCastRange(), npc, _actualVictim, true))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			npc.setTarget(_actualVictim);
			npc.doCast(skill);
		}
		else
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _actualVictim, null);
	}
	
	private static L2Skill getRandomSkill(L2Npc npc)
	{
		final double hpRatio = npc.getCurrentHp() / npc.getMaxHp();
		
		final int[] playersAround = getPlayersCountInPositions(1100, npc, false);
		
		if (hpRatio < 0.25)
		{
			if (Rnd.get(100) < 30)
				return FrequentSkill.ANTHARAS_MOUTH.getSkill();
			
			if (playersAround[1] >= 10 && Rnd.get(100) < 80)
				return FrequentSkill.ANTHARAS_TAIL.getSkill();
			
			if (playersAround[0] >= 10)
			{
				if (Rnd.get(100) < 40)
					return FrequentSkill.ANTHARAS_DEBUFF.getSkill();
				
				if (Rnd.get(100) < 10)
					return FrequentSkill.ANTHARAS_JUMP.getSkill();
			}
			
			if (Rnd.get(100) < 10)
				return FrequentSkill.ANTHARAS_METEOR.getSkill();
		}
		else if (hpRatio < 0.5)
		{
			if (playersAround[1] >= 10 && Rnd.get(100) < 80)
				return FrequentSkill.ANTHARAS_TAIL.getSkill();
			
			if (playersAround[0] >= 10)
			{
				if (Rnd.get(100) < 40)
					return FrequentSkill.ANTHARAS_DEBUFF.getSkill();
				
				if (Rnd.get(100) < 10)
					return FrequentSkill.ANTHARAS_JUMP.getSkill();
			}
			
			if (Rnd.get(100) < 7)
				return FrequentSkill.ANTHARAS_METEOR.getSkill();
		}
		else if (hpRatio < 0.75)
		{
			if (playersAround[1] >= 10 && Rnd.get(100) < 80)
				return FrequentSkill.ANTHARAS_TAIL.getSkill();
			
			if (playersAround[0] >= 10 && Rnd.get(100) < 10)
				return FrequentSkill.ANTHARAS_JUMP.getSkill();
			
			if (Rnd.get(100) < 5)
				return FrequentSkill.ANTHARAS_METEOR.getSkill();
		}
		else
		{
			if (playersAround[1] >= 10 && Rnd.get(100) < 80)
				return FrequentSkill.ANTHARAS_TAIL.getSkill();
			
			if (Rnd.get(100) < 3)
				return FrequentSkill.ANTHARAS_METEOR.getSkill();
		}
		
		if (Rnd.get(100) < 6)
			return FrequentSkill.ANTHARAS_BREATH.getSkill();
		
		if (Rnd.get(100) < 50)
			return FrequentSkill.ANTHARAS_NORMAL_ATTACK.getSkill();
		
		if (Rnd.get(100) < 5)
		{
			if (Rnd.get(100) < 50)
				return FrequentSkill.ANTHARAS_FEAR.getSkill();
			
			return FrequentSkill.ANTHARAS_SHORT_FEAR.getSkill();
		}
		
		return FrequentSkill.ANTHARAS_NORMAL_ATTACK_EX.getSkill();
	}
	
	private void CancelTimers(L2Npc npc)
	{
		cancelQuestTimers("regen_task");
		cancelQuestTimers("skill_task");
		cancelQuestTimers("spawn_minions");
		
		cancelQuestTimers("self_destruct");
		
		_monsters.forEach(L2Npc::deleteMe);
		_monsters.clear();
	}
	
	public static void main(String[] args)
	{
		new Antharas();
	}
}
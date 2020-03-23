package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.enums.sound.Music;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.group.party.L2CommandChannel;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.network.serverpackets.AbstractNpcInfo;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.Earthquake;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillCanceld;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.SocialAction;
import com.l2jhellas.gameserver.network.serverpackets.SpecialCamera;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.Rnd;

public class Frintezza extends AbstractNpcAI
{
    public static final L2BossZone _Zone = GrandBossManager.getZoneById(110011);

   	private final int[][] _invadeLoc =
   	{
   		{174102,-76039,-5105},
   		{173235,-76884,-5105},
   		{175003,-76933,-5105},
   		{174196,-76190,-5105},
   		{174013,-76120,-5105},
   		{173263,-75161,-5105}
   	};
   	
	private static final int[][] SKILLS =
	{
	    { 5015,1 },
	    { 5015,4 },
	    { 5015,2 },
	    { 5015,5 },
	    { 5018,1 },
	    { 5016,1 },
	    { 5015,3 },
	    { 5015,6 },
	    { 5018,2 },
	    { 5019,1 },
	    { 5016,1 }
	 };
	    
	    private static final int[][] _mobLoc =
	    {
	        { 18328,172894,-76019,-5107,243 },
	        { 18328,174095,-77279,-5107,16216 },
	        { 18328,174111,-74833,-5107,49043 },
	        { 18328,175344,-76042,-5107,32847 },
	        { 18330,173489,-76227,-5134,63565 },
	        { 18330,173498,-75724,-5107,58498 },
	        { 18330,174365,-76745,-5107,22424 },
	        { 18330,174570,-75584,-5107,31968 },
	        { 18330,174613,-76179,-5107,31471 },
	        { 18332,173620,-75981,-5107,4588 },
	        { 18332,173630,-76340,-5107,62454 },
	        { 18332,173755,-75613,-5107,57892 },
	        { 18332,173823,-76688,-5107,2411 },
	        { 18332,174000,-75411,-5107,54718 },
	        { 18332,174487,-75555,-5107,33861 },
	        { 18332,174517,-76471,-5107,21893 },
	        { 18332,174576,-76122,-5107,31176 },
	        { 18332,174600,-75841,-5134,35927 },
	        { 18329,173481,-76043,-5107,61312 },
	        { 18329,173539,-75678,-5107,59524 },
	        { 18329,173584,-76386,-5107,3041 },
	        { 18329,173773,-75420,-5107,51115 },
	        { 18329,173777,-76650,-5107,12588 },
	        { 18329,174585,-76510,-5107,21704 },
	        { 18329,174623,-75571,-5107,40141 },
	        { 18329,174744,-76240,-5107,29202 },
	        { 18329,174769,-75895,-5107,29572 },
	        { 18333,173861,-76011,-5107,383 },
	        { 18333,173872,-76461,-5107,8041 },
	        { 18333,173898,-75668,-5107,51856 },
	        { 18333,174422,-75689,-5107,42878 },
	        { 18333,174460,-76355,-5107,27311 },
	        { 18333,174483,-76041,-5107,30947 },
	        { 18331,173515,-76184,-5107,6971 },
	        { 18331,173516,-75790,-5134,3142 },
	        { 18331,173696,-76675,-5107,6757 },
	        { 18331,173766,-75502,-5134,60827 },
	        { 18331,174473,-75321,-5107,37147 },
	        { 18331,174493,-76505,-5107,34503 },
	        { 18331,174568,-75654,-5134,41661 },
	        { 18331,174584,-76263,-5107,31729 },
	        { 18339,173892,-81592,-5123,50849 },
	        { 18339,173958,-81820,-5123,7459 },
	        { 18339,174128,-81805,-5150,21495 },
	        { 18339,174245,-81566,-5123,41760 },
	        { 18334,173264,-81529,-5072,1646 },
	        { 18334,173265,-81656,-5072,441 },
	        { 18334,173267,-81889,-5072,0 },
	        { 18334,173271,-82015,-5072,65382 },
	        { 18334,174867,-81655,-5073,32537 },
	        { 18334,174868,-81890,-5073,32768 },
	        { 18334,174869,-81485,-5073,32315 },
	        { 18334,174871,-82017,-5073,33007 },
	        { 18335,173074,-80817,-5107,8353 },
	        { 18335,173128,-82702,-5107,5345 },
	        { 18335,173181,-82544,-5107,65135 },
	        { 18335,173191,-80981,-5107,6947 },
	        { 18335,174859,-80889,-5134,24103 },
	        { 18335,174924,-82666,-5107,38710 },
	        { 18335,174947,-80733,-5107,22449 },
	        { 18335,175096,-82724,-5107,42205 },
	        { 18336,173435,-80512,-5107,65215 },
	        { 18336,173440,-82948,-5107,417 },
	        { 18336,173443,-83120,-5107,1094 },
	        { 18336,173463,-83064,-5107,286 },
	        { 18336,173465,-80453,-5107,174 },
	        { 18336,173465,-83006,-5107,2604 },
	        { 18336,173468,-82889,-5107,316 },
	        { 18336,173469,-80570,-5107,65353 },
	        { 18336,173469,-80628,-5107,166 },
	        { 18336,173492,-83121,-5107,394 },
	        { 18336,173493,-80683,-5107,0 },
	        { 18336,173497,-80510,-5134,417 },
	        { 18336,173499,-82947,-5107,0 },
	        { 18336,173521,-83063,-5107,316 },
	        { 18336,173523,-82889,-5107,128 },
	        { 18336,173524,-80627,-5134,65027 },
	        { 18336,173524,-83007,-5107,0 },
	        { 18336,173526,-80452,-5107,64735 },
	        { 18336,173527,-80569,-5134,65062 },
	        { 18336,174602,-83122,-5107,33104 },
	        { 18336,174604,-82949,-5107,33184 },
	        { 18336,174609,-80514,-5107,33234 },
	        { 18336,174609,-80684,-5107,32851 },
	        { 18336,174629,-80627,-5107,33346 },
	        { 18336,174632,-80570,-5107,32896 },
	        { 18336,174632,-83066,-5107,32768 },
	        { 18336,174635,-82893,-5107,33594 },
	        { 18336,174636,-80456,-5107,32065 },
	        { 18336,174639,-83008,-5107,33057 },
	        { 18336,174660,-80512,-5107,33057 },
	        { 18336,174661,-83121,-5107,32768 },
	        { 18336,174663,-82948,-5107,32768 },
	        { 18336,174664,-80685,-5107,32676 },
	        { 18336,174687,-83008,-5107,32520 },
	        { 18336,174691,-83066,-5107,32961 },
	        { 18336,174692,-80455,-5107,33202 },
	        { 18336,174692,-80571,-5107,32768 },
	        { 18336,174693,-80630,-5107,32994 },
	        { 18336,174693,-82889,-5107,32622 },
	        { 18337,172837,-82382,-5107,58363 },
	        { 18337,172867,-81123,-5107,64055 },
	        { 18337,172883,-82495,-5107,64764 },
	        { 18337,172916,-81033,-5107,7099 },
	        { 18337,172940,-82325,-5107,58998 },
	        { 18337,172946,-82435,-5107,58038 },
	        { 18337,172971,-81198,-5107,14768 },
	        { 18337,172992,-81091,-5107,9438 },
	        { 18337,173032,-82365,-5107,59041 },
	        { 18337,173064,-81125,-5107,5827 },
	        { 18337,175014,-81173,-5107,26398 },
	        { 18337,175061,-82374,-5107,43290 },
	        { 18337,175096,-81080,-5107,24719 },
	        { 18337,175169,-82453,-5107,37672 },
	        { 18337,175172,-80972,-5107,32315 },
	        { 18337,175174,-82328,-5107,41760 },
	        { 18337,175197,-81157,-5107,27617 },
	        { 18337,175245,-82547,-5107,40275 },
	        { 18337,175249,-81075,-5107,28435 },
	        { 18337,175292,-82432,-5107,42225 },
	        { 18338,173014,-82628,-5107,11874 },
	        { 18338,173033,-80920,-5107,10425 },
	        { 18338,173095,-82520,-5107,49152 },
	        { 18338,173115,-80986,-5107,9611 },
	        { 18338,173144,-80894,-5107,5345 },
	        { 18338,173147,-82602,-5107,51316 },
	        { 18338,174912,-80825,-5107,24270 },
	        { 18338,174935,-80899,-5107,18061 },
	        { 18338,175016,-82697,-5107,39533 },
	        { 18338,175041,-80834,-5107,25420 },
	        { 18338,175071,-82549,-5107,39163 },
	        { 18338,175154,-82619,-5107,36345 }
	    };
	    
	    private static final int[][] PORTRAITS = 
	    {
	        { 29049,175876,-88713 },
	        { 29049,172608,-88702 },
	        { 29048,175833,-87165 },
	        { 29048,172634,-87165 }
	    };
	    
	    private static final int[][] DEMONS = 
	    {
	        { 29051,175876,-88713,-4972,28205 },
	        { 29051,172608,-88702,-4972,64817 },
	        { 29050,175833,-87165,-4972,35048 },
	        { 29050,172634,-87165,-4972,57730 }
	    };
	    
	    public static final int FRINTEZZA = 29045;
	    public static final int SCARLET1 = 29046;
	    public static final int SCARLET2 = 29047;
	    public static final int CUBE = 29061;
	    public static int GUIDE = 32011;
	    
	    public static final byte DORMANT = 0;
	    public static final byte WAITING = 1;
	    public static final byte FIGHTING = 2;
	    public static final byte DEAD = 3;
	    
	    private Set<L2Npc> _roomMobs = ConcurrentHashMap.newKeySet();
	    public L2GrandBossInstance _frintezza;
	    private int _transformed;
	    private int _check;
	    private int _currentSong;
	    private long _lastAction;
	    private int _LocCycle = 0;
	    
	    private final List<L2PcInstance> _PlayersInside = new ArrayList<>();

	    private static final int[] NPCS =
	    {
	        SCARLET1, SCARLET2, FRINTEZZA, 18328, 18329, 18330, 18331, 18332, 18333,
	        18334, 18335, 18336, 18337, 18338, 18339, 29048, 29049, 29050, 29051, 32011
	    };
	    
	public Frintezza()
	{
		super("Frintezza", "ai");
		
		registerMobs(NPCS,QuestEventType.ON_ATTACK,QuestEventType.ON_KILL);
		addStartNpc(GUIDE, CUBE);
		addTalkId(GUIDE, CUBE);
		
		StatsSet info = GrandBossManager.getStatsSet(FRINTEZZA);
		int status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA);
		
		if (status == DEAD)
		{
			long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
				startQuestTimer("frintezza_unlock", temp, null, null, false);
			else
				GrandBossManager.setBossStatus(FRINTEZZA, DORMANT);
		}
		else if (status != DORMANT)
			GrandBossManager.setBossStatus(FRINTEZZA, DORMANT);
		
		// tempfix for messed door cords
		for (int i = 0; i < 8; i++)
			DoorData.getInstance().getDoor(25150051 + i).setRange(0, 0, 0, 0, 0, 0);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("beginning"))
		{
			closeDoors();
			_check = 35;
			for (int i = 0; i <= 17; i++)
				_roomMobs.add(addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0, false));
			
			_Zone.broadcastPacket(new CreatureSay(0, ChatType.SHOUT, "Hall Alarm Device", "Intruders! Sound the alarm!"));
			
			startQuestTimer("check", null, null, 60000);
			startQuestTimer("frintezza_despawn", null, null, 60000);
		}
		else if (event.equalsIgnoreCase("check"))
		{
			if (_check == 0)
			{
				_Zone.broadcastPacket(new CreatureSay(0, ChatType.SHOUT, "Frintezza Gatekeeper", "Time limit exceeded, challenge failed!"));
				_Zone.oustAllPlayers();
						
				cancelQuestTimers("check");
				cancelQuestTimers("frintezza_despawn");
				
				deleteAllMobs();
				closeDoors();
				
				_PlayersInside.clear();
				
				GrandBossManager.setBossStatus(FRINTEZZA, DORMANT);
			}
			
			_check--;
			_Zone.broadcastPacket(new ExShowScreenMessage(_check + " minute(s) remaining.", 10000));
		}
		else if (event.equalsIgnoreCase("waiting"))
		{
			startQuestTimer("close", null, null, 27000);
			startQuestTimer("camera_1", null, null, 30000);
			_Zone.broadcastPacket(new Earthquake(174232, -88020, -5116, 45, 27));
		}
		else if (event.equalsIgnoreCase("frintezza_unlock"))
			GrandBossManager.setBossStatus(FRINTEZZA, DORMANT);
		else if (event.equalsIgnoreCase("remove_players"))
		{
			_Zone.oustAllPlayers();
			_PlayersInside.clear();
		}
		else if (event.equalsIgnoreCase("close"))
			closeDoors();
		else if (event.equalsIgnoreCase("spawn_minion"))
		{
			if (npc != null && !npc.isDead())
			{
				L2Npc mob = addSpawn(npc.getNpcId() + 2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false);
				
				startQuestTimer("action", mob, null, 200);
				startQuestTimer("spawn_minion", npc, null, 18000);
			}
		}
		else if (event.equalsIgnoreCase("action"))
			_Zone.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
		else if (event.equalsIgnoreCase("camera_1"))
		{
			GrandBossManager.setBossStatus(FRINTEZZA, FIGHTING);
			
			L2Npc dummy = addSpawn(29052, 174232, -88020, -5116, 49151, false, 0, false);
			dummy.setIsInvul(true);
			dummy.setIsImmobilized(true);
			dummy.setCollisionHeight(600);
			_Zone.broadcastPacket(new AbstractNpcInfo.NpcInfo(dummy, null));
			
			stopPcActions();
			startQuestTimer("camera_2", dummy, null, 1000);
		}
		else if (event.equalsIgnoreCase("camera_2"))
		{
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 0, 75, -89, 0, 100));
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 0, 75, -89, 0, 100));
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 300, 90, -10, 6500, 7000));
			
			_frintezza = (L2GrandBossInstance) addSpawn(FRINTEZZA, 174240, -89805, -5022, 16048, false, 0, false);
			_frintezza.setIsImmobilized(true);
			_frintezza.setIsInvul(true);
			_frintezza.disableAllSkills();
			
			GrandBossManager.addBoss(_frintezza);
			
			_frintezza.broadcastInfo();
			
			for (int[] demon : DEMONS)
			{
				L2Npc d = addSpawn(demon[0], demon[1], demon[2], demon[3], demon[4], false, 0, false);
				d.setIsImmobilized(true);
				d.disableAllSkills();
			}
			
			startQuestTimer("camera_4", npc, null, 6500);
		}
		else if (event.equalsIgnoreCase("camera_4"))
		{
			npc.deleteMe();
			
			L2Npc dummy = addSpawn(29052, 174240, -89805, -5022, 16048, false, 0, false);
			dummy.setIsInvul(true);
			dummy.setIsImmobilized(true);
			
			_Zone.broadcastPacket(new SpecialCamera(dummy.getObjectId(), 1800, 90, 8, 6500, 7000));
			startQuestTimer("camera_5", dummy, null, 900);
		}
		else if (event.equalsIgnoreCase("camera_5"))
		{
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 140, 90, 10, 2500, 4500));
			startQuestTimer("camera_5b", npc, null, 4000);
		}
		else if (event.equalsIgnoreCase("camera_5b"))
		{
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 40, 75, -10, 0, 1000));
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 40, 75, -10, 0, 12000));
			startQuestTimer("camera_7", npc, null, 1350);
		}
		else if (event.equalsIgnoreCase("camera_7"))
		{
			_Zone.broadcastPacket(new SocialAction(_frintezza.getObjectId(), 2));
			startQuestTimer("camera_8", npc, null, 7000);
		}
		else if (event.equalsIgnoreCase("camera_8"))
		{
			npc.deleteMe();
			startQuestTimer("camera_9", null, null, 1000);
		}
		else if (event.equalsIgnoreCase("camera_9"))
		{
			for (L2Npc mob : _Zone.getKnownTypeInside(L2Npc.class))
			{
				if (mob.getNpcId() == 29051 || mob.getNpcId() == 29050)
					_Zone.broadcastPacket(new SocialAction(mob.getObjectId(), 1));
			}
			
			startQuestTimer("camera_9b", null, null, 400);
		}
		else if (event.equalsIgnoreCase("camera_9b"))
		{
			for (L2Npc mob : _Zone.getKnownTypeInside(L2Npc.class))
			{
				if (mob.getNpcId() == 29051 || mob.getNpcId() == 29050)
					_Zone.broadcastPacket(new SocialAction(mob.getObjectId(), 1));
			}
			
			L2Npc dummy1 = addSpawn(29052, 172450, -87890, -5089, 16048, false, 0, false);
			L2Npc dummy2 = addSpawn(29052, 176012, -87890, -5089, 16048, false, 0, false);
			dummy1.setIsImmobilized(true);
			dummy1.setIsInvul(true);
			dummy2.setIsImmobilized(true);
			dummy2.setIsInvul(true);
			
			for (L2Character pc : _Zone.getKnownTypeInside(L2PcInstance.class))
			{
				if (pc.getX() < 174232)
					pc.broadcastPacket(new SpecialCamera(dummy1.getObjectId(), 1000, 118, 0, 0, 1000));
				else
					pc.broadcastPacket(new SpecialCamera(dummy2.getObjectId(), 1000, 62, 0, 0, 1000));
			}
			for (L2Character pc : _Zone.getKnownTypeInside(L2PcInstance.class))
			{
				if (pc.getX() < 174232)
					pc.broadcastPacket(new SpecialCamera(dummy1.getObjectId(), 1000, 118, 0, 0, 10000));
				else
					pc.broadcastPacket(new SpecialCamera(dummy2.getObjectId(), 1000, 62, 0, 0, 10000));
			}
			
			dummy1.deleteMe();
			dummy2.deleteMe();
			
			startQuestTimer("camera_10", null, null, 2000);
		}
		else if (event.equalsIgnoreCase("camera_10"))
		{
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 240, 90, 0, 0, 1000));
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 240, 90, 25, 5500, 10000));
			_Zone.broadcastPacket(new SocialAction(_frintezza.getObjectId(), 3));
			startQuestTimer("camera_12", null, null, 4500);
		}
		else if (event.equalsIgnoreCase("camera_12"))
		{
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 100, 195, 35, 0, 10000));
			startQuestTimer("camera_13", null, null, 700);
		}
		else if (event.equalsIgnoreCase("camera_13"))
		{
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 100, 195, 35, 0, 10000));
			startQuestTimer("camera_14", null, null, 1300);
		}
		else if (event.equalsIgnoreCase("camera_14"))
		{
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 120, 180, 45, 1500, 10000));
			startQuestTimer("camera_16", null, null, 1500);
		}
		else if (event.equalsIgnoreCase("camera_16"))
		{
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 520, 135, 45, 8000, 10000));
			startQuestTimer("camera_17", null, null, 7500);
		}
		else if (event.equalsIgnoreCase("camera_17"))
		{
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 1500, 110, 25, 10000, 13000));
			startQuestTimer("camera_18", null, null, 9500);
		}
		else if (event.equalsIgnoreCase("camera_18"))
		{
			L2Npc dummy = addSpawn(29052, 174232, -88020, -5111, 49151, false, 0, false);
			L2Npc scarletDummy = addSpawn(29052, 174232, -88020, -5111, 49151, false, 0, false);
			dummy.setIsInvul(true);
			dummy.setIsImmobilized(true);
			dummy.setCollisionHeight(600);
			scarletDummy.setIsInvul(true);
			scarletDummy.setIsImmobilized(true);
			
			npc = addSpawn(SCARLET1, 174232, -88020, -5111, 20458, false, 0, false);
			
			_Zone.broadcastPacket(new AbstractNpcInfo.NpcInfo(dummy, null));
			_Zone.broadcastPacket(new SpecialCamera(SCARLET1, 930, 160, -20, 0, 1000));
			_Zone.broadcastPacket(new SpecialCamera(SCARLET1, 930, 160, -20, 0, 10000));
			
			stopNpcActions();
			
			_Zone.broadcastPacket(new MagicSkillUse(scarletDummy, dummy, 5004, 1, 5800, 0));
			
			scarletDummy.deleteMe();
			dummy.deleteMe();
			
			startQuestTimer("camera_19", npc, null, 5500);
			startQuestTimer("camera_19b", npc, null, 5400);
		}
		else if (event.equalsIgnoreCase("camera_19"))
		{
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 800, 160, 5, 1000, 10000));
			startQuestTimer("camera_20", npc, null, 2100);
		}
		else if (event.equalsIgnoreCase("camera_19b"))
			_Zone.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
		else if (event.equalsIgnoreCase("camera_20"))
		{
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 300, 60, 8, 0, 10000));
			startQuestTimer("camera_21", npc, null, 2000);
		}
		else if (event.equalsIgnoreCase("camera_21"))
		{
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 500, 90, 10, 3000, 5000));
			startQuestTimer("camera_22", npc, null, 3000);
		}
		else if (event.equalsIgnoreCase("camera_22"))
		{
			for (int[] portrait : PORTRAITS)
			{
				L2Npc p = addSpawn(portrait[0], portrait[1], portrait[2], -5000, 0, false, 0, false);
				p.setIsImmobilized(true);
				p.disableAllSkills();
			}
			
			startQuestTimer("camera_23", npc, null, 2000);
			startQuestTimer("songs_play", npc, null, 10000 + Rnd.get(10000));
			startQuestTimer("skill_ai", npc, null, 10000 + Rnd.get(10000));
		}
		else if (event.equalsIgnoreCase("camera_23"))
		{
			for (L2Npc minion : _Zone.getKnownTypeInside(L2Npc.class))
			{
				if (minion.getNpcId() == FRINTEZZA)
					continue;
				
				minion.setIsImmobilized(false);
				minion.enableAllSkills();
				
				if (minion.getNpcId() == 29049 || minion.getNpcId() == 29048)
					startQuestTimer("spawn_minion", minion, null, 20000);
			}
			
			startPcActions();
			startNpcActions();
		}
		else if (event.equalsIgnoreCase("start_pc"))
			startPcActions();
		else if (event.equalsIgnoreCase("songs_play"))
		{
			if (npc.isCastingNow() || _currentSong != 0)
				startQuestTimer("songs_play", npc, null, 5000 + Rnd.get(5000));
			else if (_frintezza != null && !_frintezza.isDead())
			{
				_currentSong = Rnd.get(1, 5);
				String songName = "";
				
				switch (_currentSong)
				{
					case 1:
						songName = "Requiem of Hatred";
						break;
					case 2:
						songName = "Fugue of Jubilation";
						break;
					case 3:
						songName = "Frenetic Toccata";
						break;
					case 4:
						songName = "Hypnotic Mazurka";
						break;
					case 5:
						songName = "Mournful Chorale Prelude";
						break;
					default:
						songName = "Rondo of Solitude";
						break;
				}
				
				_Zone.broadcastPacket(new ExShowScreenMessage(songName, 4000));
				
				if (_currentSong == 1 && _transformed == 2 && npc.getCurrentHp() < npc.getMaxHp() * 0.6 && Rnd.get(100) < 80)
				{
					_Zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5007, 1, 32000, 0));
					startQuestTimer("songs_effect", null, null, 4000);
					startQuestTimer("songs_play", npc, null, 32000 + Rnd.get(10000));
				}
				else if (_currentSong == 2 || _currentSong == 3)
				{
					_Zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5007, _currentSong, 32000, 0));
					startQuestTimer("songs_effect", null, null, 5000);
					startQuestTimer("songs_play", npc, null, 32000 + Rnd.get(10000));
				}
				else if (_currentSong == 4 && _currentSong == 1)
				{
					_Zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5007, 4, 31000, 0));
					startQuestTimer("songs_effect", null, null, 5000);
					startQuestTimer("songs_play", npc, null, 31000 + Rnd.get(10000));
				}
				else if (_currentSong == 5 && _transformed == 2)
				{
					_Zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5007, 5, 35000, 0));
					startQuestTimer("songs_effect", null, null, 5000);
					startQuestTimer("songs_play", npc, null, 35000 + Rnd.get(10000));
				}
				else
				{
					_Zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5006, 1, 34000, 0));
					startQuestTimer("songs_play", npc, null, 5000 + Rnd.get(5000));
				}
			}
		}
		else if (event.equalsIgnoreCase("songs_effect"))
		{
			if (_currentSong > 0)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(5008, _currentSong);
				
				if (_currentSong < 5)
				{
					for (L2Character cha : _Zone.getKnownTypeInside(L2PcInstance.class))
					{
						if (Rnd.get(100) < 80)
							skill.getEffects(_frintezza, cha);
					}
				}
				else if (_currentSong == 5)
				{
					for (L2Character cha : _Zone.getKnownTypeInside(L2PcInstance.class))
					{
						if (Rnd.get(100) < 70)
						{
							cha.abortAttack();
							cha.abortCast();
							cha.disableAllSkills();
							cha.stopMove(null);
							cha.setIsImmobilized(true);
							cha.setIsParalyzed(true);
							cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							skill.getEffects(_frintezza, cha);
							cha.startAbnormalEffect(AbnormalEffect.DANCE_STUNNED);
						}
					}
					startQuestTimer("stop_effect", null, null, 25000);
				}
			}
		}
		else if (event.equalsIgnoreCase("stop_effect"))
		{
			_currentSong = 0;
			
			for (L2Character cha : _Zone.getKnownTypeInside(L2PcInstance.class))
			{
				cha.stopAbnormalEffect(AbnormalEffect.DANCE_STUNNED);
				cha.stopAbnormalEffect(AbnormalEffect.FLOATING_ROOT);
				cha.enableAllSkills();
				cha.setIsImmobilized(false);
				cha.setIsParalyzed(false);
			}
		}
		else if (event.equalsIgnoreCase("start_npc"))
			startNpcActions();
		else if (event.equalsIgnoreCase("skill_ai"))
		{
			if (!npc.isDead())
			{
				if (_transformed == 0)
				{
					int i = Rnd.get(0, 1);
					final L2Skill skill = SkillTable.getInstance().getInfo(SKILLS[i][0], SKILLS[i][1]);
					if (skill != null)
					{
						npc.stopMove(null);
						npc.doCast(skill);
					}
				}
				else if (_transformed == 1)
				{
					int i = Rnd.get(2, 5);
					final L2Skill skill = SkillTable.getInstance().getInfo(SKILLS[i][0], SKILLS[i][1]);
					if (skill != null)
					{
						npc.stopMove(null);
						npc.doCast(skill);
					}
					if (i == 5)
						startQuestTimer("float_effect", npc, null, 4000);
				}
				else
				{
					int i = Rnd.get(6, 10);
					final L2Skill skill = SkillTable.getInstance().getInfo(SKILLS[i][0], SKILLS[i][1]);
					if (skill != null)
					{
						npc.stopMove(null);
						npc.doCast(skill);
					}
					if (i == 10)
						startQuestTimer("float_effect", npc, null, 3000);
				}
				startQuestTimer("skill_ai", npc, null, 15000 + Rnd.get(10000));
			}
		}
		else if (event.equalsIgnoreCase("float_effect"))
		{
			if (npc.isCastingNow())
				startQuestTimer("float_effect", npc, null, 500);
			else
			{
				for (L2Character cha : _Zone.getKnownTypeInside(L2PcInstance.class))
				{
					if (cha.getFirstEffect(5016) != null)
					{
						cha.abortAttack();
						cha.abortCast();
						cha.disableAllSkills();
						cha.stopMove(null);
						cha.setIsImmobilized(true);
						cha.setIsParalyzed(true);
						cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						cha.startAbnormalEffect(AbnormalEffect.FLOATING_ROOT);
					}
				}
				startQuestTimer("stop_effect", null, null, 25000);
			}
		}
		else if (event.equalsIgnoreCase("frintezza_despawn"))
		{
			if (System.currentTimeMillis() - _lastAction > 900000)
			{
				_Zone.oustAllPlayers();
				
				cancelQuestTimers("spawn_minion");
				cancelQuestTimers("check");
				cancelQuestTimers("frintezza_despawn");
				cancelQuestTimers("waiting");
				
				deleteAllMobs();
				closeDoors();
				stopAttacks();
				
				_PlayersInside.clear();
				
				GrandBossManager.setBossStatus(FRINTEZZA, DORMANT);
			}
		}
		else if (event.equalsIgnoreCase("morph_01"))
		{
			int angle;
			if (npc.getHeading() < 32768)
				angle = Math.abs(180 - (int) (npc.getHeading() / 182.044444444));
			else
				angle = Math.abs(540 - (int) (npc.getHeading() / 182.044444444));
			
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 250, angle, 12, 2000, 15000));
			startQuestTimer("morph_02", npc, null, 3000);
		}
		else if (event.equalsIgnoreCase("morph_02"))
		{
			_Zone.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
			startQuestTimer("morph_04", npc, null, 5500);
		}
		else if (event.equalsIgnoreCase("morph_04"))
		{
			_Zone.broadcastPacket(new SocialAction(npc.getObjectId(), 4));
			startQuestTimer("start_pc", null, null, 3000);
			startQuestTimer("start_npc", null, null, 3000);
		}
		else if (event.equalsIgnoreCase("morph_05a"))
		{
			_Zone.broadcastPacket(new SocialAction(_frintezza.getObjectId(), 4));
			startQuestTimer("morph_05", npc, null, 100);
		}
		else if (event.equalsIgnoreCase("morph_05"))
		{
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 250, 120, 15, 0, 1000));
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 250, 120, 15, 0, 10000));
			startQuestTimer("morph_07", npc, null, 7000);
		}
		else if (event.equalsIgnoreCase("morph_07"))
		{
			_Zone.broadcastPacket(new MagicSkillUse(_frintezza, _frintezza, 5006, 1, 34000, 0));
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 500, 70, 15, 3000, 10000));
			startQuestTimer("morph_08", npc, null, 3000);
		}
		else if (event.equalsIgnoreCase("morph_08"))
		{
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 2500, 90, 12, 6000, 10000));
			startQuestTimer("morph_09", npc, null, 3000);
		}
		else if (event.equalsIgnoreCase("morph_09"))
		{
			int angle;
			if (npc.getHeading() < 32768)
				angle = Math.abs(180 - (int) (npc.getHeading() / 182.044444444));
			else
				angle = Math.abs(540 - (int) (npc.getHeading() / 182.044444444));
			
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 250, angle, 12, 0, 1000));
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 250, angle, 12, 0, 10000));
			startQuestTimer("morph_11", npc, null, 500);
		}
		else if (event.equalsIgnoreCase("morph_11"))
		{
			int angle;
			if (npc.getHeading() < 32768)
				angle = Math.abs(180 - (int) (npc.getHeading() / 182.044444444));
			else
				angle = Math.abs(540 - (int) (npc.getHeading() / 182.044444444));
			
			npc.doDie(npc);
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 450, angle, 14, 8000, 8000));
			startQuestTimer("morph_12", npc, null, 6250);
		}
		else if (event.equalsIgnoreCase("morph_12"))
		{
			npc.deleteMe();
			startQuestTimer("morph_13", npc, null, 950);
		}
		else if (event.equalsIgnoreCase("morph_13"))
		{
			int angle;
			if (npc.getHeading() < 32768)
				angle = Math.abs(180 - (int) (npc.getHeading() / 182.044444444));
			else
				angle = Math.abs(540 - (int) (npc.getHeading() / 182.044444444));
			
			npc = addSpawn(SCARLET2, npc.getX(), npc.getY(), npc.getZ(), 28193, false, 0, false);
			stopNpcActions();
			
			npc.broadcastInfo();
			
			_Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 450, angle, 12, 500, 14000));
			startQuestTimer("morph_15", npc, null, 8100);
		}
		else if (event.equalsIgnoreCase("morph_15"))
		{
			_Zone.broadcastPacket(new SocialAction(npc.getObjectId(), 2));

			startQuestTimer("start_pc", null, null, 6000);
			startQuestTimer("start_npc", null, null, 6000);
			startQuestTimer("songs_play", npc, null, 10000 + Rnd.get(10000));
			startQuestTimer("skill_ai", npc, null, 10000 + Rnd.get(10000));
		}
		else if (event.equalsIgnoreCase("morph_17b"))
		{
			_frintezza.doDie(_frintezza);
			startQuestTimer("morph_18", npc, null, 100);
		}
		else if (event.equalsIgnoreCase("morph_18"))
		{
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 100, 120, 5, 0, 7000));
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 100, 90, 5, 5000, 15000));
			startQuestTimer("morph_20", npc, null, 7000);
		}
		else if (event.equalsIgnoreCase("morph_20"))
		{
			_Zone.broadcastPacket(new SpecialCamera(_frintezza.getObjectId(), 900, 90, 25, 7000, 10000));
			
			closeDoors();
			deleteAllMobs();
			
			addSpawn(CUBE, 174232, -88020, -5114, 49151, false, 900000, false);
			startQuestTimer("start_pc", null, null, 7000);
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == CUBE)
		{
			int x = 150037 + Rnd.get(500);
			int y = -57720 + Rnd.get(500);
			player.teleToLocation(x, y, -2976, false);
			
			if(!_PlayersInside.isEmpty())
	    	    _PlayersInside.removeIf(p -> p.getObjectId() == player.getObjectId());
			
			return null;
		}
			
		String htmltext = "";
		if (GrandBossManager.getInstance().getBossStatus(FRINTEZZA) == DEAD)
			htmltext = "<html><body>There is nothing beyond the Magic Force Field. Come back later.<br>(You may not enter because Frintezza is not inside the Imperial Tomb.)</body></html>";
		else if (GrandBossManager.getInstance().getBossStatus(FRINTEZZA) == DORMANT)
		{
			if ((!player.isInParty() || !player.getParty().isLeader(player)) || (player.getParty().getCommandChannel() == null) || (player.getParty().getCommandChannel().getChannelLeader() != player))
				htmltext = "<html><body>No reaction. Contact must be initiated by the Command Channel Leader.</body></html>";
			else if (player.getParty().getCommandChannel().getPartys().size() < 2 || player.getParty().getCommandChannel().getPartys().size() > 5)
				htmltext = "<html><body>Your command channel needs to have at least 2 parties and a maximum of 5.</body></html>";
			else if (player.getInventory().getItemByItemId(8073) == null)
				htmltext = "<html><body>You dont have required item.</body></html>";
			else
			{
				player.destroyItemByItemId("Quest", 8073, 1, player, true);
				L2CommandChannel CC = player.getParty().getCommandChannel();
								
				for (L2Party party : CC.getPartys())
				{
					if (party == null)
						continue;
					for (L2PcInstance member : party.getPartyMembers())
					{
						if (member == null || member.getLevel() < 74)
							continue;
						if (!member.isInsideRadius(npc, 700, false, false))
							continue;
						if (_PlayersInside.size() > 45)
						{
							member.sendMessage("The number of challenges have been full, so can not enter.");
							break;
						}
						_PlayersInside.add(member);
						_Zone.allowPlayerEntry(member, 300);
						member.teleToLocation(_invadeLoc[_LocCycle][0] + Rnd.get(50), _invadeLoc[_LocCycle][1] + Rnd.get(50), _invadeLoc[_LocCycle][2], false);
					}
					if (_PlayersInside.size() > 45)
						break;
					
					_LocCycle++;
					if (_LocCycle >= 6)
						_LocCycle = 1;
				}
				
				_lastAction = System.currentTimeMillis();

				GrandBossManager.setBossStatus(Frintezza.FRINTEZZA, Frintezza.FIGHTING);
				startQuestTimer("close", null, null, 100);
				startQuestTimer("beginning", null, null, 5000);
				startQuestTimer("check", null, null, 2100000);
				startQuestTimer("frintezza_despawn", null, null, 60000);
				
			}
		}
		else
			htmltext = "<html><body>Someone else is already inside the Magic Force Field. Try again later.</body></html>";
		
		return htmltext;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
        _lastAction = System.currentTimeMillis();
        
		if (npc.getNpcId() == FRINTEZZA)
		{
			npc.setCurrentHpMp(npc.getMaxHp(), 0);
			return null;
		}
		
        switch (npc.getNpcId())
        {
        	case SCARLET1:
                if (_transformed == 0 && npc.getCurrentHp() < npc.getMaxHp() * 0.75)
                {
                    _transformed = 1;
                    stopAttacks();
                    stopPcActions();
                    stopNpcActions();
                    startQuestTimer("morph_01", npc, null, 1100);
                }
                else if (_transformed == 1 && npc.getCurrentHp() < npc.getMaxHp() * 0.5)
                {
                    _transformed = 2; 
                    stopAttacks();
                    stopPcActions();
                    stopNpcActions();
                    startQuestTimer("morph_05a", npc, null, 2000);
                }
                break;
        	case 29050:
        	case 29051:
                if (npc.getCurrentHp() < npc.getMaxHp() * 0.1 && Rnd.get(100) < 30)
                	npc.doCast(SkillTable.getInstance().getInfo(5011, 1));
                break;                
        } 
        
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		switch(npc.getNpcId())
		{
			case SCARLET2:
				_Zone.broadcastPacket(Music.BS01_D_10000.getPacket());
				
	            stopPcActions();
	            stopNpcActions();
	            
	            int angle;
	            if (npc.getHeading() < 32768)
	                angle = Math.abs(180 - (int) (npc.getHeading() / 182.044444444));
	            else
	                angle = Math.abs(540 - (int) (npc.getHeading() / 182.044444444));
	            
	           _Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 300, angle - 180, 5, 0, 7000));
	           _Zone.broadcastPacket(new SpecialCamera(npc.getObjectId(), 200, angle, 85, 4000, 10000));
	            startQuestTimer("morph_17b", npc, null, 7400);
	            
	            GrandBossManager.setBossStatus(FRINTEZZA, DEAD);
	            long respawnTime = (long) 8 + Rnd.get(-8, 8);
	            respawnTime *= 3600000;
	            
	            cancelQuestTimers("spawn_minion");
	            cancelQuestTimers("frintezza_despawn");
	            startQuestTimer("remove_players", null, null, 900000);
	            startQuestTimer("frintezza_unlock", null, null, respawnTime);
	            
	            StatsSet info = GrandBossManager.getStatsSet(FRINTEZZA);
	            info.set("respawn_time", System.currentTimeMillis() + respawnTime);
	            GrandBossManager.setStatsSet(FRINTEZZA, info);
	            break;
			case 18328:
	            int alarmsRemaining = getRemainingMonsters(npc);
	            if (alarmsRemaining == 1)
	            {
	                for (int i = 25150051; i <= 25150058; i++)
	                    openDoor(i);
	            }
	            else if (alarmsRemaining == 0)
	            {
	                _Zone.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.SHOUT, npc.getName(), "De-activate the alarm."));
	                deleteAllMobs();
	                Spawn(1);
	            }
	            break;
			case 18339:
	            if (getRemainingMonsters(npc) == 0)
	                Spawn(2);
	            break;
			case 18334:
	            if (getRemainingMonsters(npc) == 0)
	            {
	                deleteAllMobs();
	                openDoor(25150045);
	                openDoor(25150046);
	                cancelQuestTimers("check");
	                startQuestTimer("waiting", null, null, 180000);
	            }
	            break;	                        
		}
		
		return super.onKill(npc, killer, isPet);
	}

    private int getRemainingMonsters(L2Npc npc)
    {
        _roomMobs.remove(npc);
        return  (int)_roomMobs.stream().filter(m -> m.getNpcId() == npc.getNpcId()).count();
    }
    
    private void deleteAllMobs()
    {
        for (L2Npc mob : _roomMobs)
            mob.deleteMe();
        
        for (L2Npc mob : _Zone.getKnownTypeInside(L2Npc.class))
            mob.deleteMe();
        
        _roomMobs.clear();
        
        if (_frintezza != null)
        {
            _frintezza.deleteMe();
            _frintezza = null;
        }
    }
    private static void closeDoors()
    {
        for (int i = 25150051; i <= 25150058; i++)
        	 closeDoor(i);
        
        for (int i = 25150061; i <= 25150070; i++)
        	 closeDoor(i);
        
        closeDoor(25150042);
        closeDoor(25150043);
        closeDoor(25150045);
        closeDoor(25150046);
    }
    
    private static void openDoor(int doorid)
    {
        DoorData.getInstance().getDoor(doorid).openMe();
    }
    
    private static void closeDoor(int doorid)
    {
        DoorData.getInstance().getDoor(doorid).closeMe();
    }
    
    private static void stopPcActions()
    {
        for (L2Character cha : _Zone.getKnownTypeInside(L2PcInstance.class))
        {
            cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            cha.abortAttack();
            cha.abortCast();
            cha.disableAllSkills();
            cha.setTarget(null);
            cha.stopMove(null);
            cha.setIsImmobilized(true);
        }
    }
    
    private static void startPcActions()
    {
    	for (L2Character cha : _Zone.getKnownTypeInside(L2PcInstance.class))
        {
            cha.enableAllSkills();
            cha.setIsImmobilized(false);
        }
    }
   
    private void stopAttacks()
    {
        cancelQuestTimers("skill_ai");
        cancelQuestTimers("songs_play");
        cancelQuestTimers("songs_effect");
        
        if (_frintezza != null)
            _Zone.broadcastPacket(new MagicSkillCanceld(_frintezza.getObjectId()));
    }
    
    private static void stopNpcActions()
    {
        for (L2Npc mob : _Zone.getKnownTypeInside(L2Npc.class))
        {
            if (mob.getNpcId() != FRINTEZZA)
            {
                mob.disableAllSkills();
                mob.setIsInvul(true);
                mob.setIsImmobilized(true);
            }
        }
    }
    
    private static void startNpcActions()
    {
        for (L2Npc mob : _Zone.getKnownTypeInside(L2Npc.class))
        {
            if (mob.getNpcId() != FRINTEZZA)
            {
                mob.enableAllSkills();
                mob.setRunning();
                mob.setIsInvul(false);
                mob.setIsImmobilized(false);
            }
        }
    }

    private void Spawn(int spawn)
    {
        if (spawn == 1)
        {
            for (int i = 41; i <= 44; i++)
                _roomMobs.add(addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0, false));
            
            for (int i = 25150051; i <= 25150058; i++)
            	openDoor(i);
            
            openDoor(25150042);
            openDoor(25150043);
        }
        else
        {
        	closeDoor(25150042);
        	closeDoor(25150043);
        	closeDoor(25150045);
        	closeDoor(25150046);
            
            for (int i = 25150061; i <= 25150070; i++)
            	openDoor(i);
            
            for (int i = 45; i <= 131; i++)
                _roomMobs.add(addSpawn(_mobLoc[i][0], _mobLoc[i][1], _mobLoc[i][2], _mobLoc[i][3], _mobLoc[i][4], false, 0, false));
        }
    }
    
	public static void main(String[] args)
	{
		new Frintezza();
	}
}
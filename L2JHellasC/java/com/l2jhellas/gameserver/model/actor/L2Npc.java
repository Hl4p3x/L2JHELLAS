package com.l2jhellas.gameserver.model.actor;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.SevenSigns;
import com.l2jhellas.gameserver.SevenSignsFestival;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.datatables.sql.SpawnTable;
import com.l2jhellas.gameserver.datatables.xml.HelperBuffData;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.datatables.xml.MultisellData;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jhellas.gameserver.instancemanager.QuestManager;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.instancemanager.games.Lottery;
import com.l2jhellas.gameserver.model.AutoChatHandler;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2DropCategory;
import com.l2jhellas.gameserver.model.L2DropData;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.MobGroupTable;
import com.l2jhellas.gameserver.model.actor.instance.L2ControllableMobInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2FestivalGuideInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcWalkerInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2TeleporterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2WarehouseInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.stat.NpcStat;
import com.l2jhellas.gameserver.model.actor.status.NpcStatus;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.model.entity.events.engines.EventManager;
import com.l2jhellas.gameserver.model.entity.olympiad.Olympiad;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import com.l2jhellas.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.MoveToPawn;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.NpcSay;
import com.l2jhellas.gameserver.network.serverpackets.RadarControl;
import com.l2jhellas.gameserver.network.serverpackets.ServerObjectInfo;
import com.l2jhellas.gameserver.network.serverpackets.SocialAction;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.taskmanager.DecayTaskManager;
import com.l2jhellas.gameserver.taskmanager.RandomAnimationTaskManager;
import com.l2jhellas.gameserver.templates.L2HelperBuff;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.StringUtil;

public class L2Npc extends L2Character
{
	public static final int INTERACTION_DISTANCE = 150;
	
	private L2Spawn _spawn;
	
	private boolean _isBusy = false;
	volatile boolean _isDecayed = false;		
	public boolean isEventMob = false, _isEventMobCTF = false, _isCTF_throneSpawn = false, _isCTF_Flag = false;
	public boolean _isEventMobTvT = false;
	public boolean _isEventMobDM = false;
	public boolean _isEventMobVIP = false;
	public boolean _isEventVIPNPC = false, _isEventVIPNPCEnd = false;
    private boolean _isInTown = false;
	
	private int _isSpoiledBy = 0;
	public int pathfindCount = 0;
	public int pathfindTime = 0;	
	private int _castleIndex = -2;
	private int _currentLHandId; // normally this shouldn't change from the template, but there exist exceptions
	private int _currentRHandId; // normally this shouldn't change from the template, but there exist exceptions
	private int _currentCollisionHeight; // used for npc grow effect skills
	private int _currentCollisionRadius; // used for npc grow effect skills
	private int _scriptValue = 0;
	private long lastSocialBroadcast = 0;
	
	
	private String _busyMessage = "";
	public String _CTF_FlagTeamName;
	
	public void onRandomAnimation()
	{
		long current = System.currentTimeMillis();
		
		if (current - lastSocialBroadcast > 4000)
		{
			lastSocialBroadcast = current;
			// Send a packet SocialAction to all L2PcInstance in the _KnownPlayers of the L2NpcInstance
			broadcastPacket(new SocialAction(getObjectId(), Rnd.get(2, 3)), 700);
		}
		
	}
	
	public void onRandomAnimation(int id)
	{
		long current = System.currentTimeMillis();
		
		if (current - lastSocialBroadcast > 4000)
		{
			lastSocialBroadcast = current;
			broadcastPacket(new SocialAction(getObjectId(), id), 700);
		}
		
	}
	
	public void startRandomAnimationTimer()
	{
		if (!hasRandomAnimation())
			return;
		
		final int timer = (isMob()) ? Rnd.get(Config.MIN_MONSTER_ANIMATION, Config.MAX_MONSTER_ANIMATION) : Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION);
		RandomAnimationTaskManager.getInstance().add(this, timer);
	}
	
	public boolean hasRandomAnimation()
	{
		return (Config.MAX_NPC_ANIMATION > 0);
	}
	
	public class destroyTemporalNPC implements Runnable
	{
		private final L2Spawn _oldSpawn;
		
		public destroyTemporalNPC(L2Spawn spawn)
		{
			_oldSpawn = spawn;
		}
		
		@Override
		public void run()
		{
			try
			{
				_oldSpawn.getLastSpawn().deleteMe();
				_oldSpawn.stopRespawn();
				SpawnTable.getInstance().deleteSpawn(_oldSpawn, false);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	public class destroyTemporalSummon implements Runnable
	{
		L2Summon _summon;
		L2PcInstance _player;
		
		public destroyTemporalSummon(L2Summon summon, L2PcInstance player)
		{
			_summon = summon;
			_player = player;
		}
		
		@Override
		public void run()
		{
			_summon.unSummon(_player);
		}
	}
	
	public L2Npc(int objectId, L2NpcTemplate template)
	{
		// Call the L2Character constructor to set the _template of the L2Character, copy skills from template to object
		// and link _calculators to NPC_STD_CALCULATOR
		super(objectId, template);
		
		getStat(); // init stats
		getStatus(); // init status
		initCharStatusUpdateValues();
		
		// initialize the "current" equipment
		_currentLHandId = getTemplate().lhand;
		_currentRHandId = getTemplate().rhand;
		// initialize the "current" collisions
		_currentCollisionHeight = getTemplate().collisionHeight;
		_currentCollisionRadius = getTemplate().collisionRadius;
		
		// Set the name of the L2Character
		setName(template.name);
	}
	
	@Override
	public NpcStat getStat()
	{
		if ((super.getStat() == null) || !(super.getStat() instanceof NpcStat))
			setStat(new NpcStat(this));
		return (NpcStat) super.getStat();
	}
	
	@Override
	public NpcStatus getStatus()
	{
		if ((super.getStatus() == null) || !(super.getStatus() instanceof NpcStatus))
			setStatus(new NpcStatus(this));
		return (NpcStatus) super.getStatus();
	}
	
	@Override
	public final L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	public int getNpcId()
	{
		return getTemplate().npcId;
	}
	
	public final String getFactionId()
	{
		return getTemplate().factionId;
	}
	
	@Override
	public final int getLevel()
	{
		return getTemplate().level;
	}
	
	public boolean isAggressive()
	{
		return false;
	}
	
	public int getAggroRange()
	{
		return getTemplate().aggroRange;
	}
	
	public int getFactionRange()
	{
		return getTemplate().factionRange;
	}
	
	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead;
	}
	
	@Override
	public void updateAbnormalEffect(AbnormalEffect mask)
	{
		// NpcInfo info = new NpcInfo(this);
		// broadcastPacket(info);
		
		// Send a Server->Client packet NpcInfo with state of abnormal effect to all L2PcInstance in the _KnownPlayers of the L2NpcInstance
		for (L2PcInstance player : L2World.getInstance().getVisibleObjects(this, L2PcInstance.class, 2000))
			if (player != null)
			{
				if (getMoveSpeed() == 0)
					player.sendPacket(new ServerObjectInfo(this, player));
				else
					player.sendPacket(new NpcInfo(this, player));
			}
	}
	
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2FestivalGuideInstance)
			return 10000;
		
		if (object instanceof L2NpcInstance || !(object instanceof L2Character))
			return 0;
		
		if (object instanceof L2Playable)
			return 1500;
		
		return 500;
	}
	
	public int getDistanceToForgetObject(L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	public int getLeftHandItem()
	{
		return _currentLHandId;
	}
	
	public int getRightHandItem()
	{
		return _currentRHandId;
	}
	
	public boolean isSpoil()
	{
		return _isSpoiledBy > 0;
	}

	public final int getIsSpoiledBy()
	{
		return _isSpoiledBy;
	}
	
	public final void setIsSpoiledBy(int value)
	{
		_isSpoiledBy = value;
	}
	
	public final boolean isBusy()
	{
		return _isBusy;
	}
	
	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}
	
	public final String getBusyMessage()
	{
		return _busyMessage;
	}
	
	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}
	      
	protected boolean canTarget(L2PcInstance player)
	{
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}		
		return true;
	}
	
	protected boolean canInteract(L2PcInstance player)
	{
		// Can't interact while casting a spell.
		if (player.isCastingNow())
			return false;
		
		// Can't interact while died.
		if (player.isDead() || player.isFakeDeath())
			return false;

		// Can't interact sitted.
		if (player.isSitting())
			return false;
		
		// Can't interact in shop mode, or during a transaction or a request.
		if (player.isInStoreMode() || player.isProcessingTransaction())
			return false;
		
		// Can't interact if regular distance doesn't match.
		if (!isInsideRadius(player, INTERACTION_DISTANCE, true, false))
			return false;
		
		return true;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;
		
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			if (isAutoAttackable(player) && !isAlikeDead())
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			else
			{
				if (!canInteract(player))
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				else
				{
					if(isAlikeDead())
					{
					   player.sendPacket(ActionFailed.STATIC_PACKET);
					   return;
					}
					
					if (player.isMoving() || player.isInCombat())
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					
					if (!player.isSitting())
					     player.sendPacket(new MoveToPawn(player, this, INTERACTION_DISTANCE));
					
					if (isMoving())
						 player.stopMove(null);

					player.sendPacket(ActionFailed.STATIC_PACKET);
					
					if (hasRandomAnimation() && !isWalker())
						onRandomAnimation();
										
					if (player.isInFunEvent() && EventManager.getInstance().getCurrentEvent().onTalkNpc(this, player))
						return;

					List<Quest> scripts = getTemplate().getEventQuests(QuestEventType.QUEST_START);
					if (scripts != null && !scripts.isEmpty())
						player.setLastQuestNpcObject(getObjectId());

					scripts = getTemplate().getEventQuests(QuestEventType.ON_FIRST_TALK);
					if (scripts != null && scripts.size() == 1)
						scripts.get(0).notifyFirstTalk(this, player);
					else
						showChatWindow(player);	
				}
			}
		}
	}
	
	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player == null)
			return;
		
		player.setTarget(this);
		player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
		
		if (isAutoAttackable(player))
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
		}
		
		if (player.getAccessLevel().isGm())
		{		
			if (!Config.ALT_GAME_VIEWNPC)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(0);
				StringBuilder html1 = new StringBuilder("<html><body><center><font color=\"LEVEL\">NPC Information</font></center>");
				String className = getClass().getName().substring(43);
				html1.append("<br>");

				html1.append("Instance Type: " + className + "<br1>Faction: " + getFactionId() + "<br1>Location ID: " + (getSpawn() != null ? getSpawn().getLocation() : 0) + "<br1>");

				if (this instanceof L2ControllableMobInstance)
					html1.append("Mob Group: " + MobGroupTable.getInstance().getGroupForMob((L2ControllableMobInstance) this).getGroupId() + "<br>");
				else
					html1.append("Respawn Time: " + (getSpawn() != null ? (getSpawn().getRespawnDelay() / 1000) + "  Seconds<br>" : "?  Seconds<br>"));

				html1.append("<table border=\"0\" width=\"100%\">");
				html1.append("<tr><td>Object ID</td><td>" + getObjectId() + "</td><td>NPC ID</td><td>" + getTemplate().npcId + "</td></tr>");
				html1.append("<tr><td>Coords</td><td>" + getX() + "," + getY() + "," + getZ() + "</td></tr>");			
				html1.append("<tr><td>Level</td><td>" + getLevel() + "</td><td>Aggro</td><td>" + ((this instanceof L2Attackable) ? ((L2Attackable) this).getAggroRange() : 0) + "</td></tr>");
				html1.append("</table><br>");

				html1.append("<font color=\"LEVEL\">Combat</font>");
				html1.append("<table border=\"0\" width=\"100%\">");
				html1.append("<tr><td>Current HP</td><td>" + getCurrentHp() + "</td><td>Current MP</td><td>" + getCurrentMp() + "</td></tr>");
				html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1, this, null)) + "*" + getStat().calcStat(Stats.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
				html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
				html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
				html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate(null) + "</td></tr>");
				html1.append("<tr><td>Critical</td><td>" + getCriticalHit(null, null) + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
				html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
				html1.append("</table><br>");

				html1.append("<font color=\"LEVEL\">Basic Stats</font>");
				html1.append("<table border=\"0\" width=\"100%\">");
				html1.append("<tr><td>STR</td><td>" + getSTR() + "</td><td>DEX</td><td>" + getDEX() + "</td><td>CON</td><td>" + getCON() + "</td></tr>");
				html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getWIT() + "</td><td>MEN</td><td>" + getMEN() + "</td></tr>");
				html1.append("</table>");

				html1.append("<br><center><table>");
				html1.append("<tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				html1.append("<tr><td><button value=\"Show Skillist\" action=\"bypass -h admin_show_skilllist_npc " + getTemplate().npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				html1.append("<tr><td align=center><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				html1.append("<tr><td align=center><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				html1.append("</table></center><br>");
				html1.append("</body></html>");

				html.setHtml(html1.toString());
				player.sendPacket(html);
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(0);
				StringBuilder html1 = new StringBuilder("<html><body>");

				html1.append("<br><center><font color=\"LEVEL\">[Combat Stats]</font></center>");
				html1.append("<table border=0 width=\"100%\">");
				html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1, this, null)) + "*" + (int) getStat().calcStat(Stats.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
				html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
				html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
				html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate(null) + "</td></tr>");
				html1.append("<tr><td>Critical</td><td>" + getCriticalHit(null, null) + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
				html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
				html1.append("<tr><td>Race</td><td>" + getTemplate().race + "</td><td></td><td></td></tr>");
				html1.append("</table>");

				html1.append("<br><center><font color=\"LEVEL\">[Basic Stats]</font></center>");
				html1.append("<table border=0 width=\"100%\">");
				html1.append("<tr><td>STR</td><td>" + getSTR() + "</td><td>DEX</td><td>" + getDEX() + "</td><td>CON</td><td>" + getCON() + "</td></tr>");
				html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getWIT() + "</td><td>MEN</td><td>" + getMEN() + "</td></tr>");
				html1.append("</table>");

				html1.append("<tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				html1.append("<tr><td><button value=\"Show Skillist\" action=\"bypass -h admin_show_skilllist_npc " + getTemplate().npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				html1.append("<tr><td align=center><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
				html1.append("<tr><td align=center><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");

				html1.append("<center><font color=\"LEVEL\">[Drop Info]</font></center>");
				html1.append("<table border=1 width=\"100%\">");
				html1.append("<tr><td><center>Item Name</center></td><td width=\"55\" valign=middle align=center><center>Category</center></td><td width=\"50\" valign=middle align=center><center>Chance</center></td></tr>");

				if (getTemplate().getDropData() != null)
					for (L2DropCategory cat : getTemplate().getDropData())
						for (L2DropData drop : cat.getAllDrops())
						{
							String name = ItemTable.getInstance().getTemplate(drop.getItemId()).getItemName();
							html1.append("<tr><td><font color=\"33EEEE\">" + name + "</font></td><td width=\"55\" valign=middle align=center>" + (drop.isQuestDrop() ? "<font color=\"FF6600\">Quest</font>" : (cat.isSweep() ? "<font color=\"LEVEL\">Sweep</font>" : "<font color=\"33FF77\">Drop</font>")) + "</td><td width=\"50\" valign=middle align=center>" + (drop.getChance() >= 10000 ? (double) drop.getChance() / 10000 : drop.getChance() < 10000 ? (double) drop.getChance() / 10000 : "N/A") + "%</td></tr>");
						}

				html1.append("</table>");
				html1.append("</body></html>");

				html.setHtml(html1.toString());
				player.sendPacket(html);
			}
		
		}
		else if (Config.PLAYER_ALT_GAME_VIEWNPC)
		{		
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			StringBuilder html1 = new StringBuilder("<html><body>");
			html1.append("<center><font color=\"LEVEL\">[Drop Info]</font></center>");
			html1.append("<table border=1 width=\"100%\">");
			html1.append("<tr><td><center>Item Name</center></td><td width=\"55\" valign=middle align=center><center>Category</center></td><td width=\"50\" valign=middle align=center><center>Chance</center></td></tr>");
			
			if (getTemplate().getDropData() != null)
				for (L2DropCategory cat : getTemplate().getDropData())
					for (L2DropData drop : cat.getAllDrops())
					{
						String name = ItemTable.getInstance().getTemplate(drop.getItemId()).getItemName();
						html1.append("<tr><td><font color=\"33EEEE\">" + name + "</font></td><td width=\"55\" valign=middle align=center>" + (drop.isQuestDrop() ? "<font color=\"FF6600\">Quest</font>" : (cat.isSweep() ? "<font color=\"LEVEL\">Sweep</font>" : "<font color=\"33FF77\">Drop</font>")) + "</td><td width=\"50\" valign=middle align=center>" + (drop.getChance() >= 10000 ? (double) drop.getChance() / 10000 : drop.getChance() < 10000 ? (double) drop.getChance() / 10000 : "N/A") + "%</td></tr>");
					}
			
			html1.append("</table>");
			html1.append("</body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public final Castle getCastle()
	{
		return CastleManager.getInstance().getCastleById(MapRegionTable.getAreaCastle(this.getX(),this.getY()));
	}
	
	public final boolean getIsInTown()
	{
		if (_castleIndex < 0)
			getCastle();
		return _isInTown;
	}
	
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// if (canInteract(player))
		{
			if (isBusy() && getBusyMessage().length() > 0)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/npcbusy.htm");
				html.replace("%busymessage%", getBusyMessage());
				html.replace("%npcname%", getName());
				html.replace("%playername%", player.getName());
				player.sendPacket(html);
			}
			else if (command.equalsIgnoreCase("TerritoryStatus"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				{
					if (getCastle().getOwnerId() > 0)
					{
						html.setFile("data/html/territorystatus.htm");
						L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
						html.replace("%clanname%", clan.getName());
						html.replace("%clanleadername%", clan.getLeaderName());
					}
					else
					{
						html.setFile("data/html/territorynoclan.htm");
					}
				}
				html.replace("%castlename%", getCastle().getName());
				html.replace("%taxpercent%", "" + getCastle().getTaxPercent());
				html.replace("%objectId%", String.valueOf(getObjectId()));
				{
					if (getCastle().getCastleId() > 6)
					{
						html.replace("%territory%", "The Kingdom of Elmore");
					}
					else
					{
						html.replace("%territory%", "The Kingdom of Aden");
					}
				}
				player.sendPacket(html);
			}
			else if (command.startsWith("Quest"))
			{
				String quest = "";
				try
				{
					quest = command.substring(5).trim();
				}
				catch (IndexOutOfBoundsException ioobe)
				{
				}
				
				if (quest.isEmpty())
					QuestWindowGeneral(player, this);
				else
					QuestWindowSingle(player, this, QuestManager.getInstance().getQuest(quest));
				
			}
			else if (command.startsWith("Chat"))
			{
				int val = 0;
				try
				{
					val = Integer.parseInt(command.substring(5));
				}
				catch (IndexOutOfBoundsException ioobe)
				{
				}
				catch (NumberFormatException nfe)
				{
				}
				showChatWindow(player, val);
			}
			else if (command.startsWith("Link"))
			{
				String path = command.substring(5).trim();
				if (path.indexOf("..") != -1)
					return;
				String filename = "data/html/" + path;
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
			}
			else if (command.startsWith("NobleTeleport"))
			{
				if (!player.isNoble())
				{
					String filename = "data/html/teleporter/nobleteleporter-no.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					player.sendPacket(html);
					return;
				}
				int val = 0;
				try
				{
					val = Integer.parseInt(command.substring(5));
				}
				catch (IndexOutOfBoundsException ioobe)
				{
				}
				catch (NumberFormatException nfe)
				{
				}
				showChatWindow(player, val);
			}
			else if (command.startsWith("Loto"))
			{
				int val = 0;
				try
				{
					val = Integer.parseInt(command.substring(5));
				}
				catch (IndexOutOfBoundsException ioobe)
				{
				}
				catch (NumberFormatException nfe)
				{
				}
				if (val == 0)
				{
					// new loto ticket
					for (int i = 0; i < 5; i++)
						player.setLoto(i, 0);
				}
				showLotoWindow(player, val);
			}
			else if (command.startsWith("CPRecovery"))
			{
				makeCPRecovery(player);
			}
			else if (command.startsWith("SupportMagic"))
			{
				makeSupportMagic(player);
			}
			else if (command.startsWith("GiveBlessing"))
			{
				giveBlessingSupport(player);
			}
			else if (command.startsWith("multisell"))
			{
				MultisellData.getInstance().SeparateAndSend(Integer.parseInt(command.substring(9).trim()), player, false, getCastle().getTaxRate());
			}
			else if (command.startsWith("exc_multisell"))
			{
				MultisellData.getInstance().SeparateAndSend(Integer.parseInt(command.substring(13).trim()), player, true, getCastle().getTaxRate());
			}
			else if (command.startsWith("Augment"))
			{
				int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
				switch (cmdChoice)
				{
					case 1:
						player.sendPacket(SystemMessageId.SELECT_THE_ITEM_TO_BE_AUGMENTED);
						player.sendPacket(ExShowVariationMakeWindow.STATIC_PACKET);
						break;
					case 2:
						player.sendPacket(SystemMessageId.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION);
						player.sendPacket(ExShowVariationCancelWindow.STATIC_PACKET);
						break;
				}
			}
			else if (command.startsWith("npcfind_byid"))
			{
				try
				{
					L2Spawn spawn = SpawnTable.getInstance().getSpawn(Integer.parseInt(command.substring(12).trim()));
					
					if (spawn != null)
						player.sendPacket(new RadarControl(0, 1, spawn.getLocx(), spawn.getLocy(), spawn.getLocz()));
				}
				catch (NumberFormatException nfe)
				{
					player.sendMessage("Wrong command parameters");
				}
			}
			else if (command.startsWith("EnterRift"))
			{
				try
				{
					Byte b1 = Byte.parseByte(command.substring(10)); // Selected Area: Recruit, Soldier etc
					DimensionalRiftManager.getInstance().start(player, b1, this);
				}
				catch (Exception e)
				{
				}
			}
			else if (command.startsWith("ChangeRiftRoom"))
			{
				if (player.isInParty() && player.getParty().isInDimensionalRift())
				{
					player.getParty().getDimensionalRift().manualTeleport(player, this);
				}
				else
				{
					DimensionalRiftManager.getInstance().handleCheat(player, this);
				}
			}
			else if (command.startsWith("ExitRift"))
			{
				if (player.isInParty() && player.getParty().isInDimensionalRift())
				{
					player.getParty().getDimensionalRift().manualExitRift(player, this);
				}
				else
				{
					DimensionalRiftManager.getInstance().handleCheat(player, this);
				}
			}
		}
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		// regular NPCs don't have weapons instances
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		// Get the weapon identifier equipped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().rhand;
		
		if (weaponId < 1)
			return null;
		
		// Get the weapon item equipped in the right hand of the L2NpcInstance
		L2Item item = ItemTable.getInstance().getTemplate(getTemplate().rhand);
		
		if (!(item instanceof L2Weapon))
			return null;
		
		return (L2Weapon) item;
	}
	
	public void giveBlessingSupport(L2PcInstance player)
	{
		if (player == null)
			return;
		
		// Blessing of protection - author kerberos_20. Used codes from Rayan - L2Emu project.
		// Prevent a cursed weapon wielder of being buffed - I think no need of that because karma check > 0
		if (player.isCursedWeaponEquiped())
			return;
		
		int player_level = player.getLevel();
		// Select the player
		setTarget(player);
		// If the player is too high level, display a message and return
		if (player_level > 39 || player.getClassId().level() >= 2)
		{
			String content = "<html><body>Newbie Guide:<br>I'm sorry, but you are not eligible to receive the protection blessing.<br1>It can only be bestowed on <font color=\"LEVEL\">characters below level 39 who have not made a seccond transfer.</font></body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		L2Skill skill = SkillTable.getInstance().getInfo(5182, 1);
		doCast(skill);
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// regular NPCs don't have weapons instances
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		// Get the weapon identifier equipped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().lhand;
		
		if (weaponId < 1)
			return null;
		
		// Get the weapon item equipped in the right hand of the L2NpcInstance
		L2Item item = ItemTable.getInstance().getTemplate(getTemplate().lhand);
		
		if (!(item instanceof L2Weapon))
			return null;
		
		return (L2Weapon) item;
	}
	
	public void insertObjectIdAndShowChatWindow(L2PcInstance player, String content)
	{
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
		NpcHtmlMessage npcReply = new NpcHtmlMessage(getObjectId());
		npcReply.setHtml(content);
		player.sendPacket(npcReply);
	}
	
	public String getHtmlPath(int npcId, int val)
	{
		String filename;
		
		if (val == 0)
			filename = "data/html/default/" + npcId + ".htm";
		else
			filename = "data/html/default/" + npcId + "-" + val + ".htm";
		
		if (HtmCache.getInstance().isLoadable(filename))
			return filename;
		
		return "data/html/npcdefault.htm";
	}

	
	public static void QuestWindowGeneral(L2PcInstance player, L2Npc npc)
	{
		final List<Quest> quests = new ArrayList<>();
		
		List<Quest> scripts = npc.getTemplate().getEventQuests(QuestEventType.ON_TALK);
		if (scripts != null)
		{
			for (Quest quest : scripts)
			{
				if (quest == null || !quest.isRealQuest() || quests.contains(quest))
					continue;
				
				QuestState qs = player.getQuestState(quest.getName());
				if (qs == null || qs.isCreated())
					continue;
				
				quests.add(quest);
			}
		}
		
		scripts = npc.getTemplate().getEventQuests(QuestEventType.QUEST_START);
		if (scripts != null)
		{
			for (Quest quest : scripts)
			{
				if (quest == null || !quest.isRealQuest() || quests.contains(quest))
					continue;
				
				quests.add(quest);
			}
		}
		
		if (quests.isEmpty())
			QuestWindowSingle(player, npc, null);
		else if (quests.size() == 1)
			QuestWindowSingle(player, npc, quests.get(0));
		else
			QuestWindowChoose(player, npc, quests);
	}
	
	public static void QuestWindowChoose(L2PcInstance player, L2Npc npc, List<Quest> quests)
	{
		final StringBuilder sb = new StringBuilder("<html><body>");
		
		for (Quest q : quests)
		{
			StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_Quest ", q.getName(), "\">[", q.getDescr());
			
			final QuestState qs = player.getQuestState(q.getName());
			if (qs != null && qs.isStarted())
				sb.append(" (In Progress)]</a><br>");
			else if (qs != null && qs.isCompleted())
				sb.append(" (Done)]</a><br>");
			else
				sb.append("]</a><br>");
		}
		
		sb.append("</body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setHtml(sb.toString());
		html.replace("%objectId%", npc.getObjectId());
		player.sendPacket(html);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public static void QuestWindowSingle(L2PcInstance player, L2Npc npc, Quest quest)
	{
		if (quest == null)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setHtml(Quest.getNoQuestMsg());
			player.sendPacket(html);
			
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (quest.isRealQuest() && (player.getWeightPenalty() > 2 || player.getInventoryLimit() * 0.8 <= player.getInventory().getSize()))
		{
			player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
			return;
		}
		
		QuestState qs = player.getQuestState(quest.getName());
		if (qs == null)
		{
			if (quest.isRealQuest() && player.getAllQuests(false).size() >= 25)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setHtml(Quest.getTooMuchQuestsMsg());
				player.sendPacket(html);
				
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			final List<Quest> scripts = npc.getTemplate().getEventQuests(QuestEventType.QUEST_START);
			if (scripts != null && scripts.contains(quest))
				qs = quest.newQuestState(player);
		}
		
		if (qs != null)
			quest.notifyTalk(npc, qs.getPlayer());
	}
	
	// 0 - first buy lottery ticket window
	// 1-20 - buttons
	// 21 - second buy lottery ticket window
	// 22 - selected ticket with 5 numbers
	// 23 - current lottery jackpot
	// 24 - Previous winning numbers/Prize claim
	// >24 - check lottery ticket by item object id
	public void showLotoWindow(L2PcInstance player, int val)
	{
		int npcId = getTemplate().npcId;
		String filename;
		SystemMessage sm;
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		if (val == 0) // 0 - first buy lottery ticket window
		{
			filename = (getHtmlPath(npcId, 1));
			html.setFile(filename);
		}
		else if (val >= 1 && val <= 21) // 1-20 - buttons, 21 - second buy lottery ticket window
		{
			if (!Lottery.getInstance().isStarted())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
				return;
			}
			
			filename = (getHtmlPath(npcId, 5));
			html.setFile(filename);
			
			int count = 0;
			int found = 0;
			// counting buttons and unsetting button if found
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == val)
				{
					// unsetting button
					player.setLoto(i, 0);
					found = 1;
				}
				else if (player.getLoto(i) > 0)
				{
					count++;
				}
			}
			
			// if not reached limit 5 and not unseted value
			if (count < 5 && found == 0 && val <= 20)
				for (int i = 0; i < 5; i++)
					if (player.getLoto(i) == 0)
					{
						player.setLoto(i, val);
						break;
					}
			
			// setting pushed buttons
			count = 0;
			for (int i = 0; i < 5; i++)
				if (player.getLoto(i) > 0)
				{
					count++;
					String button = String.valueOf(player.getLoto(i));
					if (player.getLoto(i) < 10)
						button = "0" + button;
					String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
					String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
					html.replace(search, replace);
				}
			
			if (count == 5)
			{
				String search = "0\">Return";
				String replace = "22\">The winner selected the numbers above.";
				html.replace(search, replace);
			}
		}
		else if (val == 22) // 22 - selected ticket with 5 numbers
		{
			if (!Lottery.getInstance().isStarted())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				// tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
				return;
			}
			
			int price = Config.ALT_LOTTERY_TICKET_PRICE;
			int lotonumber = Lottery.getInstance().getId();
			int enchant = 0;
			int type2 = 0;
			
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == 0)
					return;
				
				if (player.getLoto(i) < 17)
					enchant += Math.pow(2, player.getLoto(i) - 1);
				else
					type2 += Math.pow(2, player.getLoto(i) - 17);
			}
			if (player.getAdena() < price)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				player.sendPacket(sm);
				return;
			}
			if (!player.reduceAdena("Loto", price, this, true))
				return;
			Lottery.getInstance().increasePrize(price);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2);
			sm.addNumber(lotonumber);
			sm.addItemName(4442);
			player.sendPacket(sm);
			
			L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), 4442);
			item.setCount(1);
			item.setCustomType1(lotonumber);
			item.setEnchantLevel(enchant);
			item.setCustomType2(type2);
			player.getInventory().addItem("Loto", item, player, this);
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			L2ItemInstance adenaupdate = player.getInventory().getItemByItemId(57);
			iu.addModifiedItem(adenaupdate);
			player.sendPacket(iu);
			
			filename = (getHtmlPath(npcId, 3));
			html.setFile(filename);
		}
		else if (val == 23) // 23 - current lottery jackpot
		{
			filename = (getHtmlPath(npcId, 3));
			html.setFile(filename);
		}
		else if (val == 24) // 24 - Previous winning numbers/Prize claim
		{
			filename = (getHtmlPath(npcId, 4));
			html.setFile(filename);
			
			int lotonumber = Lottery.getInstance().getId();
			String message = "";
			for (L2ItemInstance item : player.getInventory().getItems())
			{
				if (item == null)
					continue;
				if (item.getItemId() == 4442 && item.getCustomType1() < lotonumber)
				{
					message = message + "<a action=\"bypass -h npc_%objectId%_Loto " + item.getObjectId() + "\">" + item.getCustomType1() + " Event Number ";
					int[] numbers = Lottery.getInstance().decodeNumbers(item.getEnchantLevel(), item.getCustomType2());
					for (int i = 0; i < 5; i++)
					{
						message += numbers[i] + " ";
					}
					int[] check = Lottery.getInstance().checkTicket(item);
					if (check[0] > 0)
					{
						switch (check[0])
						{
							case 1:
								message += "- 1st Prize";
								break;
							case 2:
								message += "- 2nd Prize";
								break;
							case 3:
								message += "- 3th Prize";
								break;
							case 4:
								message += "- 4th Prize";
								break;
						}
						message += " " + check[1] + "a.";
					}
					message += "</a><br>";
				}
			}
			if (message == "")
			{
				message += "There is no winning lottery ticket...<br>";
			}
			html.replace("%result%", message);
		}
		else if (val > 24) // >24 - check lottery ticket by item object id
		{
			int lotonumber = Lottery.getInstance().getId();
			L2ItemInstance item = player.getInventory().getItemByObjectId(val);
			if (item == null || item.getItemId() != 4442 || item.getCustomType1() >= lotonumber)
				return;
			int[] check = Lottery.getInstance().checkTicket(item);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addItemName(4442);
			player.sendPacket(sm);
			
			int adena = check[1];
			if (adena > 0)
				player.addAdena("Loto", adena, this, true);
			player.destroyItem("Loto", item, this, false);
			return;
		}
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%race%", "" + Lottery.getInstance().getId());
		html.replace("%adena%", "" + Lottery.getInstance().getPrize());
		html.replace("%ticket_price%", "" + Config.ALT_LOTTERY_TICKET_PRICE);
		html.replace("%prize5%", "" + (Config.ALT_LOTTERY_5_NUMBER_RATE * 100));
		html.replace("%prize4%", "" + (Config.ALT_LOTTERY_4_NUMBER_RATE * 100));
		html.replace("%prize3%", "" + (Config.ALT_LOTTERY_3_NUMBER_RATE * 100));
		html.replace("%prize2%", "" + Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE);
		html.replace("%enddate%", "" + DateFormat.getDateInstance().format(Lottery.getInstance().getEndDate()));
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void makeCPRecovery(L2PcInstance player)
	{
		if (getNpcId() != 31225 && getNpcId() != 31226)
			return;
		if (player.isCursedWeaponEquiped())
		{
			player.sendMessage("Go away, you're not welcome here.");
			return;
		}
		
		int neededmoney = 100;
		SystemMessage sm;
		if (!player.reduceAdena("RestoreCP", neededmoney, player.getLastFolkNPC(), true))
			return;
		player.setCurrentCp(player.getMaxCp());
		// cp restored
		sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
		sm.addString(player.getName());
		player.sendPacket(sm);
	}
	
	public void makeSupportMagic(L2PcInstance player)
	{
		if (player == null)
			return;
		
		// Prevent a cursed weapon wielder of being buffed
		if (player.isCursedWeaponEquiped())
			return;
		
		int player_level = player.getLevel();
		int lowestLevel = 0;
		int highestLevel = 0;
		
		// Select the player
		setTarget(player);
		
		// Calculate the min and max level between witch the player must be to obtain buff
		if (player.isMageClass())
		{
			lowestLevel = HelperBuffData.getInstance().getMagicClassLowestLevel();
			highestLevel = HelperBuffData.getInstance().getMagicClassHighestLevel();
		}
		else
		{
			lowestLevel = HelperBuffData.getInstance().getPhysicClassLowestLevel();
			highestLevel = HelperBuffData.getInstance().getPhysicClassHighestLevel();
		}
		
		// If the player is too high level, display a message and return
		if (player_level > highestLevel)
		{
			String content = "<html><body>Newbie Guide:<br>Only a <font color=\"LEVEL\">novice character of level " + highestLevel + " or less</font> can receive my support magic.<br>Your novice character is the first one that you created and raised in this world.</body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		
		// If the player is too low level, display a message and return
		if (player_level < lowestLevel)
		{
			String content = "<html><body>Come back here when you have reached level " + lowestLevel + ". I will give you support magic then.</body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		
		L2Skill skill = null;
		// Go through the Helper Buff list define in sql table helper_buff_list and cast skill
		
		for (L2HelperBuff helperBuffItem : HelperBuffData.getInstance().getHelperBuffTable())
		{
			if (helperBuffItem.isMagicClassBuff() == player.isMageClass())
			{
				if (player_level >= helperBuffItem.getLowerLevel() && player_level <= helperBuffItem.getUpperLevel())
				{
					skill = SkillTable.getInstance().getInfo(helperBuffItem.getSkillID(), helperBuffItem.getSkillLevel());
					if (skill.getSkillType() == L2SkillType.SUMMON)
						player.doCast(skill);
					else
						skill.getEffects(this, player);
				}
			}
		}
	}
	
	public void showChatWindow(L2PcInstance player)
	{
		showChatWindow(player, 0);
	}
	
	private boolean showPkDenyChatWindow(L2PcInstance player, String type)
	{
		String html = HtmCache.getInstance().getHtm("data/html/" + type + "/" + getNpcId() + "-pk.htm");
		
		if (html != null)
		{
			NpcHtmlMessage pkDenyMsg = new NpcHtmlMessage(getObjectId());
			pkDenyMsg.setHtml(html);
			player.sendPacket(pkDenyMsg);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		
		return false;
	}

	public void showChatWindow(L2PcInstance player, int val)
	{
		if (player.getKarma() > 0)
		{
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof L2MerchantInstance)
			{
				if (showPkDenyChatWindow(player, "merchant"))
					return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && this instanceof L2TeleporterInstance)
			{
				if (showPkDenyChatWindow(player, "teleporter"))
					return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && this instanceof L2WarehouseInstance)
			{
				if (showPkDenyChatWindow(player, "warehouse"))
					return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && this instanceof L2FishermanInstance)
			{
				if (showPkDenyChatWindow(player, "fisherman"))
					return;
			}
		}
		
		if (getTemplate().type == "L2Auctioneer" && val == 0)
			return;
		
		int npcId = getTemplate().npcId;
		
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
		int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS);
		int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
		boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
		int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		
		switch (npcId)
		{
			case 31078:
			case 31079:
			case 31080:
			case 31081:
			case 31082: // Dawn Priests
			case 31083:
			case 31084:
			case 31168:
			case 31692:
			case 31694:
			case 31997:
				switch (playerCabal)
				{
					case SevenSigns.CABAL_DAWN:
						if (isSealValidationPeriod)
							if (compWinner == SevenSigns.CABAL_DAWN)
								if (compWinner != sealGnosisOwner)
									filename += "dawn_priest_2c.htm";
								else
									filename += "dawn_priest_2a.htm";
							else
								filename += "dawn_priest_2b.htm";
						else
							filename += "dawn_priest_1b.htm";
						break;
					case SevenSigns.CABAL_DUSK:
						if (isSealValidationPeriod)
							filename += "dawn_priest_3b.htm";
						else
							filename += "dawn_priest_3a.htm";
						break;
					default:
						if (isSealValidationPeriod)
							if (compWinner == SevenSigns.CABAL_DAWN)
								filename += "dawn_priest_4.htm";
							else
								filename += "dawn_priest_2b.htm";
						else
							filename += "dawn_priest_1a.htm";
						break;
				}
				break;
			case 31085:
			case 31086:
			case 31087:
			case 31088: // Dusk Priest
			case 31089:
			case 31090:
			case 31091:
			case 31169:
			case 31693:
			case 31695:
			case 31998:
				switch (playerCabal)
				{
					case SevenSigns.CABAL_DUSK:
						if (isSealValidationPeriod)
							if (compWinner == SevenSigns.CABAL_DUSK)
								if (compWinner != sealGnosisOwner)
									filename += "dusk_priest_2c.htm";
								else
									filename += "dusk_priest_2a.htm";
							else
								filename += "dusk_priest_2b.htm";
						else
							filename += "dusk_priest_1b.htm";
						break;
					case SevenSigns.CABAL_DAWN:
						if (isSealValidationPeriod)
							filename += "dusk_priest_3b.htm";
						else
							filename += "dusk_priest_3a.htm";
						break;
					default:
						if (isSealValidationPeriod)
							if (compWinner == SevenSigns.CABAL_DUSK)
								filename += "dusk_priest_4.htm";
							else
								filename += "dusk_priest_2b.htm";
						else
							filename += "dusk_priest_1a.htm";
						break;
				}
				break;
			case 31095: //
			case 31096: //
			case 31097: //
			case 31098: // Enter Necropolises
			case 31099: //
			case 31100: //
			case 31101: //
			case 31102: //
				if (isSealValidationPeriod)
				{
					if (playerCabal != compWinner || sealAvariceOwner != compWinner)
					{
						switch (compWinner)
						{
							case SevenSigns.CABAL_DAWN:
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
								filename += "necro_no.htm";
								break;
							case SevenSigns.CABAL_DUSK:
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
								filename += "necro_no.htm";
								break;
							case SevenSigns.CABAL_NULL:
								filename = (getHtmlPath(npcId, val)); // do the default!
								break;
						}
					}
					else
						filename = (getHtmlPath(npcId, val)); // do the default!
				}
				else
				{
					if (playerCabal == SevenSigns.CABAL_NULL)
						filename += "necro_no.htm";
					else
						filename = (getHtmlPath(npcId, val)); // do the default!
				}
				break;
			case 31114: //
			case 31115: //
			case 31116: // Enter Catacombs
			case 31117: //
			case 31118: //
			case 31119: //
				if (isSealValidationPeriod)
				{
					if (playerCabal != compWinner || sealGnosisOwner != compWinner)
					{
						switch (compWinner)
						{
							case SevenSigns.CABAL_DAWN:
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
								filename += "cata_no.htm";
								break;
							case SevenSigns.CABAL_DUSK:
								player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
								filename += "cata_no.htm";
								break;
							case SevenSigns.CABAL_NULL:
								filename = (getHtmlPath(npcId, val)); // do the default!
								break;
						}
					}
					else
						filename = (getHtmlPath(npcId, val)); // do the default!
				}
				else
				{
					if (playerCabal == SevenSigns.CABAL_NULL)
						filename += "cata_no.htm";
					else
						filename = (getHtmlPath(npcId, val)); // do the default!
				}
				break;
			case 31111: // Gatekeeper Spirit (Disciples)
				if (playerCabal == sealAvariceOwner && playerCabal == compWinner)
				{
					switch (sealAvariceOwner)
					{
						case SevenSigns.CABAL_DAWN:
							filename += "spirit_dawn.htm";
							break;
						case SevenSigns.CABAL_DUSK:
							filename += "spirit_dusk.htm";
							break;
						case SevenSigns.CABAL_NULL:
							filename += "spirit_null.htm";
							break;
					}
				}
				else
				{
					filename += "spirit_null.htm";
				}
				break;
			case 31112: // Gatekeeper Spirit (Disciples)
				filename += "spirit_exit.htm";
				break;
			case 31127: //
			case 31128: //
			case 31129: // Dawn Festival Guides
			case 31130: //
			case 31131: //
				filename += "festival/dawn_guide.htm";
				break;
			case 31137: //
			case 31138: //
			case 31139: // Dusk Festival Guides
			case 31140: //
			case 31141: //
				filename += "festival/dusk_guide.htm";
				break;
			case 31092: // Black Marketeer of Mammon
				filename += "blkmrkt_1.htm";
				break;
			case 31113: // Merchant of Mammon
				switch (compWinner)
				{
					case SevenSigns.CABAL_DAWN:
						if (playerCabal != compWinner || playerCabal != sealAvariceOwner)
						{
							player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						break;
					case SevenSigns.CABAL_DUSK:
						if (playerCabal != compWinner || playerCabal != sealAvariceOwner)
						{
							player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						break;
				}
				filename += "mammmerch_1.htm";
				break;
			case 31126: // Blacksmith of Mammon
				switch (compWinner)
				{
					case SevenSigns.CABAL_DAWN:
						if (playerCabal != compWinner || playerCabal != sealGnosisOwner)
						{
							player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						break;
					case SevenSigns.CABAL_DUSK:
						if (playerCabal != compWinner || playerCabal != sealGnosisOwner)
						{
							player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
							player.sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						break;
				}
				filename += "mammblack_1.htm";
				break;
			case 31132:
			case 31133:
			case 31134:
			case 31135:
			case 31136: // Festival Witches
			case 31142:
			case 31143:
			case 31144:
			case 31145:
			case 31146:
				filename += "festival/festival_witch.htm";
				break;
			case 31688:
				if (player.isNoble())
					filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";
				else
					filename = (getHtmlPath(npcId, val));
				break;
			case 31690:
			case 31769:
			case 31770:
			case 31771:
			case 31772:
				if (player.isHero())
					filename = Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm";
				else
					filename = (getHtmlPath(npcId, val));
				break;
			default:
				if (npcId >= 31865 && npcId <= 31918)
				{
					filename += "rift/GuardianOfBorder.htm";
					break;
				}
				if ((npcId >= 31093 && npcId <= 31094) || (npcId >= 31172 && npcId <= 31201) || (npcId >= 31239 && npcId <= 31254))
					return;
				// Get the text of the selected HTML file in function of the npcId and of the page number
				filename = (getHtmlPath(npcId, val));
				break;
		}
		
		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		
		// String word = "npc-"+npcId+(val>0 ? "-"+val : "" )+"-dialog-append";
		
		if (this instanceof L2MerchantInstance)
			if (Config.LIST_PET_RENT_NPC.contains(npcId))
				html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");
		
		html.replace("%name%", getName());
		html.replace("%player_name%", player.getName());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void showChatWindow(L2PcInstance player, String filename)
	{
		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static int Savvato()
	{
		return Calendar.SATURDAY;
	}
	
	private static int dayofweek()
	{
		return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
	}
	
	public int getExpReward()
	{
		final double rateXp = getStat().calcStat(Stats.MAX_HP, 1, this, null);
		int exp = 0;
		
		if (Config.ALLOW_SATURDAY_RATE_XP_SP && dayofweek() == Savvato())
			exp = (int) (getTemplate().rewardExp * rateXp * Config.SATURDAY_RATE_XP);
		else
			exp = (int) (getTemplate().rewardExp * rateXp * Config.RATE_XP);
		
		return exp;
	}
	
	public int getSpReward()
	{
		double rateSp = getStat().calcStat(Stats.MAX_HP, 1, this, null);
		int sp = 0;
		
		if (Config.ALLOW_SATURDAY_RATE_XP_SP && dayofweek() == Savvato())
			sp = (int) (getTemplate().rewardSp * rateSp * Config.SATURDAY_RATE_SP);
		else
			sp = (int) (getTemplate().rewardSp * rateSp * Config.RATE_SP);
		
		return sp;
	}
	
	public int getExpReward(int isPremium)
	{
		double rateXp = getStat().calcStat(Stats.MAX_HP, 1, this, null);
		
		if (isPremium == 1)
			return (int) (getTemplate().rewardExp * rateXp * Config.PREMIUM_RATE_XP);

		return (int) (getTemplate().rewardExp * rateXp * Config.RATE_XP);
	}
	
	public int getSpReward(int isPremium)
	{
		double rateSp = getStat().calcStat(Stats.MAX_HP, 1, this, null);
		
		if (isPremium == 1)
			return (int) (getTemplate().rewardSp * rateSp * Config.PREMIUM_RATE_SP);

		return (int) (getTemplate().rewardSp * rateSp * Config.RATE_SP);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		// normally this wouldn't really be needed, but for those few exceptions,
		// we do need to reset the weapons back to the initial templated weapon.
		_currentLHandId = getTemplate().lhand;
		_currentRHandId = getTemplate().rhand;
		_currentCollisionHeight = getTemplate().collisionHeight;
		_currentCollisionRadius = getTemplate().collisionRadius;
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	public void setSpawn(L2Spawn spawn)
	{
		_spawn = spawn;
	}
	
	@Override
	public void onSpawn()
	{
		if (getNpcId() == 31127 || getNpcId() == 31137)
		    SevenSignsFestival.getInstance().npcSpawned(this);
		
		AutoChatHandler.getInstance().npcSpawned(this);
		super.onSpawn();	
	}
	
	@Override
	public void onDecay()
	{
		if (isDecayed())
			return;
		
		setDecayed(true);
		
		// reset champion status if the thing is a mob
		setChampion(false);

		// Remove the L2NpcInstance from the world when the decay task is launched
		super.onDecay();
		
		// Decrease its spawn counter
		if (_spawn != null)
			_spawn.decreaseCount(this);
	}
	
	@Override
	public void deleteMe()
	{
		ZoneManager.getInstance().getRegion(this).removeFromZones(this);
		decayMe();
		super.deleteMe();
	}
	
	public L2Spawn getSpawn()
	{
		return _spawn;
	}
	
	@Override
	public String toString()
	{
		return getTemplate().name;
	}
	
	public boolean isDecayed()
	{
		return _isDecayed;
	}
	
	public void setDecayed(boolean decayed)
	{
		_isDecayed = decayed;
	}
	
	public void endDecayTask()
	{
		if (!isDecayed())
		{
			DecayTaskManager.getInstance().cancelDecayTask(this);
			onDecay();
		}
	}
	
	public boolean isMob()
	{
		return false; 
	}
	
	// Two functions to change the appearance of the equipped weapons on the NPC
	// This is only useful for a few NPCs and is most likely going to be called from AI
	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
	}
	
	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
	}
	
	public void setCollisionHeight(int height)
	{
		_currentCollisionHeight = height;
	}
	
	public void setCollisionRadius(int radius)
	{
		_currentCollisionRadius = radius;
	}
	
	public int getCollisionHeight()
	{
		return _currentCollisionHeight;
	}
	
	public int getCollisionRadius()
	{
		return _currentCollisionRadius;
	}

	public L2Npc scheduleDespawn(long delay)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(this.new DespawnTask(this), delay);
		return this;
	}

	public class DespawnTask implements Runnable
	{
		L2Npc _npc;
		
		public DespawnTask(L2Npc npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			if (_npc != null)
				_npc.deleteMe();
		}
	}
	
	public int getScriptValue()
	{
		return _scriptValue;
	}
	
	public void setScriptValue(int val)
	{
		_scriptValue = val;
	}
	
	public boolean isScriptValue(int val)
	{
		return _scriptValue == val;
	}
	
	public boolean isWalker()
	{
		return this instanceof L2NpcWalkerInstance;
	}
	
	public void broadcastNpcSay(String message)
	{
		if(getNpcId() == 70016)
			broadcastPacket(new CreatureSay(getObjectId(), ChatType.GENERAL.getClientId(), getName(), message), 1250);
		else
		    broadcastPacket(new NpcSay(getObjectId(), ChatType.GENERAL.getClientId(), getNpcId(), message), 1250);
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (getMoveSpeed() == 0)
			activeChar.sendPacket(new ServerObjectInfo(this, activeChar));
		else
			activeChar.sendPacket(new NpcInfo(this, activeChar));
	}
}
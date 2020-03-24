package com.l2jhellas.gameserver.model.quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.holder.SkillHolder;
import com.l2jhellas.gameserver.instancemanager.QuestManager;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.QuestList;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class Quest
{
	public static final Logger _log = Logger.getLogger(Quest.class.getName());
	
	private static final String LOAD_QUEST_STATES = "SELECT name,value FROM character_quests WHERE char_id=? AND var='<state>'";
	private static final String LOAD_QUEST_VARIABLES = "SELECT name,var,value FROM character_quests WHERE char_id=? AND var<>'<state>'";
	private static final String DELETE_INVALID_QUEST = "DELETE FROM character_quests WHERE name=?";
	
	private static final String HTML_NONE_AVAILABLE = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
	private static final String HTML_ALREADY_COMPLETED = "<html><body>This quest has already been completed.</body></html>";
	private static final String HTML_TOO_MUCH_QUESTS = "<html><body>You have already accepted the maximum number of quests. No more than 25 quests may be undertaken simultaneously.<br>For quest information, enter Alt+U.</body></html>";
	
	public static final byte STATE_CREATED = 0;
	public static final byte STATE_STARTED = 1;
	public static final byte STATE_COMPLETED = 2;
	
	private final Map<Integer, List<QuestTimer>> _eventTimers = new ConcurrentHashMap<>();
	
	private final int _id;
	private final String _name;
	private final String _descr;
	private boolean _onEnterWorld;
	private int[] _itemsIds;
	
	public Quest(int questId, String name, String descr)
	{
		_id = questId;
		_name = name;
		_descr = descr;
		_onEnterWorld = false;
		QuestManager.getInstance().addQuest(this);
	}
	
	public int getQuestId()
	{
		return _id;
	}
	
	public boolean isRealQuest()
	{
		return _id > 0;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String getDescr()
	{
		return _descr;
	}
	
	public void setOnEnterWorld(boolean val)
	{
		_onEnterWorld = val;
	}
	
	public boolean getOnEnterWorld()
	{
		return _onEnterWorld;
	}
	
	public int[] getItemsIds()
	{
		return _itemsIds;
	}
	
	public void setItemsIds(int... itemIds)
	{
		_itemsIds = itemIds;
	}
	
	public QuestState newQuestState(L2PcInstance player)
	{
		return new QuestState(player, this, STATE_CREATED);
	}
	
	public final static void playerEnter(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement invalidQuest = con.prepareStatement(DELETE_INVALID_QUEST);
			
			PreparedStatement statement = con.prepareStatement(LOAD_QUEST_STATES);
			statement.setInt(1, player.getObjectId());
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				String questId = rs.getString("name");
				
				Quest q = QuestManager.getInstance().getQuest(questId);
				if (q == null)
				{
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuest.setString(1, questId);
						invalidQuest.executeUpdate();
					}
					
					_log.finer("Unknown  quest " + questId + " for player " + player.getName());
					continue;
				}
				
				new QuestState(player, q, rs.getByte("value"));
			}
			
			rs.close();
			statement.close();
			
			statement = con.prepareStatement(LOAD_QUEST_VARIABLES);
			statement.setInt(1, player.getObjectId());
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				String questId = rs.getString("name");
				
				QuestState qs = player.getQuestState(questId);
				if (qs == null)
				{
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuest.setString(1, questId);
						invalidQuest.executeUpdate();
					}
					
					_log.finer("Unknown quest " + questId + " for player " + player.getName());
					continue;
				}
				
				qs.setInternal(rs.getString("var"), rs.getString("value"));
			}
			
			rs.close();
			statement.close();
			invalidQuest.close();
		}
		catch (Exception e)
		{
			_log.warning(Quest.class.getSimpleName() + ": could not insert char quest:");
		}
		
		// events
		for (Quest q : QuestManager.getInstance().getAllManagedScripts())
		{
			player.processQuestEvent(q.getName(), "enter");
		}
		
		player.sendPacket(new QuestList(player));
	}
	
	public L2PcInstance getRandomPartyMember(L2PcInstance player, L2Object object)
	{
		// No valid player instance is passed, there is nothing to check.
		if (player == null)
			return null;
		
		// No party or no object, return player.
		if (object == null || !player.isInParty())
			return player;
		
		// Player's party.
		List<L2PcInstance> members = new ArrayList<>();
		for (L2PcInstance member : player.getParty().getPartyMembers())
		{
			if (member.isInsideRadius(object, Config.ALT_PARTY_RANGE, true, false))
				members.add(member);
		}
		
		// No party members, return. (note: player is party member too, in most cases he is included in members too)
		if (members.isEmpty())
			return null;
		
		// Random party member.
		return members.get(Rnd.get(members.size()));
	}
	
	public QuestState checkPlayerCondition(L2PcInstance player, L2Npc npc, String var, String value)
	{
		// No valid player instance is passed, there is nothing to check.
		if (player == null)
			return null;
		
		// Check player's quest conditions.
		QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;
		
		// Condition exists? Condition has correct value?
		if (st.get(var) == null || !value.equalsIgnoreCase(st.get(var)))
			return null;
		
		// Invalid npc instance?
		if (npc == null)
			return null;
		
		// Player is in range?
		if (!player.isInsideRadius(npc, Config.ALT_PARTY_RANGE, true, false))
			return null;
		
		return st;
	}
	
	public List<L2PcInstance> getPartyMembers(L2PcInstance player, L2Npc npc, String var, String value)
	{
		// Output list.
		List<L2PcInstance> candidates = new ArrayList<>();
		
		// Valid player instance is passed and player is in a party? Check party.
		if (player != null && player.isInParty())
		{
			// Filter candidates from player's party.
			for (L2PcInstance partyMember : player.getParty().getPartyMembers())
			{
				if (partyMember == null)
					continue;
				
				// Check party members' quest condition.
				if (checkPlayerCondition(partyMember, npc, var, value) != null)
					candidates.add(partyMember);
			}
		}
		// Player is solo, check the player
		else if (checkPlayerCondition(player, npc, var, value) != null)
			candidates.add(player);
		
		return candidates;
	}
	
	public L2PcInstance getRandomPartyMember(L2PcInstance player, L2Npc npc, String var, String value)
	{
		// No valid player instance is passed, there is nothing to check.
		if (player == null)
			return null;
		
		// Get all candidates fulfilling the condition.
		final List<L2PcInstance> candidates = getPartyMembers(player, npc, var, value);
		
		// No candidate, return.
		if (candidates.isEmpty())
			return null;
		
		// Return random candidate.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	public L2PcInstance getRandomPartyMember(L2PcInstance player, L2Npc npc, String value)
	{
		return getRandomPartyMember(player, npc, "cond", value);
	}
	
	public QuestState checkPlayerState(L2PcInstance player, L2Npc npc, byte state)
	{
		// No valid player instance is passed, there is nothing to check.
		if (player == null)
			return null;
		
		// Check player's quest conditions.
		QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;
		
		// State correct?
		if (st.getState() != state)
			return null;
		
		// Invalid npc instance?
		if (npc == null)
			return null;
		
		// Player is in range?
		if (!player.isInsideRadius(npc, Config.ALT_PARTY_RANGE, true, false))
			return null;
		
		return st;
	}
	
	public static L2PcInstance getApprentice(L2PcInstance player)
	{
		final int apprenticeId = player.getApprentice();
		if (apprenticeId == 0)
			return null;
		
		final L2Clan clan = player.getClan();
		if (clan == null)
			return null;
		
		final L2ClanMember member = clan.getClanMember(apprenticeId);
		if (member != null && member.isOnline())
		{
			final L2PcInstance apprentice = member.getPlayerInstance();
			if (apprentice != null && player.isInsideRadius(apprentice, 1500, true, false))
				return apprentice;
		}
		
		return null;
	}
	
	public List<L2PcInstance> getPartyMembersState(L2PcInstance player, L2Npc npc, byte state)
	{
		// Output list.
		List<L2PcInstance> candidates = new ArrayList<>();
		
		// Valid player instance is passed and player is in a party? Check party.
		if (player != null && player.isInParty())
		{
			// Filter candidates from player's party.
			for (L2PcInstance partyMember : player.getParty().getPartyMembers())
			{
				if (partyMember == null)
					continue;
				
				// Check party members' quest state.
				if (checkPlayerState(partyMember, npc, state) != null)
					candidates.add(partyMember);
			}
		}
		// Player is solo, check the player
		else if (checkPlayerState(player, npc, state) != null)
			candidates.add(player);
		
		return candidates;
	}
	
	public L2PcInstance getRandomPartyMemberState(L2PcInstance player, L2Npc npc, byte state)
	{
		// No valid player instance is passed, there is nothing to check.
		if (player == null)
			return null;
		
		// Get all candidates fulfilling the condition.
		final List<L2PcInstance> candidates = getPartyMembersState(player, npc, state);
		
		// No candidate, return.
		if (candidates.isEmpty())
			return null;
		
		// Return random candidate.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	public QuestState getClanLeaderQuestState(L2PcInstance player, L2Npc npc)
	{
		// If player is the leader, retrieves directly the qS and bypass others checks
		if (player.isClanLeader() && player.isInsideRadius(npc, Config.ALT_PARTY_RANGE, true, false))
			return player.getQuestState(getName());
		
		// Verify if the player got a clan
		L2Clan clan = player.getClan();
		if (clan == null)
			return null;
		
		// Verify if the leader is online
		L2PcInstance leader = clan.getLeader().getPlayerInstance();
		if (leader == null)
			return null;
		
		// Verify if the player is on the radius of the leader. If true, send leader's quest state.
		if (leader.isInsideRadius(npc, Config.ALT_PARTY_RANGE, true, false))
			return leader.getQuestState(getName());
		
		return null;
	}
	
	public void startQuestTimer(String name, L2Npc npc, L2PcInstance player,long time)
	{
		startQuestTimer(name,time,npc,player,false);
	}
	
	public void startQuestTimer(String name, L2Npc npc, L2PcInstance player,long time,boolean repeating)
	{
		startQuestTimer(name,time,npc,player,repeating);
	}
	
	public void startQuestTimer(String name, long time, L2Npc npc, L2PcInstance player, boolean repeating)
	{
		// Get quest timers for this timer type.
		List<QuestTimer> timers = _eventTimers.get(name.hashCode());
		if (timers == null)
		{
			// None timer exists, create new list.
			timers = new CopyOnWriteArrayList<>();
			
			// Add new timer to the list.
			timers.add(new QuestTimer(this, name, npc, player, time, repeating));
			
			// Add timer list to the map.
			_eventTimers.put(name.hashCode(), timers);
		}
		else
		{
			// Check, if specific timer already exists.
			for (QuestTimer timer : timers)
			{
				// If so, return.
				if (timer != null && timer.equals(this, name, npc, player))
					return;
			}
			
			// Add new timer to the list.
			timers.add(new QuestTimer(this, name, npc, player, time, repeating));
		}
	}
	
	public QuestTimer getQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		// Get quest timers for this timer type.
		List<QuestTimer> timers = _eventTimers.get(name.hashCode());
		
		// Timer list does not exists or is empty, return.
		if (timers == null || timers.isEmpty())
			return null;
		
		// Check, if specific timer exists.
		for (QuestTimer timer : timers)
		{
			// If so, return him.
			if (timer != null && timer.equals(this, name, npc, player))
				return timer;
		}
		return null;
	}
	
	public void cancelQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		// If specified timer exists, cancel him.
		QuestTimer timer = getQuestTimer(name, npc, player);
		if (timer != null)
			timer.cancel();
	}
	
	public void cancelQuestTimers(String name)
	{
		// Get quest timers for this timer type.
		List<QuestTimer> timers = _eventTimers.get(name.hashCode());
		
		// Timer list does not exists or is empty, return.
		if (timers == null || timers.isEmpty())
			return;
		
		// Cancel all quest timers.
		for (QuestTimer timer : timers)
		{
			if (timer != null)
				timer.cancel();
		}
	}
	
	// Note, keep it default. It is used withing QuestTimer, when it terminates.
	
	void removeQuestTimer(QuestTimer timer)
	{
		// Timer does not exist, return.
		if (timer == null)
			return;
		
		// Get quest timers for this timer type.
		List<QuestTimer> timers = _eventTimers.get(timer.getName().hashCode());
		
		// Timer list does not exists or is empty, return.
		if (timers == null || timers.isEmpty())
			return;
		
		// Remove timer from the list.
		timers.remove(timer);
	}
	
	public L2Npc addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(),false, 0, false);
	}
	
	public L2Npc addSpawn(int npcId, L2Character cha, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, despawnDelay, isSummonSpawn);
	}

	public L2Npc addSpawn(int npcId, Location loc, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), randomOffset, despawnDelay, isSummonSpawn);
	}

	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false);
	}
	
	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		L2Npc result = null;
		try
		{
			final L2NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
			if (template != null)
			{
				
				if (randomOffset)
				{
					x += Rnd.get(-100, 100);
					y += Rnd.get(-100, 100);
				}
				
				final L2Spawn spawn = new L2Spawn(template);
				spawn.setHeading(heading);
				spawn.setLocx(x);
				spawn.setLocy(y);
				spawn.setLocz(z + 20);
				spawn.stopRespawn();
				result = spawn.doSpawn();
				
				if (despawnDelay > 0)
					result.scheduleDespawn(despawnDelay);
				
				return result;
			}
		}
		catch (Exception e1)
		{
			_log.warning(Quest.class.getName() + ": Could not spawn Npc " + npcId);
		}
		
		return null;
	}
	
	public static String getNoQuestMsg()
	{
		return HTML_NONE_AVAILABLE;
	}
	
	public static String getAlreadyCompletedMsg()
	{
		return HTML_ALREADY_COMPLETED;
	}
	
	public static String getTooMuchQuestsMsg()
	{
		return HTML_TOO_MUCH_QUESTS;
	}
	
	public boolean showResult(L2Npc npc, L2PcInstance player, String result)
	{
		if (player == null || result == null || result.isEmpty())
			return false;
		
		if (result.endsWith(".htm") || result.endsWith(".html"))
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(npc == null ? 0 : npc.getNpcId());
			if (isRealQuest())
				npcReply.setFile("./data/html/scripts/quests/" + getName() + "/" + result);
			else
				npcReply.setFile("./data/html/scripts/" + getDescr() + "/" + getName() + "/" + result);
			
			if (npc != null)
				npcReply.replace("%objectId%", npc.getName());
			
			player.sendPacket(npcReply);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (result.startsWith("<html>"))
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(npc == null ? 0 : npc.getNpcId());
			npcReply.setHtml(result);
			
			if (npc != null)
				npcReply.replace("%objectId%", npc.getName());
			
			player.sendPacket(npcReply);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
			player.sendMessage(result);
		
		return true;
	}
	
	public boolean giveItemRandomly(L2PcInstance player, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, null, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}

	public boolean giveItemRandomly(L2PcInstance player, L2Npc npc, int itemId, long amountToGive, long limit, double dropChance, boolean playSound)
	{
		return giveItemRandomly(player, npc, itemId, amountToGive, amountToGive, limit, dropChance, playSound);
	}

	public boolean giveItemRandomly(L2PcInstance player, L2Npc npc, int itemId, long minAmount, long maxAmount, long limit, double dropChance, boolean playSound)
	{            
		final QuestState st = player.getQuestState(getName());
		
		if (st == null)
			return false;
		
		final long currentCount = st.getQuestItemsCount(itemId);
		
		if ((limit > 0) && (currentCount >= limit))
			return true;
		
		minAmount *= Config.RATE_QUEST_DROP;
		maxAmount *= Config.RATE_QUEST_DROP;
		dropChance *= Config.RATE_QUEST_DROP; 
		
		long amountToGive = (minAmount == maxAmount) ? minAmount : Rnd.get(minAmount, maxAmount);
		final double random = Rnd.nextDouble();
		if ((dropChance >= random) && (amountToGive > 0) && player.getInventory().validateCapacityByItemId(itemId))
		{
			if ((limit > 0) && ((currentCount + amountToGive) > limit))
				amountToGive = limit - currentCount;
			
			player.addItem("Quest", itemId, amountToGive, npc, true);
			
			if ((currentCount + amountToGive) == limit)
			{
				if (playSound)
					st.playSound(QuestState.SOUND_MIDDLE);

				return true;
			}
				
			if (playSound)
				st.playSound(QuestState.SOUND_ITEMGET);
			
			if (limit <= 0)
					return true;
		}
		return false;
	}
	
	public boolean showError(L2PcInstance player, Throwable e)
	{
		_log.warning(Quest.class.getName());
		
		if (e.getMessage() == null)
			e.printStackTrace();
		
		if (player != null && player.isGM())
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(0);
			npcReply.setHtml("<html><body><title>Script error</title>" + e.getMessage() + "</body></html>");
			player.sendPacket(npcReply);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		return false;
	}
	
	public String getHtmlText(String fileName)
	{
		if (isRealQuest())
			return HtmCache.getInstance().getHtmForce("./data/html/scripts/quests/" + getName() + "/" + fileName);
		
		return HtmCache.getInstance().getHtmForce("./data/html/scripts/" + getDescr() + "/" + getName() + "/" + fileName);
	}
	
	public void addEventId(int npcId, QuestEventType eventType)
	{
		try
		{
			final L2NpcTemplate t = NpcData.getInstance().getTemplate(npcId);
			if (t != null)
				t.addQuestEvent(eventType, this);
		}
		catch (Exception e)
		{
			_log.warning(Quest.class.getSimpleName() + ": Exception on addEventId(): " + e.getMessage());
		}
	}
	
	public void addStartNpc(int... npcIds)
	{
		for (int npcId : npcIds)
			addEventId(npcId, QuestEventType.QUEST_START);
	}
	
	public void addAttackId(int... npcIds)
	{
		for (int npcId : npcIds)
			addEventId(npcId, QuestEventType.ON_ATTACK);
	}
	
	public final boolean notifyAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		String res = null;
		try
		{
			res = onAttack(npc, attacker, damage, isPet);
		}
		catch (Exception e)
		{
			return showError(attacker, e);
		}
		return showResult(npc, attacker, res);
	}
	
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		return null;
	}
	
	public void addAttackActId(int... npcIds)
	{
		for (int npcId : npcIds)
			addEventId(npcId, QuestEventType.ON_ATTACK_ACT);
	}
	
	public final boolean notifyAttackAct(L2Npc npc, L2PcInstance victim)
	{
		String res = null;
		try
		{
			res = onAttackAct(npc, victim);
		}
		catch (Exception e)
		{
			return showError(victim, e);
		}
		return showResult(npc, victim, res);
	}
	
	public String onAttackAct(L2Npc npc, L2PcInstance victim)
	{
		return null;
	}
	
	public void addAggroRangeEnterId(int... npcIds)
	{
		for (int npcId : npcIds)
			addEventId(npcId, QuestEventType.ON_AGGRO_RANGE_ENTER);
	}
	
	private class TmpOnAggroEnter implements Runnable
	{
		private final L2Npc _npc;
		private final L2PcInstance _pc;
		private final boolean _isPet;
		
		public TmpOnAggroEnter(L2Npc npc, L2PcInstance pc, boolean isPet)
		{
			_npc = npc;
			_pc = pc;
			_isPet = isPet;
		}
		
		@Override
		public void run()
		{
			String res = null;
			try
			{
				res = onAggroRangeEnter(_npc, _pc, _isPet);
			}
			catch (Exception e)
			{
				showError(_pc, e);
			}
			showResult(_npc, _pc, res);
			
		}
	}
	
	public final boolean notifyAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		ThreadPoolManager.getInstance().executeAi(new TmpOnAggroEnter(npc, player, isPet));
		return true;
	}
	
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		return null;
	}
	
	public final boolean notifyAcquireSkill(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAcquireSkill(npc, player, skill);
			if (res == "true")
				return true;
			else if (res == "false")
				return false;
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(npc, player, res);
	}
	
	public String onAcquireSkill(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	public final boolean notifyAcquireSkillInfo(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAcquireSkillInfo(npc, player, skill);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(npc, player, res);
	}
	
	public String onAcquireSkillInfo(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	public final boolean notifyAcquireSkillList(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onAcquireSkillList(npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(npc, player, res);
	}
	
	public String onAcquireSkillList(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	public final boolean notifyDeath(L2Character killer, L2Character victim, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onDeath(killer, victim, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		if (killer instanceof L2Npc)
			return showResult((L2Npc) killer, player, res);
		
		return showResult(null, player, res);
	}
	
	public String onDeath(L2Character killer, L2PcInstance player)
	{
		if (killer instanceof L2Npc)
			return onAdvEvent("", (L2Npc) killer, player);
		
		return onAdvEvent("", null, player);
	}
	
	public String onDeath(L2Character killer, L2Character victim, L2PcInstance player)
	{
		if (killer instanceof L2Npc)
			return onAdvEvent("", (L2Npc) killer, player);
		
		return onAdvEvent("", null, player);
	}
	
	public final boolean notifyEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onAdvEvent(event, npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(npc, player, res);
	}
	
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		// if not overridden by a subclass, then default to the returned value of the simpler (and older) onEvent override
		// if the player has a state, use it as parameter in the next call, else return null
		if (player != null)
		{
			QuestState qs = player.getQuestState(getName());
			if (qs != null)
				return onEvent(event, qs);
		}
		return null;
	}
	
	public String onEvent(String event, QuestState qs)
	{
		return null;
	}
	
	public final boolean notifyEnterWorld(L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onEnterWorld(player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(null, player, res);
	}
	
	public String onEnterWorld(L2PcInstance player)
	{
		return null;
	}
	
	public void addEnterZoneId(int... zoneIds)
	{
		for (int zoneId : zoneIds)
		{
			final L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			if (zone != null)
				zone.addQuestEvent(QuestEventType.ON_ENTER_ZONE, this);
		}
	}
	
	public final boolean notifyEnterZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		String res = null;
		try
		{
			res = onEnterZone(character, zone);
		}
		catch (Exception e)
		{
			if (player != null)
				return showError(player, e);
		}
		if (player != null)
			return showResult(null, player, res);
		return true;
	}
	
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		return null;
	}
	
	public void addExitZoneId(int... zoneIds)
	{
		for (int zoneId : zoneIds)
		{
			final L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			if (zone != null)
				zone.addQuestEvent(QuestEventType.ON_EXIT_ZONE, this);
		}
	}
	
	public final boolean notifyExitZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		String res = null;
		try
		{
			res = onExitZone(character, zone);
		}
		catch (Exception e)
		{
			if (player != null)
				return showError(player, e);
		}
		if (player != null)
			return showResult(null, player, res);
		return true;
	}
	
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		return null;
	}
	
	public void addFactionCallId(int... npcIds)
	{
		for (int npcId : npcIds)
			addEventId(npcId, QuestEventType.ON_FACTION_CALL);
	}
	
	public final boolean notifyFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		String res = null;
		try
		{
			res = onFactionCall(npc, caller, attacker, isPet);
		}
		catch (Exception e)
		{
			return showError(attacker, e);
		}
		return showResult(npc, attacker, res);
	}
	
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		return null;
	}
	
	public void addFirstTalkId(int... npcIds)
	{
		for (int npcId : npcIds)
			addEventId(npcId, QuestEventType.ON_FIRST_TALK);
	}
	
	public final boolean notifyFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		
		// if the quest returns text to display, display it.
		if (res != null && res.length() > 0)
			return showResult(npc, player, res);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
		return true;
	}
	
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	public void addItemUse(int... itemIds)
	{
		for (int itemId : itemIds)
		{
			L2Item t = ItemTable.getInstance().getTemplate(itemId);
			if (t != null)
				t.addQuestEvent(this);
		}
	}
	
	public final boolean notifyItemUse(L2ItemInstance item, L2PcInstance player, L2Object target)
	{
		String res = null;
		try
		{
			res = onItemUse(item, player, target);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(null, player, res);
	}
	
	public String onItemUse(L2ItemInstance item, L2PcInstance player, L2Object target)
	{
		return null;
	}
	
	public void addKillId(Set<Integer> set)
	{
		for (int killId : set)
			addEventId(killId, QuestEventType.ON_KILL);
	}
	
	public void addKillId(int... killIds)
	{
		for (int killId : killIds)
			addEventId(killId, QuestEventType.ON_KILL);
	}
	
	public final boolean notifyKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		String res = null;
		try
		{
			res = onKill(npc, killer, isPet);
		}
		catch (Exception e)
		{
			return showError(killer, e);
		}
		return showResult(npc, killer, res);
	}
	
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		return null;
	}
	
	public void addSpawnId(int... npcIds)
	{
		for (int npcId : npcIds)
			addEventId(npcId, QuestEventType.ON_SPAWN);
	}
	
	public final boolean notifySpawn(L2Npc npc)
	{
		try
		{
			onSpawn(npc);
		}
		catch (Exception e)
		{
			_log.warning(Quest.class.getSimpleName() + ": Exception on onSpawn() in notifySpawn(): " + e.getMessage());
			return true;
		}
		return false;
	}
	
	public String onSpawn(L2Npc npc)
	{
		return null;
	}
	
	public void addSkillSeeId(int... npcIds)
	{
		for (int npcId : npcIds)
			addEventId(npcId, QuestEventType.ON_SKILL_SEE);
	}
	
	protected void castSkill(L2Npc npc, L2Playable target, SkillHolder skill)
	{
		npc.setTarget(target);
		npc.doCast(skill.getSkill());
	}

	protected void castSkill(L2Npc npc, L2Playable target, L2Skill skill)
	{
		npc.setTarget(target);
		npc.doCast(skill);
	}
	
	public class TmpOnSkillSee implements Runnable
	{
		private final L2Npc _npc;
		private final L2PcInstance _caster;
		private final L2Skill _skill;
		private final L2Object[] _targets;
		private final boolean _isPet;
		
		public TmpOnSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
		{
			_npc = npc;
			_caster = caster;
			_skill = skill;
			_targets = targets;
			_isPet = isPet;
		}
		
		@Override
		public void run()
		{
			String res = null;
			try
			{
				res = onSkillSee(_npc, _caster, _skill, _targets, _isPet);
			}
			catch (Exception e)
			{
				showError(_caster, e);
			}
			showResult(_npc, _caster, res);
			
		}
	}
	
	public final boolean notifySkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		ThreadPoolManager.getInstance().executeAi(new TmpOnSkillSee(npc, caster, skill, targets, isPet));
		return true;
	}
	
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		return null;
	}
	
	public void addSpellFinishedId(int... npcIds)
	{
		for (int npcId : npcIds)
			addEventId(npcId, QuestEventType.ON_SPELL_FINISHED);
	}
	
	public final boolean notifySpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onSpellFinished(npc, player, skill);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		return showResult(npc, player, res);
	}
	
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	public void addTalkId(int... talkIds)
	{
		for (int talkId : talkIds)
			addEventId(talkId, QuestEventType.ON_TALK);
	}
	
	public final boolean notifyTalk(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onTalk(npc, player);
		}
		catch (Exception e)
		{
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(npc, player, res);
	}
	
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		return null;
	}
	
	private void setQuestToOfflineMembers(Integer[] objectsId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement stm = con.prepareStatement("INSERT INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)");
			
			for (Integer charId : objectsId)
			{
				stm.setInt(1, charId.intValue());
				stm.setString(2, getName());
				stm.setString(3, "<state>");
				stm.setString(4, "1");			
				stm.executeUpdate();
			}
			
			stm.close();
			con.close();
		}
		catch (Exception e)
		{
			_log.info("Error in updating character_quest table from Quest.java on method setQuestToOfflineMembers");
			_log.info(e.toString());
		}
	}
	
	private void deleteQuestToOfflineMembers(int clanId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement stm = con.prepareStatement("DELETE FROM character_quests WHERE name = ? and char_id IN (SELECT obj_Id FROM characters WHERE clanid = ? AND online = 0)");	
			stm.setString(1, getName());
			stm.setInt(2, clanId);		
			stm.executeUpdate();		
			stm.close();
			con.close();
		}
		catch (Exception e)
		{
			_log.info("Error in deleting infos from character_quest table from Quest.java on method deleteQuestToOfflineMembers");
			_log.info(e.toString());
		}
	}
	
	public void setQuestToClanMembers(L2PcInstance player)
	{
		if (player.isClanLeader())
		{
			L2PcInstance[] onlineMembers = player.getClan().getOnlineMembers();
			Integer[] offlineMembersIds = player.getClan().getOfflineMembersIds();
			
			for (L2PcInstance onlineMember : onlineMembers)
			{
				if (!onlineMember.isClanLeader())
					onlineMember.setQuestState(player.getQuestState(getName()));
			}
			
			setQuestToOfflineMembers(offlineMembersIds);
		}
	}

	public void finishQuestToClan(L2PcInstance player)
	{
		if (player.isClanLeader())
		{
			L2PcInstance[] onlineMembers = player.getClan().getOnlineMembers();
			
			for (L2PcInstance onlineMember : onlineMembers)
			{
				if (!onlineMember.isClanLeader())
					onlineMember.delQuestState(onlineMember.getQuestState(getName()));
			}
			
			deleteQuestToOfflineMembers(player.getClanId());
		}
	}
	
	@Override
	public boolean equals(Object o)
	{
		Quest q = (Quest) o;
		
		if (_id != q._id)
			return false;
		
		if (!_name.equals(q._name))
			return false;
		
		return true;
	}
	
}
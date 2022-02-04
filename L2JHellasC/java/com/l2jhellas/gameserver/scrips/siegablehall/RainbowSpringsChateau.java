package com.l2jhellas.gameserver.scrips.siegablehall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable.TeleportWhereType;
import com.l2jhellas.gameserver.instancemanager.ClanHallSiegeManager;
import com.l2jhellas.gameserver.instancemanager.CustomSpawnManager;
import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.spawn.L2Spawn;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.scrips.siegable.ClanHallSiegeEngine;
import com.l2jhellas.gameserver.scrips.siegable.SiegableHall;
import com.l2jhellas.gameserver.scrips.siegable.SiegeStatus;
import com.l2jhellas.util.Broadcast;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class RainbowSpringsChateau extends ClanHallSiegeEngine
{
	private static final int RAINBOW_SPRINGS = 62;
	
	private static final int WAR_DECREES = 8034;
	private static final int RAINBOW_NECTAR = 8030;
	private static final int RAINBOW_MWATER = 8031;
	private static final int RAINBOW_WATER = 8032;
	private static final int RAINBOW_SULFUR = 8033;
	
	private static final int MESSENGER = 35604;
	private static final int CARETAKER = 35603;
	private static final int CHEST = 35593;
	
    private static String ARENASPAWN[] = 
    {
    	"rainbow_springs_arena_0",
    	"rainbow_springs_arena_1",
    	"rainbow_springs_arena_2",
    	"rainbow_springs_arena_3"
    };
	
	private static final int[] GOURDS =
	{
		35588,
		35589,
		35590,
		35591
	};
	
	private static final int[] YETIS =
	{
		35596,
		35597,
		35598,
		35599
	};
	
	private static final Location[] ARENAS = new Location[]
	{
		new Location(151562, -127080, -2214), // Arena 1
		new Location(153141, -125335, -2214), // Arena 2
		new Location(153892, -127530, -2214), // Arena 3
		new Location(155657, -125752, -2214), // Arena 4
	};
	
	protected static final int[] ARENA_ZONES =
	{
		112081,
		112082,
		112083,
		112084
	};
	
	private static final String[] TEXT_PASSAGES =
	{
		"Fight for Rainbow Springs!",
		"Are you a match for the Yetti?",
		"Did somebody order a knuckle sandwich?"
	};
	
	private static final L2Skill[] DEBUFFS = {};
	
	private final Map<Integer, Integer> _warDecreesCount = new HashMap<>();
	private final List<L2Clan> _acceptedClans = new ArrayList<>(4);
	private final Map<String, ArrayList<L2Clan>> _usedTextPassages = new HashMap<>();
	private final Map<L2Clan, Integer> _pendingItemToGet = new ConcurrentHashMap<>();
	
	private SiegableHall _rainbow;
	private ScheduledFuture<?> _nextSiege;
	private ScheduledFuture<?> _siegeEnd;
	private String _registrationEnds;
	
	private final List<L2Spawn> _gourds = new ArrayList<>(4);
	
	public L2Clan _winner;
	
	public RainbowSpringsChateau()
	{
		super("RainbowSpringsChateau" , "siegablehall", RAINBOW_SPRINGS);
			
		_rainbow = ClanHallSiegeManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
		
		if (_rainbow == null)
			return;
		
		addFirstTalkId(MESSENGER, CARETAKER);
		addTalkId(MESSENGER, CARETAKER);
		addFirstTalkId(YETIS);
		addTalkId(YETIS);				 		 
		addItemUse(RAINBOW_NECTAR,RAINBOW_MWATER,RAINBOW_WATER,RAINBOW_SULFUR);
		
		loadAttackers();
		
		long delay = _rainbow.getNextSiegeTime();
		if (delay > -1)
		{
			setRegistrationEndString(delay - 3600000);
			
			_nextSiege = ThreadPoolManager.getInstance().scheduleGeneral(new SetFinalAttackers(), delay);
		}
		else
			_log.warning("No date was set for Rainbow Springs Chateau siege. Siege is canceled.");
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String html = "";
		
		final int npcId = npc.getNpcId();
		if (npcId == MESSENGER)
		{
			final String main = (_rainbow.getOwnerId() > 0) ? "messenger_yetti001.htm" : "messenger_yetti001a.htm";
			html = HtmCache.getInstance().getHtm("data/html/script/siegablehall/RainbowSpringsChateau/" + main);
			html = html.replace("%time%", _registrationEnds);
			
			if (_rainbow.getOwnerId() > 0)
				html = html.replace("%owner%", ClanTable.getInstance().getClan(_rainbow.getOwnerId()).getName());
		}
		else if (npcId == CARETAKER)
			html = (_rainbow.isInSiege()) ? "game_manager003.htm" : "game_manager001.htm";
		else if (Util.contains(YETIS, npcId))
		{
			if (_rainbow.isInSiege())
			{
				if (!player.isClanLeader())
					html = "no_L2Clan_leader.htm";
				else
				{
					final L2Clan L2Clan = player.getClan();
					if (_acceptedClans.contains(L2Clan))
					{
						int index = _acceptedClans.indexOf(L2Clan);
						if (npcId == YETIS[index])
							html = "yeti_main.htm";
					}
				}
			}
		}
		
		return html;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String html = event;
		
		final L2Clan clan = player.getClan();
		
		switch (npc.getNpcId())
		{
			case MESSENGER:
				switch (event)
				{
					case "register":
						if (!player.isClanLeader())
							html = "messenger_yetti010.htm";
						else if (clan.hasCastle() > 0 || clan.hasHideout() > 0)
							html = "messenger_yetti012.htm";
						else if (!_rainbow.isRegistering())
							html = "messenger_yetti014.htm";
						else if (_warDecreesCount.containsKey(clan.getClanId()))
							html = "messenger_yetti013.htm";
						else if (clan.getLevel() < 3 || clan.getMembersCount() < 5)
							html = "messenger_yetti011.htm";
						else
						{
							final L2ItemInstance warDecrees = player.getInventory().getItemByItemId(WAR_DECREES);
							if (warDecrees == null)
								html = "messenger_yetti008.htm";
							else
							{
								int count = warDecrees.getCount();
								
								_warDecreesCount.put(clan.getClanId(), count);
								
								player.destroyItem("Rainbow Springs Registration", warDecrees, npc, true);
								
								addAttacker(clan.getClanId(), count);
								
								html = "messenger_yetti009.htm";
							}
						}
						break;
					
					case "cancel":
						if (!player.isClanLeader())
							html = "messenger_yetti010.htm";
						else if (!_warDecreesCount.containsKey(clan.getClanId()))
							html = "messenger_yetti016.htm";
						else if (!_rainbow.isRegistering())
							html = "messenger_yetti017.htm";
						else
						{
							removeAttacker(clan.getClanId());
							html = "messenger_yetti018.htm";
						}
						break;
					
					case "unregister":
						if (_rainbow.isRegistering())
						{
							if (_warDecreesCount.containsKey(clan.getClanId()))
							{
								player.addItem("Rainbow Spring unregister", WAR_DECREES, _warDecreesCount.get(clan.getClanId()) / 2, npc, true);
								_warDecreesCount.remove(clan.getClanId());
								html = "messenger_yetti019.htm";
							}
							else
								html = "messenger_yetti020.htm";
						}
						else if (_rainbow.isWaitingBattle())
						{
							_acceptedClans.remove(clan);
							
							html = "messenger_yetti020.htm";
						}
						break;
				}
				break;
			
			case CARETAKER:
				if (event.equals("portToArena"))
				{
					final L2Party party = player.getParty();
					if (clan == null)
						html = "game_manager009.htm";
					else if (!player.isClanLeader())
						html = "game_manager004.htm";
					else if (!player.isInParty())
						html = "game_manager005.htm";
					else if (party.getPartyLeaderOID() != player.getObjectId())
						html = "game_manager006.htm";
					else
					{
						final int clanId = player.getClanId();
						boolean nonClanMemberInParty = false;
						for (L2PcInstance member : party.getPartyMembers())
						{
							if (member.getClanId() != clanId)
							{
								nonClanMemberInParty = true;
								break;
							}
						}
						
						if (nonClanMemberInParty)
							html = "game_manager007.htm";
						else if (party.getMemberCount() < 5)
							html = "game_manager008.htm";
						else if (clan.hasCastle() > 0 || clan.hasHideout() > 0)
							html = "game_manager010.htm";
						else if (clan.getLevel() < SiegeManager.getInstance().getSiegeClanMinLevel())
							html = "game_manager011.htm";
						else if (!_acceptedClans.contains(clan))
							html = "game_manager014.htm";
						else
							portToArena(player, _acceptedClans.indexOf(clan));
					}
				}
				break;
		}
		
		if (event.startsWith("enterText"))
		{
			if (!_acceptedClans.contains(clan))
				return null;
			
			String[] split = event.split("_ ");
			if (split.length < 2)
				return null;
			
			final String passage = split[1];
			if (!isValidPassage(passage))
				return null;
			
			if (_usedTextPassages.containsKey(passage))
			{
				ArrayList<L2Clan> list = _usedTextPassages.get(passage);
				
				if (list.contains(clan))
					html = "yeti_passage_used.htm";
				else
				{
					list.add(clan);
					
					if (_pendingItemToGet.containsKey(clan))
					{
						int left = _pendingItemToGet.get(clan);
						++left;
						_pendingItemToGet.put(clan, left);
					}
					else
					{
						_pendingItemToGet.put(clan, 1);
					}
					
					html = "yeti_item_exchange.htm";
				}
			}
		}
		// TODO(Zoey76): Rewrite this to prevent exploits...
		// else if (event.startsWith("getItem"))
		// {
		// if (!_pendingItemToGet.containsKey(clan))
		// {
		// html = "yeti_cannot_exchange.htm";
		// }
		//
		// int left = _pendingItemToGet.get(clan);
		// if (left > 0)
		// {
		// int itemId = Integer.parseInt(event.split("_")[1]);
		// player.addItem("Rainbow Spring Chateau Siege", itemId, 1, npc, true);
		// --left;
		// _pendingItemToGet.put(clan, left);
		// html = "yeti_main.htm";
		// }
		// else
		// {
		// html = "yeti_cannot_exchange.htm";
		// }
		// }
		
		return html;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (!_rainbow.isInSiege() || !(killer instanceof L2Playable))
			return null;
		
		final L2Clan clan = killer.getActingPlayer().getClan();
		if (clan == null || !_acceptedClans.contains(clan))
			return null;
		
		final int npcId = npc.getNpcId();
		final int index = _acceptedClans.indexOf(clan);
		
		if (npcId == CHEST)
		{
			shoutRandomText(npc);
		}
		else if (npcId == GOURDS[index])
		{
			if (_siegeEnd != null)
			{
				_siegeEnd.cancel(false);
				_siegeEnd = null;
			}
			ThreadPoolManager.getInstance().executeTask(new SiegeEnd(clan));
		}
		return null;
	}
	
	@Override
	public String onItemUse(L2ItemInstance item, L2PcInstance player, L2Object target)
	{
		if (!_rainbow.isInSiege())
			return null;
		
		if (!(target instanceof L2Npc))
			return null;
		
		final int npcId = ((L2Npc) target).getNpcId();
		if (!Util.contains(YETIS, npcId))
			return null;
		
		final L2Clan Clan = player.getClan();
		if (Clan == null || !_acceptedClans.contains(Clan))
			return null;

		final int itemId = item.getItemId();
		final int index = _acceptedClans.indexOf(Clan);

		if (itemId == RAINBOW_NECTAR)
		{
			final L2Spawn gourd = _gourds.stream().filter(sp -> sp.getLastSpawn() != null && sp.getLastSpawn().getNpcId() == GOURDS[index]).findFirst().orElse(null);
			if (gourd != null)
				gourd.getLastSpawn().reduceCurrentHp(1000, player);
		}
		else if (itemId == RAINBOW_MWATER)
		{
			final L2Spawn gourd = _gourds.stream().filter(sp -> sp.getLastSpawn() != null && sp.getLastSpawn().getNpcId() == GOURDS[index]).findFirst().orElse(null);
			
			final L2Npc gourdNpc = gourd.getLastSpawn();
			if (gourdNpc != null)
				gourdNpc.setCurrentHp(gourdNpc.getCurrentHp() + 1000);
		}
		else if (itemId == RAINBOW_WATER)
		{
			int iterator = _acceptedClans.size();
			AtomicInteger ati = new AtomicInteger();
			for (int i = 0; i < iterator; i++)
			{
				ati.set(i);
				final L2Spawn oldSpawn = _gourds.stream().filter(sp -> sp.getLastSpawn() != null && sp.getLastSpawn().getNpcId() == GOURDS[(iterator - 1) - ati.get()]).findFirst().orElse(null);
				final L2Spawn curSpawn = _gourds.stream().filter(sp -> sp.getLastSpawn() != null && sp.getLastSpawn().getNpcId() == GOURDS[ati.get()]).findFirst().orElse(null);

				if (curSpawn != null && oldSpawn != null)
					curSpawn.getLastSpawn().teleToLocation(oldSpawn.getLocx(),oldSpawn.getLocy(),oldSpawn.getLocz(),false);			
			}			
			ati.set(0);			
		}
		else if (itemId == RAINBOW_SULFUR)
		{
			for (int id : ARENA_ZONES)
			{
				if (id == _acceptedClans.indexOf(Clan))
					continue;
				
				final L2ZoneType zone = ZoneManager.getInstance().getZoneById(id);
				for (L2Character creature : zone.getCharactersInside())
				{
					for (L2Skill sk : DEBUFFS)
						sk.getEffects(creature, creature);
				}
			}
		}
		return null;
	}
	
	@Override
	public L2Clan getWinner()
	{
		return _winner;
	}
	
	private static void portToArena(L2PcInstance leader, int arena)
	{
		if (arena < 0 || arena > 3)
			return;
		
		for (L2PcInstance player : leader.getParty().getPartyMembers())
		{
			player.stopAllEffects();
			
			if (player.getPet() != null)
				player.getPet().unSummon(player);
			
			player.teleToLocation(ARENAS[arena],false);
		}
	}
	
	protected void spawnGourds()
	{
		if (_gourds.isEmpty())
		{
			for (int i = 0; i < _acceptedClans.size(); i++)
			{
				CustomSpawnManager.getInstance().getSpawnsByEventName(ARENASPAWN[i]).forEach(sp ->
				{						
					_gourds.add(sp.doSpawn().getSpawn());
				});
			}
		}
	}
	
	private void shoutRandomText(L2Npc npc)
	{
		int length = TEXT_PASSAGES.length;
		
		if (_usedTextPassages.size() >= length)
			return;
		
		String message = Rnd.get(TEXT_PASSAGES);
		
		if (_usedTextPassages.containsKey(message))
			shoutRandomText(npc);
		else
		{
			_usedTextPassages.put(message, new ArrayList<>());
			
			npc.broadcastNpcShout(message);
		}
	}
	
	private static boolean isValidPassage(String text)
	{
		for (String st : TEXT_PASSAGES)
		{
			if (st.equalsIgnoreCase(text))
				return true;
		}
		return false;
	}
	
	private static void removeAttacker(int L2ClanId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM rainbowsprings_attacker_list WHERE L2ClanId = ?"))
		{
			ps.setInt(1, L2ClanId);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warning("RainbowSpringsChateau:Couldn't remove attacker:" + e);
		}
	}
	
	private static void addAttacker(int L2ClanId, int count)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO rainbowsprings_attacker_list VALUES (?,?)"))
		{
			ps.setInt(1, L2ClanId);
			ps.setInt(2, count);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warning("RainbowSpringsChateau:Couldn't add attacker." + e);
		}
	}
	
	@Override
	public void loadAttackers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rset = s.executeQuery("SELECT * FROM rainbowsprings_attacker_list"))
		{
			while (rset.next())
				_warDecreesCount.put(rset.getInt("L2Clan_id"), rset.getInt("decrees_count"));
		}
		catch (Exception e)
		{
			_log.warning("RainbowSpringsChateau:Couldn't load attackers." + e);
		}
	}
	
	protected void setRegistrationEndString(long time)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(time));
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR);
		int mins = c.get(Calendar.MINUTE);
		
		_registrationEnds = year + "-" + month + "-" + day + " " + hour + (mins < 10 ? ":0" : ":") + mins;
	}
	
	public void launchSiege()
	{
		if (_nextSiege != null)
		{
			_nextSiege.cancel(false);
			_nextSiege = null;
		}
		ThreadPoolManager.getInstance().executeTask(new SiegeStart());
	}
	
	@Override
	public void endSiege()
	{
		if (_siegeEnd != null)
		{
			_siegeEnd.cancel(false);
			_siegeEnd = null;
		}
		ThreadPoolManager.getInstance().executeTask(new SiegeEnd(null));
	}
	
	protected class SetFinalAttackers implements Runnable
	{
		@Override
		public void run()
		{
			int spotLeft = 4;
			if (_rainbow.getOwnerId() > 0)
			{
				L2Clan owner = ClanTable.getInstance().getClan(_rainbow.getOwnerId());
				if (owner != null)
				{
					_rainbow.free();
					
					owner.setHasHideout(0);
					
					_acceptedClans.add(owner);
					
					--spotLeft;
				}
				
				for (int i = 0; i < spotLeft; i++)
				{
					long counter = 0;
					
					L2Clan Clan = null;
					for (Entry<Integer, Integer> entry : _warDecreesCount.entrySet())
					{
						final int L2ClanId = entry.getKey();
						
						L2Clan actingL2Clan = ClanTable.getInstance().getClan(L2ClanId);
						if (actingL2Clan == null || actingL2Clan.getDissolvingExpiryTime() > 0)
						{
							_warDecreesCount.remove(L2ClanId);
							continue;
						}
						
						final long count = entry.getValue();
						if (count > counter)
						{
							counter = count;
							Clan = actingL2Clan;
						}
					}
					
					if (Clan != null && _acceptedClans.size() < 4)
					{
						_acceptedClans.add(Clan);
						
						final L2PcInstance leader = Clan.getLeader().getPlayerInstance();
						if (leader != null)
							leader.sendMessage("Your Clan has been accepted to join the RainBow Srpings Chateau siege!");
					}
				}
				
				if (_acceptedClans.size() >= 2)
				{
					_nextSiege = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStart(), 3600000);
					_rainbow.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
				}
				else
					Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addString(_hall.getName()));
			}
		}
	}
	
	protected class SiegeStart implements Runnable
	{
		@Override
		public void run()
		{
			spawnGourds();
			
			_siegeEnd = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeEnd(null), _rainbow.getSiegeLength() - 120000);
		}
	}
	
	private class SiegeEnd implements Runnable
	{
		private final L2Clan _winner;
		
		protected SiegeEnd(L2Clan winner)
		{
			_winner = winner;
		}
		
		@Override
		public void run()
		{
			// Unspawn gourds.		
			if (!_gourds.isEmpty())
			{
				for (int i = 0; i < _acceptedClans.size(); i++)
					CustomSpawnManager.getInstance().despawnByEventName(ARENASPAWN[i]);
			}
			
			if (_winner != null)
				_rainbow.setOwner(_winner);
						
			ThreadPoolManager.getInstance().scheduleGeneral(new SetFinalAttackers(), _rainbow.getNextSiegeTime());
			setRegistrationEndString((_rainbow.getNextSiegeTime() + System.currentTimeMillis()) - 3600000);
			
			// Teleport out of the arenas is made 2 mins after game ends
			ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				for (int arenaId : ARENA_ZONES)
				{
					for (L2Character chr : ZoneManager.getInstance().getZoneById(arenaId).getCharactersInside())
						chr.teleToLocation(TeleportWhereType.TOWN);
				}
			}, 120000);
		}
	}
	
	public static void main(String[] args)
	{
		new RainbowSpringsChateau();
	}
}
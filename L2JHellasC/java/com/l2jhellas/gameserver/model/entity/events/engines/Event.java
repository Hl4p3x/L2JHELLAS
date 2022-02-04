package com.l2jhellas.gameserver.model.entity.events.engines;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.enums.Team;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.enums.player.PartyLootType;
import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.spawn.L2Spawn;
import com.l2jhellas.gameserver.model.spawn.SpawnData;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public abstract class Event
{
	protected int eventId;
	protected EventConfig config = EventConfig.getInstance();
	public Map<Integer, EventTeam> teams;
	protected ResurrectorTask resurrectorTask;
	public Clock clock;
	protected String scorebartext;
	protected int time;
	public int winnerTeam;
	protected int loserTeam;
	
	protected Map<L2PcInstance, int[]> players;
	
	protected class Clock implements Runnable
	{		
		public String getTime()
		{
			String mins = "" + time / 60;
			String secs = (time % 60 < 10 ? "0" + time % 60 : "" + time % 60);
			return mins + ":" + secs + "";
		}
		
		@Override
		public void run()
		{
			clockTick();
										
			if (time <= 0)
				schedule(1);
			else
			{
				time--;
				ThreadPoolManager.getInstance().scheduleGeneral(clock, 1000);
			}
		}
		
		public void setTime(int t)
		{
			time = t;
		}
		
		public void startClock(int mt)
		{
			time = mt;
			ThreadPoolManager.getInstance().scheduleGeneral(clock, 1);
		}
	}
	
	public Event()
	{
		teams = new ConcurrentHashMap<>();
		clock = new Clock();
		players = new ConcurrentHashMap<>();
		time = 0;
	}
	
	protected void clockTick()
	{
		
	}

	public void onHit(L2PcInstance actor, L2PcInstance target)
	{
		
	}
	
	public void useCapture(L2PcInstance player, L2Npc base)
	{
		
	}
	
	protected void addToResurrector(L2PcInstance player)
	{	
		ThreadPoolManager.getInstance().scheduleGeneral(new ResurrectorTask(player), 7000);
	}
	
	protected class ResurrectorTask implements Runnable
	{
		private L2PcInstance player;
		
		public ResurrectorTask(L2PcInstance p)
		{
			player = p;
		}
		
		@Override
		public void run()
		{
			if (EventManager.getInstance().isRegistered(player))
			{
				player.doRevive();	

				if (EventManager.getInstance().getBoolean("eventBufferEnabled")) 
					EventBuffer.getInstance().buffPlayer(player);

				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());
				teleportToTeamPos(player);
			}
		}
	}
		
	protected void announce(Set<L2PcInstance> list, String text)
	{
		for (L2PcInstance player : list)
			player.sendPacket(new CreatureSay(0, 18, "", "[Event] " + text));
	}

	protected void createNewTeam(int id, String name, int[] color, int[] startPos)
	{
		teams.put(id, new EventTeam(id, name, color, startPos));
	}
	
	public void createPartyOfTeam(int teamId)
	{
		int count = 0;
		L2Party party = null;
		
		List<L2PcInstance> list = new CopyOnWriteArrayList<>();
		
		getStreamedPlayerList().forEach(p ->
		{
			if (getTeam(p) == teamId)
				list.add(p);
		});

		for (L2PcInstance player : list)
		{
			if (count % 9 == 0 && list.size() - count != 1)
				party = new L2Party(player, PartyLootType.RANDOM);
			if (count % 9 < 9)
				player.joinParty(party);
			count++;
		}
	}
	
	public void divideIntoTeams(int number)
	{
		List<L2PcInstance> registeredplayers = EventManager.getInstance().players.stream().filter(Objects :: nonNull).filter(L2PcInstance::isOnline).collect(Collectors.toList());
		List<L2PcInstance> registeredhealers = registeredplayers.stream().filter(L2PcInstance :: isHealer).collect(Collectors.toList());

		AtomicInteger counter = new AtomicInteger(0);

		if(registeredplayers.size() > 0)
		{
			registeredplayers.stream().filter(L2PcInstance :: isNotHealer).forEach(player -> 
			{
				counter.incrementAndGet();

				players.put(player, new int[] {counter.get(), 0, 0 });
				EventManager.getInstance().players.remove(player);

				if (counter.get() == number)
					counter.set(0);
			});
		}
	
		counter.set(0);

		if(registeredhealers.size() > 0)
		{
			if(registeredhealers.size() == 2)
			{				
				L2PcInstance healer1 = registeredhealers.get(0);
				L2PcInstance healer2 = registeredhealers.get(1);

				players.put(healer1, new int[] {1, 0, 0 });
				players.put(healer2, new int[] {2, 0, 0 });

				EventManager.getInstance().players.remove(healer1);
				EventManager.getInstance().players.remove(healer2);
			}
			else
			{
				if(getPlayersOfTeam(1).size() > getPlayersOfTeam(2).size())
					counter.set(1);

				registeredhealers.forEach(player -> 
				{
					counter.incrementAndGet();

					players.put(player, new int[] {counter.get(), 0, 0 });
					EventManager.getInstance().players.remove(player);

					if (counter.get() == number)
						counter.set(0);
				});
			}
		}		
	}
	
	public void forceSitAll()
	{
		getStreamedPlayerList().forEach(player ->
		{
			player.abortAttack();
			player.abortCast();
			player.setIsParalyzed(true);
			player.setIsInvul(true);
			player.startAbnormalEffect(AbnormalEffect.HOLD_2);
		});
	}
	
	public void forceStandAll()
	{
		getStreamedPlayerList().forEach(player ->
		{
			player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
			player.setIsInvul(false);
			player.setIsParalyzed(false);
		});
	}
	
	public void InvisAll()
	{		
		getStreamedPlayerList().forEach(player ->
		{
			player.abortAttack();
			player.abortCast();
			player.getAppearance().setIsVisible(false);
		});
	}
	
	public void unInvisAll()
	{
		getStreamedPlayerList().forEach(player ->
		{
			player.getAppearance().setIsVisible(true);
			player.broadcastUserInfo();
		});
	}
	
	public boolean getBoolean(String propName)
	{
		return config.getBoolean(eventId, propName);
	}
	
	public int[] getColor(String owner)
	{
		return config.getColor(eventId, owner);
	}
	
	public int getInt(String propName)
	{
		return config.getInt(eventId, propName);
	}
	
	protected Set<L2PcInstance> getPlayerList()
	{
		return players.keySet();				
	}
	
	protected List<L2PcInstance> getStreamedPlayerList()
	{
		return players.keySet().stream().filter(Objects:: nonNull).filter(L2PcInstance::isOnline).collect(Collectors.toList());			
	}
	
	public List<L2PcInstance> getPlayersOfTeam(int team)
	{
		return getStreamedPlayerList().stream().filter( p -> getTeam(p) == team).collect(Collectors.toList());
	}
	
	protected EventTeam getPlayersTeam(L2PcInstance player)
	{
		return teams.get(players.get(player)[0]);
	}
	
	public List<L2PcInstance> getPlayersWithStatus(int status)
	{
		return getStreamedPlayerList().stream().filter( p -> getStatus(p) == status).collect(Collectors.toList());
	}
	
	protected List<L2PcInstance> getTopPlayers(int limit)
	{
		List<L2PcInstance> temp = new CopyOnWriteArrayList<>();
		List<L2PcInstance> copy = new CopyOnWriteArrayList<>(players.keySet());
		for (int i = 0; i < limit; i++)
		{
			L2PcInstance max = null;
			for (L2PcInstance ps : copy)
			{
				if ((max == null) || (players.get(ps)[2] > players.get(max)[2]))
					max = ps;
			}
			if (max == null)
				break;
			
			temp.add(max);
			copy.remove(max);
		}
		
		return temp;
	}
	
	public L2PcInstance getPlayerWithMaxScore()
	{
		List<L2PcInstance> players = getTopPlayers(1);
		if (players.isEmpty())
			return null;
		
		return players.get(0);
	}
	
	public int[] getPosition(String owner, int num)
	{
		return config.getPosition(eventId, owner, num);
	}
	
	protected L2PcInstance getRandomPlayer()
	{
		List<L2PcInstance> temp = new CopyOnWriteArrayList<>();
		
		getStreamedPlayerList().forEach(player ->
		{
			temp.add(player);
		});
		
		return temp.get(Rnd.get(temp.size()));
	}
	
	protected L2PcInstance getRandomPlayerFromTeam(int team)
	{
		List<L2PcInstance> temp = new CopyOnWriteArrayList<>();
		getStreamedPlayerList().forEach(player ->
		{
			if (getTeam(player) == team)
				temp.add(player);
		});
		
		return temp.get(Rnd.get(temp.size()));
	}
	
	protected List<L2PcInstance> getPlayersFromTeamWithStatus(int team, int status)
	{
		List<L2PcInstance> players = getPlayersWithStatus(status);
		List<L2PcInstance> temp = new CopyOnWriteArrayList<>();
		
		for (L2PcInstance player : players)
			if (getTeam(player) == team)
				temp.add(player);
		
		return temp;
	}
	
	protected L2PcInstance getRandomPlayerFromTeamWithStatus(int team, int status)
	{
		List<L2PcInstance> temp = getPlayersFromTeamWithStatus(team, status);
		return temp.get(Rnd.get(temp.size()));
	}
	
	public List<Integer> getRestriction(String type)
	{
		return config.getRestriction(eventId, type);
	}
	
	public int getScore(L2PcInstance player)
	{
		return players.get(player)[2];
	}
	
	protected int getStatus(L2PcInstance player)
	{
		return players.get(player)[1];
	}
	
	public String getString(String propName)
	{
		return config.getString(eventId, propName);
	}
	
	public int getTeam(L2PcInstance player)
	{
		return players.get(player)[0];
	}
	
	public int getWinnerTeam()
	{
		int maxScore = 0;
		int teamNum = 0;
		for (EventTeam team : teams.values())
		{
			if (team.getScore() > maxScore)
			{
				maxScore = team.getScore();
				teamNum = team.getId();
			}
		}
		
		return teamNum;
	}
	
	public void giveReward(List<L2PcInstance> players, int id, int ammount)
	{
		for (L2PcInstance player : players)
		{
			if (player == null)
				continue;
			
			player.addItem("Event", id, ammount, player, true);
			//EventStats.getInstance().tempTable.get(player.getObjectId())[0] = 1;
		}
	}
	
	public void giveReward(L2PcInstance player, int id, int ammount)
	{
		//EventStats.getInstance().tempTable.get(player.getObjectId())[0] = 1;
		player.addItem("Event", id, ammount, player, true);
	}
	
	public void increasePlayersScore(L2PcInstance player)
	{	
		int old = getScore(player);
		setScore(player, old + 1);
		//EventStats.getInstance().tempTable.get(player.getObjectId())[3] = EventStats.getInstance().tempTable.get(player.getObjectId())[3] + 1;
	}
	
	protected void msgToAll(String text)
	{
		if (text.isEmpty())
			return;
		getStreamedPlayerList().forEach(player ->
		{
			player.sendMessage(text);
		});
	}
	
	public void onDie(L2PcInstance victim, L2Character killer)
	{
		//EventStats.getInstance().tempTable.get(victim.getObjectId())[2] = EventStats.getInstance().tempTable.get(victim.getObjectId())[2] + 1;
	}
	
	public void onKill(L2Character victim, L2PcInstance killer)
	{
		//EventStats.getInstance().tempTable.get(killer.getObjectId())[1] = EventStats.getInstance().tempTable.get(killer.getObjectId())[1] + 1;
	}
	
	public void onLogout(L2PcInstance player)
	{
		if (players.containsKey(player))
			removePlayer(player);
		
		player.setXYZ(EventManager.getInstance().positions.get(player)[0], EventManager.getInstance().positions.get(player)[1], EventManager.getInstance().positions.get(player)[2]);
		player.getAppearance().setNameColor(EventManager.getInstance().colors.get(player));
		
		String title = EventManager.getInstance().titles.get(player);	
		player.setTitle(title == null || title.isEmpty() ? "" : title);

		if (teams.size() == 1)
		{
			if (getPlayerList().size() == 1)
				endEvent();
		}
		else
		{
			int t = players.values().iterator().next()[0];
			for (L2PcInstance p : getPlayerList())
				if (getTeam(p) != t)
					return;
			
			endEvent();
		}
	}
	
	public boolean onSay(int type, L2PcInstance player, String text)
	{
		return true;
	}
	
	public boolean onTalkNpc(L2Npc npc, L2PcInstance player)
	{
		return false;
	}
	
	public boolean onUseItem(L2PcInstance player, L2ItemInstance item)
	{
		if (EventManager.getInstance().getRestriction("item").contains(item.getItemId()) || getRestriction("item").contains(item.getItemId()))
			return false;
		
		if (item.getItemType() == L2EtcItemType.POTION && !getBoolean("allowPotions"))
			return false;
		
		if (item.getItemType() == L2EtcItemType.SCROLL)
			return false;
		
		if (item.getItemType() == L2EtcItemType.PET_COLLAR)
			return false;
		
		return true;
	}
	
	public boolean onUseMagic(L2Skill skill)
	{
		if (EventManager.getInstance().getRestriction("skill").contains(skill.getId()) || getRestriction("skill").contains(skill.getId()))
			return false;

		if (skill.getSkillType() == L2SkillType.RESURRECT)
			return false;
		
		if (skill.getSkillType() == L2SkillType.SUMMON_FRIEND 
		|| skill.getSkillType() == L2SkillType.SUMMON
		|| skill.getSkillType() == L2SkillType.SPAWN)
			return false;
		
		if (skill.getSkillType() == L2SkillType.RECALL)
			return false;
		
		if (skill.getSkillType() == L2SkillType.FAKE_DEATH)
			return false;
		
		return true;
	}
	
	protected void prepare(L2PcInstance player)
	{
		if (player.isDead())
			player.doRevive();
		
		player.abortAllAttacks();
		
		player.getAppearance().setIsVisible(true);
		
		if (player.getPet()!=null)
			player.getPet().unSummon(player);
		
		if (player.isMounted())
			player.dismount();
		
		if (getBoolean("removeBuffs"))
			player.stopAllEffects();
		else
		{
			for (L2Effect e : player.getAllEffects())
				if (e.getSkill().isHeroSkill())
					e.exit();
		}
		
		L2ItemInstance wpn = player.getActiveWeaponInstance();
		if (wpn != null && wpn.isHeroItem())
			player.useEquippableItem(wpn.getItemId(), false);
		
		if (player.getParty() != null)
		{
			L2Party party = player.getParty();
			party.removePartyMember(player);
		}

		if (teams.size() == 2)
		{
			final int teamId = getTeam(player);
			switch (teamId)
			{
				case 0:
				{
					player.setTeam(Team.NONE);
					break;
				}
				case 1:
				{
					player.setTeam(Team.BLUE);
					break;
				}
				case 2:
				{
					player.setTeam(Team.RED);
					break;
				}
			}
		}
				
		int[] nameColor = getPlayersTeam(player).getTeamColor();
		player.getAppearance().setNameColor(nameColor[0], nameColor[1], nameColor[2]);
		player.setTitle("<- 0 ->");
		
		if (EventManager.getInstance().getBoolean("eventBufferEnabled")) 
			EventBuffer.getInstance().buffPlayer(player);
		
		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());	
		player.broadcastUserInfo();
	}

	public void preparePlayers()
	{
		getStreamedPlayerList().forEach(player ->
		{
			prepare(player);	
		});
	}
	
	protected void removePlayer(L2PcInstance player)
	{
		players.remove(player);
	}
	
	public void reset()
	{
		players.clear();
		winnerTeam = 0;
		
		for (EventTeam team : teams.values())
			team.setScore(0);
	}
	
	protected void setScore(L2PcInstance player, int score)
	{
		players.get(player)[2] = score;
		player.setTitle("<- " + score + " ->");
		player.broadcastUserInfo();
	}
	
	public void setStatus(L2PcInstance player, int status)
	{
		if (players.containsKey(player))
			players.get(player)[1] = status;
	}
	
	protected void setTeam(L2PcInstance player, int team)
	{
		players.get(player)[0] = team;
	}
	
	public L2Spawn spawnNPC(int xPos, int yPos, int zPos, int npcId)
	{
		final L2NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
		
		try
		{
			final L2Spawn spawn = new L2Spawn(template);
			spawn.setLocx(xPos);
			spawn.setLocy(yPos);
			spawn.setLocz( zPos);
			spawn.setRespawnDelay(1);
			SpawnData.getInstance().addNewSpawn(spawn, false);
			spawn.doSpawn();
			spawn.getLastSpawn().broadcastInfo();
			return spawn;
		}
		catch (Exception e)
		{
			return null;
		}
	}
		
	public void closeAllDoors()
	{
        closeDoor(24190002);
        closeDoor(24190003);
        closeDoor(24190001);
        closeDoor(24190004);
	}

	public void openAllDoors()
	{
        openDoor(24190002);
        openDoor(24190003);
        openDoor(24190001);
        openDoor(24190004);
	}
	
	protected static void closeDoor(int i)
    {           
        L2DoorInstance doorInstance = DoorData.getInstance().getDoor(i);        
           if (doorInstance != null)
               doorInstance.closeMe();        
    }
   
	protected static void openDoor(int i)
    {
        L2DoorInstance doorInstance = DoorData.getInstance().getDoor(i);           
          if (doorInstance != null)
              doorInstance.openMe();
    }
	
	protected void teleportPlayer(L2PcInstance player, int[] coordinates)
	{
		player.teleToLocation(coordinates[0] + (Rnd.get(coordinates[3] * 2) - coordinates[3]), coordinates[1] + (Rnd.get(coordinates[3] * 2) - coordinates[3]), coordinates[2], false);
	}
	
	public void teleportToTeamPos()
	{		
		getStreamedPlayerList().forEach(player ->
		{
			teleportTask(player);
		});
	}
	
	protected void teleportToTeamPos(L2PcInstance player)
	{
		if(player != null && player.isOnline())
		{
			int[] pos = getPosition(teams.get(getTeam(player)).getName(), 0);
			teleportPlayer(player, pos);
		}
	}
		
	protected void teleportTask(L2PcInstance player)
	{	
		ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(player), Rnd.get(1500,3500));
	}
	
	protected class TeleportTask implements Runnable
	{		
		L2PcInstance _player;
		
		public TeleportTask(L2PcInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			teleportToTeamPos(_player);
		}
	}
	
	protected void unspawnNPC(L2Spawn npcSpawn)
	{
		if (npcSpawn == null)
			return;
		
		npcSpawn.getLastSpawn().deleteMe();
		npcSpawn.stopRespawn();
		SpawnData.getInstance().deleteSpawn(npcSpawn, false);
	}
	
	public int numberOfTeams()
	{
		return teams.size();
	}

	public void sendMsg()
	{
		getStreamedPlayerList().forEach(player ->
		{
			player.sendPacket(new ExShowScreenMessage(1, -1, 2, 0, 0, 0, 0, false, 3000, 0, getStartingMsg()));
		});
	}
	
	protected abstract void endEvent();
	protected abstract String getStartingMsg();
	protected abstract void start();
	protected abstract void showHtml(L2PcInstance player, int obj);
	protected abstract void schedule(int time);
	protected abstract boolean canAttack(L2PcInstance player,L2PcInstance target);
}
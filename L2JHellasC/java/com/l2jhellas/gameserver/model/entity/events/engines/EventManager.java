package com.l2jhellas.gameserver.model.entity.events.engines;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.enums.Team;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.CTF;
import com.l2jhellas.gameserver.model.entity.events.DM;
import com.l2jhellas.gameserver.model.entity.events.Domination;
import com.l2jhellas.gameserver.model.entity.events.DoubleDomination;
import com.l2jhellas.gameserver.model.entity.events.Korean;
import com.l2jhellas.gameserver.model.entity.events.LMS;
import com.l2jhellas.gameserver.model.entity.events.TvT;
import com.l2jhellas.gameserver.model.entity.events.VIPTvT;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.util.Broadcast;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class EventManager
{
	private EventConfig config;
	public Map<Integer, Event> events;
	public List<L2PcInstance> players;
	private Event current;
	protected Map<L2PcInstance, Integer> colors;
	protected Map<L2PcInstance, String> titles;
	protected Map<L2PcInstance, int[]> positions;
	protected Map<L2PcInstance, Integer> votes;
	protected State status;
	protected int counter;
	protected Countdown cdtask;
	private Scheduler task;
	protected Random rnd = new Random();
	protected List<Integer> eventIds;
	
	protected enum State
	{
		REGISTERING, VOTING, RUNNING, END
	}
	
	protected class Countdown implements Runnable
	{
		protected String getTime()
		{
			String mins = "" + counter / 60;
			String secs = (counter % 60 < 10 ? "0" + counter % 60 : "" + counter % 60);
			return mins + ":" + secs;
		}
		
		@Override
		public void run()
		{
			if (status == State.REGISTERING)
			{
				switch (counter)
				{
					case 300:
					case 240:
					case 180:
					case 120:
					case 60:
						announce(counter / 60 + " min(s) left to register, " + getCurrentEvent().getString("eventName"));
						break;
					case 30:
					case 10:
						announce(counter + " seconds left to register!");
						break;
				}
			}
			
			if (status == State.VOTING && counter == getInt("showVotePopupAt") && getBoolean("votePopupEnabled"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(0);
				StringBuilder sb = new StringBuilder();
				int count = 0;

				sb.append("<html><body><center><table width=270><tr><td width=270><center>Event Engine - Vote for your favourite event!</center></td></tr></table></center><br>");
				
				for (Map.Entry<Integer, Event> event : events.entrySet())
				{
					count++;
					sb.append("<center><table width=270 " + (count % 2 == 1 ? "" : "bgcolor=000000") + "><tr><td width=240>" + event.getValue().getString("eventName") + "</td><td width=30><a action=\"bypass -h eventvote " + event.getKey() + "\">Vote</a></td></tr></table></center>");
					sb.append("<img src=\"L2UI.Squaregray\" width=\"270\" height=\"1\">");
				}

				sb.append("</body></html>");
				html.setHtml(sb.toString());
				
				for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				{
					if (player == null || votes.containsKey(player) || player.getLevel() < 40)
						continue;
					
					player.sendPacket(html);
				}
			}
			
			if (counter == 0)
				schedule(1);
			else
			{
				counter--;
				ThreadPoolManager.getInstance().scheduleGeneral(cdtask, 1000);
			}
		}
	}

	protected class Scheduler implements Runnable
	{
		@Override
		public void run()
		{
			switch (status)
			{
				case VOTING:
					if (votes.size() > 0)
						setCurrentEvent(getVoteWinner());
					else
						setCurrentEvent(eventIds.get(rnd.nextInt(eventIds.size())));
					
					announce("The next event will be: " + getCurrentEvent().getString("eventName"));
					announce("Registering phase started! You have " + getInt("registerTime") / 60 + " minutes to register!");
					announce("You can use commands .join - .leave  , or visit the event manager.");
					setStatus(State.REGISTERING);
					counter = getInt("registerTime") - 1;
					ThreadPoolManager.getInstance().scheduleGeneral(cdtask, 1);
					break;					
				case REGISTERING:
					announce("Registering phase ended!");
					if (players.size() < getCurrentEvent().getInt("minPlayers"))
					{
						announce("There are not enough participants! Next event in " + getInt("betweenEventsTime") / 60 + "mins!");
						setCurrentEvent(0);
						players.clear();
						colors.clear();
						positions.clear();
						setStatus(State.VOTING);
						counter = getInt("betweenEventsTime") - 1;
						ThreadPoolManager.getInstance().scheduleGeneral(cdtask, 1);
					}
					else
					{
						announce("Event started!");
						setStatus(State.RUNNING);
						msgToAll("You'll be teleported to the event in 10 seconds.");
						schedule(8000);
					}
					break;					
				case RUNNING:
					if (getCurrentEvent() instanceof Korean)
					{
						if (players.size() < 15)
						{
							if (players.size() % 2 != 0)
								players.remove(Rnd.get(players.size()));
						}
						else
						{
							while (players.size() % 3 != 0)
								players.remove(Rnd.get(players.size()));
						}
					}
					getCurrentEvent().start();
					
					for (L2PcInstance player : players)
					{
						if(player == null)
							continue;
						
						EventStats.getInstance().tempTable.put(player.getObjectId(), new int[] { 0, 0, 0, 0 });
					}
					break;
			default:
				break;
			}
		}
	}
	
	protected void teleportFinish()
	{	
		setStatus(State.END);
		announce("You'll be teleported back in 10 seconds!");
		
		ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(), 10000);
	}
	
	protected class TeleportTask implements Runnable
	{		
		public TeleportTask()
		{
			
		}
		
		@Override
		public void run()
		{
			teleBackEveryone();
			EventStats.getInstance().applyChanges();
			EventStats.getInstance().tempTable.clear();
			EventStats.getInstance().sumPlayerStats();
			players.clear();
			colors.clear();
			positions.clear();
			titles.clear();
			getCurrentEvent().reset();
			setCurrentEvent(0);
			announce("Event ended! Next event in " + getInt("betweenEventsTime") / 60 + " mins!");
			setStatus(State.VOTING);
			counter = getInt("betweenEventsTime") - 1;
			ThreadPoolManager.getInstance().scheduleGeneral(cdtask, 1);
		}
	}
	
	public EventManager()
	{
		config = EventConfig.getInstance();
		
		events = new ConcurrentHashMap<>();
		players = new CopyOnWriteArrayList<>();
		votes = new ConcurrentHashMap<>();
		titles = new ConcurrentHashMap<>();
		colors = new ConcurrentHashMap<>();
		positions = new ConcurrentHashMap<>();
		eventIds = new CopyOnWriteArrayList<>();
		status = State.VOTING;
		task = new Scheduler();
		cdtask = new Countdown();
		counter = 0;

		events.put(1, new DM());
		events.put(2, new Domination());
		events.put(3, new DoubleDomination());	
		events.put(4, new LMS());		
		events.put(5, new TvT());		
		events.put(6, new VIPTvT());		
		events.put(7, new CTF());		
		events.put(8, new Korean());
						
		for (int eventId : events.keySet())
			eventIds.add(eventId);
		
		counter = getInt("firstAfterStartTime") - 1;
		ThreadPoolManager.getInstance().scheduleGeneral(cdtask, 1);

		System.out.println("Event Engine Started");
	}
	
	public boolean addVote(L2PcInstance player, int eventId)
	{
		if (getStatus() != State.VOTING)
		{
			player.sendMessage("You can't vote now!");
			return false;
		}
		if (votes.containsKey(player))
		{
			player.sendMessage("You have already voted for an event!");
			return false;
		}
		if (player.getLevel() < 40)
		{
			player.sendMessage("Your level is too low to vote for events!");
			return false;
		}
		
		player.sendMessage("You have succesfully voted for the event");
		votes.put(player, eventId);
		return true;
	}
	
	protected static void announce(String text)
	{
		Broadcast.toAllOnlinePlayers(new CreatureSay(0, 18, "", "[Event] " + text));
	}
	
	private boolean canRegister(L2PcInstance player)
	{
		if (players.contains(player))
		{
			player.sendMessage("You are already registered to the event!");
			return false;
		}
		if (player.isInJail())
		{
			player.sendMessage("You can't register from the jail.");
			return false;
		}
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You can't register while you are in the olympiad.");
			return false;
		}
		if (player.getLevel() > getCurrentEvent().getInt("maxLvl"))
		{
			player.sendMessage("You are greater than the maximum allowed lvl.");
			return false;
		}
		if (player.getLevel() < getCurrentEvent().getInt("minLvl"))
		{
			player.sendMessage("You are lower than the minimum allowed lvl.");
			return false;
		}
		if (player.getKarma() > 0)
		{
			player.sendMessage("You can't register if you have karma.");
			return false;
		}
		if (player.isCursedWeaponEquiped())
		{
			player.sendMessage("You can't register with a cursed weapon.");
			return false;
		}
		if (player.isDead())
		{
			player.sendMessage("You can't register while you are dead.");
			return false;
		}
		if (!getBoolean("dualboxAllowed"))
		{
			String ip = player.getClient().getConnection().getInetAddress().getHostAddress();
			for (L2PcInstance p : players)
			{
				if (p.getClient().getConnection().getInetAddress().getHostAddress().equalsIgnoreCase(ip))
				{
					player.sendMessage("You have already joined the event with another character.");
					return false;
				}
			}
		}
		
		return true;
	}

	public boolean canAttack(L2PcInstance player, L2PcInstance target)
	{
		if (getStatus() == State.RUNNING)
			return getCurrentEvent().canAttack(player, target);
		
		return true;
	}
	
	public void end(String text)
	{
		announce(text);		
		teleportFinish();	
	}
		
	public boolean getBoolean(String propName)
	{
		return config.getBoolean(0, propName);
	}
	
	public Event getCurrentEvent()
	{
		return current;
	}
	
	public List<String> getEventNames()
	{
		List<String> map = new CopyOnWriteArrayList<>();
		for (Event event : events.values())
			map.add(event.getString("eventName"));
		
		return map;
	}

	public int getInt(String propName)
	{
		return config.getInt(0, propName);
	}
	
	protected int[] getPosition(String owner, int num)
	{
		return config.getPosition(0, owner, num);
	}
	
	public List<Integer> getRestriction(String type)
	{
		return config.getRestriction(0, type);
	}
	
	public int getInt(int eventId, String propName)
	{
		return config.getInt(eventId, propName);
	}
	
	public boolean getBoolean(int eventId, String propName)
	{
		return config.getBoolean(eventId, propName);
	}
	
	public String getString(int eventId, String propName)
	{
		return config.getString(eventId, propName);
	}
	
	private State getStatus()
	{
		return status;
	}
	
	public String getString(String propName)
	{
		return config.getString(0, propName);
	}
	
	private int getVoteCount(int event)
	{
		int count = 0;
		for (int e : votes.values())
			if (e == event)
				count++;
		
		return count;
	}
	
	protected int getVoteWinner()
	{
		int eventId = 0;
		int maxVotes = -1;
		Map<Integer, Integer> temp = new ConcurrentHashMap<>();
		for (int vote : votes.values())
		{
			if (!temp.containsKey(vote))
				temp.put(vote, 1);
			else
				temp.put(vote, temp.get(vote) + 1);
		}
		for (Entry<Integer, Integer> v : temp.entrySet())
		{
			if (v.getValue() > maxVotes)
			{
				eventId = v.getKey();
				maxVotes = v.getValue();
			}
		}
		
		votes.clear();
		return eventId;
	}
	
	public boolean isRegistered(L2PcInstance player)
	{
		if (getCurrentEvent() != null)
			return getCurrentEvent().players.containsKey(player);
		
		return false;
	}
	
	public boolean isRegistered(L2Character player)
	{
		if (getCurrentEvent() != null)
			return getCurrentEvent().players.containsKey(player);
		
		return false;
	}
	
	public boolean isRunning()
	{
		if (getStatus() == State.RUNNING)
			return true;
		
		return false;
	}
	
	protected void msgToAll(String text)
	{
		for (L2PcInstance player : players)
			player.sendMessage(text);
	}
	
	public void onLogout(L2PcInstance player)
	{
		if (votes.containsKey(player))
			votes.remove(player);

		if (players.contains(player))
			players.remove(player);

		if(colors.containsKey(player))
			colors.remove(player);

		if(titles.containsKey(player))
			titles.remove(player);

		if(positions.containsKey(player))
			positions.remove(player);
	}
	
	public boolean registerPlayer(L2PcInstance player)
	{
		if (getStatus() != State.REGISTERING)
		{
			player.sendMessage("You can't register now!");
			return false;
		}
		if (getBoolean("eventBufferEnabled"))
		{
			if (!EventBuffer.getInstance().playerHaveTemplate(player)) 
			{ 
				player.sendMessage("You have to set a buff template first!"); 
				EventBuffer.getInstance().showHtml(player); 
				return false; 
			}
		}
		if (canRegister(player))
		{
			player.sendMessage("You have succesfully registered to the event!");
			players.add(player);
			
			if(player.getTitle() != null)
			   titles.put(player, player.getTitle());
			
			colors.put(player, player.getAppearance().getNameColor());
			positions.put(player, new int[] { player.getX(), player.getY(), player.getZ() });
			return true;
		}
		
		player.sendMessage("You have failed registering to the event!");
		return false;
	}
	
	protected void schedule(int time)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(task, time);
	}
	
	protected void setCurrentEvent(int eventId)
	{
		current = eventId == 0 ? null : events.get(eventId);
	}
	
	protected void setStatus(State s)
	{
		status = s;
	}
	
	public void showFirstHtml(L2PcInstance player, int obj)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(obj);
		StringBuilder sb = new StringBuilder();
		int count = 0;

		sb.append("<html><body><center><table width=270><tr><td width=145>Event Buffer</td><td width=75>" + (getBoolean("eventBufferEnabled") ? "<a action=\"bypass -h eventbuffershow\">Buffer</a>" : "") + "</td></tr></table></center><br>");
		
		if (getStatus() == State.VOTING)
		{
			sb.append("<center><table width=270 bgcolor=000000><tr><td width=90>Events</td><td width=140><center>Time left: " + cdtask.getTime() + "</center></td><td width=40><center>Votes</center></td></tr></table></center><br>");
	
			for (Map.Entry<Integer, Event> event : events.entrySet())
			{
				count++;
				sb.append("<center><table width=270 " + (count % 2 == 1 ? "" : "bgcolor=000000") + "><tr><td width=180>" + event.getValue().getString("eventName") + "</td><td width=30><a action=\"bypass -h eventinfo " + event.getKey() + "\">Info</a></td><td width=30><center>" + getVoteCount(event.getKey()) + "</td></tr></table></center>");
				sb.append("<img src=\"L2UI.Squaregray\" width=\"270\" height=\"1\">");
			}
			
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
		}
		else if (getStatus() == State.REGISTERING)
		{
			sb.append("<center><table width=270><tr><td width=70>");
			
			if (players.contains(player))
				sb.append("<a action=\"bypass -h npc_" + obj + "_unreg\">Unregister</a>");
			else
				sb.append("<a action=\"bypass -h npc_" + obj + "_reg\">Register</a>");
			
			sb.append("</td><td width=130><center><a action=\"bypass -h eventinfo " + getCurrentEvent().getInt("EventId") + "\">" + getCurrentEvent().getString("eventName") + "</a></td><td width=70>Time: " + cdtask.getTime() + "</td></tr></table><br>");
			
			sb.append("<center><table width=270 "+"><tr><td width=120>" + "Registered Players: " + EventManager.getInstance().players.size() + "</td><td width=40>");
			
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
		}
		else if (getStatus() == State.RUNNING)
			getCurrentEvent().showHtml(player, obj);
	}
	
	protected void teleBackEveryone()
	{
		for (L2PcInstance player : getCurrentEvent().getPlayerList())
		{				
			if (player.isOnline() != 0)
			{
				player.setTeam(Team.NONE);

				if (player.isDead())
					player.doRevive();
				
				player.getAppearance().setNameColor(colors.get(player));
				
				String title = titles.get(player);
				
				player.setTitle(title == null || title.isEmpty() ? "" : title);
				
				if (player.getParty() != null)
					player.leaveParty();
				
				player.teleToLocation(positions.get(player)[0], positions.get(player)[1], positions.get(player)[2], true);
			    player.broadcastUserInfo();
			}
			else
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE char_name=?"))
				{
					statement.setInt(1, positions.get(player)[0]);
					statement.setInt(2, positions.get(player)[1]);
					statement.setInt(3, positions.get(player)[2]);
					statement.setString(4, player.getName());
					statement.execute();
				}
				catch (SQLException se)
				{
					se.printStackTrace();
				}
			}						
		}
	}

	public boolean unregisterPlayer(L2PcInstance player)
	{
		if (!players.contains(player))
		{
			player.sendMessage("You are not registered to the event!");
			return false;
		}
		else if (getStatus() != State.REGISTERING)
		{
			player.sendMessage("You can't unregister now!");
			return false;
		}
		
		player.sendMessage("You have succesfully unregistered from the event!");
		players.remove(player);
		colors.remove(player);
		positions.remove(player);
		return true;
	}
	
	public boolean areTeammates(L2PcInstance player, L2PcInstance target) 
	{ 
		if (getCurrentEvent() == null) 
			return false; 
		
		if (getCurrentEvent().numberOfTeams() < 2) 
			return false; 
		
		if (getCurrentEvent().getTeam(player) == getCurrentEvent().getTeam(target)) 
			return true;
		
		return false;
	}
	
	public void manualStart(int eventId)
	{
		setCurrentEvent(eventId);
		announce("The next event will be: " + getCurrentEvent().getString("eventName"));
		announce("Registering phase started! You have " + getInt("registerTime") / 60 + " minutes to register!");
		announce("You can use commands .join - .leave  , or visit the event manager.");
		setStatus(State.REGISTERING);
		counter = getInt("registerTime") - 1;
	}
	
	public void manualStop()
	{
		announce("The event has been aborted by a GM.");
		if (getStatus() == State.REGISTERING)
		{
			setCurrentEvent(0);
			players.clear();
			colors.clear();
			positions.clear();
			setStatus(State.VOTING);
			counter = getInt("betweenEventsTime") - 1;
		}
		else if (getStatus() == State.RUNNING)
			getCurrentEvent().endEvent();
	}
	
	public boolean isSpecialEvent()
	{
		return getCurrentEvent() != null && (getCurrentEvent() instanceof LMS || getCurrentEvent() instanceof DM);
	}
		
	private static class SingletonHolder
	{
		protected static final EventManager _instance = new EventManager();
	}
	
	public static EventManager getInstance()
	{
		return SingletonHolder._instance;
	}
}
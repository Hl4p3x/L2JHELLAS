package com.l2jhellas.gameserver.model.entity.events.engines;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class EventStats
{
	public Map<Integer, int[]> tempTable;
	private Map<Integer, Map<Integer, StatModell>> stats;
	private Map<Integer, int[]> statSums;
	
	private static class SingletonHolder
	{
		protected static final EventStats _instance = new EventStats();
	}
	
	public static EventStats getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected class StatModell
	{
		protected int num;
		protected int wins;
		protected int losses;
		protected int kills;
		protected int deaths;
		protected int scores;
		
		protected StatModell(int num, int wins, int losses, int kills, int deaths, int scores)
		{
			this.num = num;
			this.wins = wins;
			this.losses = losses;
			this.kills = kills;
			this.deaths = deaths;
			this.scores = scores;
		}
	}
	
	public EventStats()
	{
		stats = new ConcurrentHashMap<>();
		tempTable = new ConcurrentHashMap<>();
		statSums = new ConcurrentHashMap<>();
	}
	
	protected void applyChanges()
	{
		int eventId = EventManager.getInstance().getCurrentEvent().eventId;
		for (L2PcInstance player : EventManager.getInstance().getCurrentEvent().getPlayerList())
		{
			int playerId = player.getObjectId();
			
			if (!stats.containsKey(playerId))
				stats.put(playerId, new ConcurrentHashMap<Integer, StatModell>());
			
			if (!stats.get(playerId).containsKey(eventId))
				stats.get(playerId).put(eventId, new StatModell(0, 0, 0, 0, 0, 0));
			
			if (tempTable.get(playerId)[0] == 1)
				stats.get(playerId).get(eventId).wins = stats.get(playerId).get(eventId).wins + 1;
			else
				stats.get(playerId).get(eventId).losses = stats.get(playerId).get(eventId).losses + 1;
			
			stats.get(playerId).get(eventId).num = stats.get(playerId).get(eventId).num + 1;
			stats.get(playerId).get(eventId).kills = stats.get(playerId).get(eventId).kills + tempTable.get(playerId)[1];
			stats.get(playerId).get(eventId).deaths = stats.get(playerId).get(eventId).deaths + tempTable.get(playerId)[2];
			stats.get(playerId).get(eventId).scores = stats.get(playerId).get(eventId).scores + tempTable.get(playerId)[3];
		}
	}
	
	public int getEventKills(int playerId)
	{
		int kills = 0;
		
		if(!stats.containsKey(playerId))
			return 0;
		
		for (Map.Entry<Integer, StatModell> statmodell : stats.get(playerId).entrySet())
			kills += statmodell.getValue().kills;
		
		return kills;
	}
	
	public int getEvents(int playerId)
	{
		int num = 0;
		
		if(!stats.containsKey(playerId))
			return 0;
		
		for (Map.Entry<Integer, StatModell> statmodell : stats.get(playerId).entrySet())
			num += statmodell.getValue().num;
		
		return num;
	}
	
	public int getEventWins(int playerId)
	{
		int wins = 0;
		
		if(!stats.containsKey(playerId))
			return 0;
		
		for (Map.Entry<Integer, StatModell> statmodell : stats.get(playerId).entrySet())
			wins += statmodell.getValue().wins;
		
		return wins;
	}
	
	protected void sumPlayerStats()
	{
		statSums.clear();
		
		for (int playerId : stats.keySet())
		{
			int num = 0;
			int wins = 0;
			int losses = 0;
			int kills = 0;
			int deaths = 0;
			int faveventid = 0;
			int faveventamm = 0;
			
			for (Map.Entry<Integer, StatModell> statmodell : stats.get(playerId).entrySet())
			{
				num += statmodell.getValue().num;
				wins += statmodell.getValue().wins;
				losses += statmodell.getValue().losses;
				kills += statmodell.getValue().kills;
				deaths += statmodell.getValue().deaths;
				
				if (statmodell.getValue().num > faveventamm)
				{
					faveventamm = statmodell.getValue().num;
					faveventid = statmodell.getKey();
				}
			}
			statSums.put(playerId, new int[] { num, wins, losses, kills, deaths, faveventid });
		}
	}
}
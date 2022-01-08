package com.l2jhellas.gameserver.model.entity.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.Event;
import com.l2jhellas.gameserver.model.entity.events.engines.EventManager;
import com.l2jhellas.gameserver.model.entity.events.engines.EventTeam;
import com.l2jhellas.gameserver.model.spawn.L2Spawn;
import com.l2jhellas.gameserver.model.spawn.SpawnData;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class DoubleDomination extends Event
{
	protected EventState eventState;
	private Core task = new Core();
	private Map<L2Spawn, Integer> zones = new ConcurrentHashMap<>();
	private int time;
	private int holder;
	private enum EventState
	{
		START, FIGHT, END, INACTIVE
	}
	
	protected class Core implements Runnable
	{
		@SuppressWarnings("incomplete-switch")
		@Override
		public void run()
		{
			try
			{
				switch (eventState)
				{
					case START:
						closeAllDoors();
						divideIntoTeams(2);
						preparePlayers();
						Spawn();
						teleportToTeamPos();
						createPartyOfTeam(1);
						createPartyOfTeam(2);
						forceSitAll();
						setStatus(EventState.FIGHT);
						schedule(35000);
						break;
						
					case FIGHT:
						forceStandAll();
						sendMsg();
						setStatus(EventState.END);
						clock.startClock(getInt("matchTime"));
						break;
						
					case END:
						openAllDoors();
						clock.setTime(0);
						if (winnerTeam == 0)
							winnerTeam = getWinnerTeam();
						
						unSpawnZones();
						setStatus(EventState.INACTIVE);
						
						if (winnerTeam == 0)
							EventManager.getInstance().end("The event ended in a tie! both teams had " + teams.get(1).getScore() + " domination points!");
						else
						{
							giveReward(getPlayersOfTeam(winnerTeam), getInt("rewardId"), getInt("rewardAmmount"));
							EventManager.getInstance().end("Congratulation! The " + teams.get(winnerTeam).getName() + " team won the event with " + teams.get(winnerTeam).getScore() + " domination points!");
						}
						break;
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				EventManager.getInstance().end("Error! Event ended.");
			}
		}
	}
	
	public DoubleDomination()
	{
		super();
		eventId = 3;
		createNewTeam(1, "Blue", getColor("Blue"), getPosition("Blue", 1));
		createNewTeam(2, "Red", getColor("Red"), getPosition("Red", 1));
	}
	
	@Override
	protected void clockTick()
	{
		int team1 = 0;
		int team2 = 0;
		
		for (L2Spawn zone : zones.keySet())
		{
			for (L2PcInstance player : getPlayerList())
			{
				switch (getTeam(player))
				{
					case 1:
						if (Math.sqrt(player.getPlanDistanceSq(zone.getLastSpawn().getX(), zone.getLastSpawn().getY())) <= getInt("zoneRadius"))
							team1++;
						break;
						
					case 2:
						if (Math.sqrt(player.getPlanDistanceSq(zone.getLastSpawn().getX(), zone.getLastSpawn().getY())) <= getInt("zoneRadius"))
							team2++;
						break;
				}
			}
			
			if (team1 > team2)
				zones.put(zone, 1);
			
			if (team2 > team1)
				zones.put(zone, 2);
			
			if (team1 == team2)
				zones.put(zone, 0);
			
			team1 = 0;
			team2 = 0;
		}
		
		if (zones.containsValue(1) && (!zones.containsValue(0) && !zones.containsValue(2)))
		{
			if (holder != 1)
			{
				announce(getPlayerList(), "The " + teams.get(1).getName() + " team captured both zones. Score in 10sec!");
				holder = 1;
				time = 0;
			}
			
			if (time == getInt("timeToScore") - 1)
			{
				for (L2PcInstance player : getPlayersOfTeam(1))
					increasePlayersScore(player);
				teams.get(1).increaseScore();
				teleportToTeamPos();
				time = 0;
				announce(getPlayerList(), "The " + teams.get(1).getName() + " team scored!");
				holder = 0;
			}
			else
				time++;
		}
		else if (zones.containsValue(2) && (!zones.containsValue(0) && !zones.containsValue(1)))
		{
			if (holder != 2)
			{
				announce(getPlayerList(), "The " + teams.get(2).getName() + " team captured both zones. Score in 10sec!");
				holder = 1;
				time = 0;
			}
			
			if (time == getInt("timeToScore") - 1)
			{
				for (L2PcInstance player : getPlayersOfTeam(2))
					increasePlayersScore(player);
				teams.get(2).increaseScore();
				teleportToTeamPos();
				time = 0;
				announce(getPlayerList(), "The " + teams.get(2).getName() + " team scored!");
				holder = 0;
			}
			else
				time++;
		}
		else
		{
			if (holder != 0)
				announce(getPlayerList(), "Canceled!");
			
			holder = 0;
			time = 0;
		}
	}
	
	@Override
	protected void endEvent()
	{
		setStatus(EventState.END);
		clock.setTime(0);
	}
	
	@Override
	public void onDie(L2PcInstance victim, L2Character killer)
	{
		super.onDie(victim, killer);
		addToResurrector(victim);
	}
	
	@Override
	protected void schedule(int time)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(task, time);
	}
	
	protected void setStatus(EventState s)
	{
		eventState = s;
	}
	
	@Override
	protected void showHtml(L2PcInstance player, int obj)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(obj);
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><table width=270><tr><td width=200>Event Engine </td><td><a action=\"bypass -h eventstats 1\">Statistics</a></td></tr></table><br><center><table width=270 bgcolor=5A5A5A><tr><td width=70>Running</td><td width=130><center>" + getString("eventName") + "</td><td width=70>Time: " + clock.getTime() + "</td></tr></table><table width=270><tr><td><center><font color=" + teams.get(1).getHexaColor() + ">" + teams.get(1).getScore() + "</font> - <font color=" + teams.get(2).getHexaColor() + ">" + teams.get(2).getScore() + "</font></td></tr></table><br><table width=270>");
		
		int i = 0;
		for (EventTeam team : teams.values())
		{
			i++;
			sb.append("<tr><td><font color=" + team.getHexaColor() + ">" + team.getName() + "</font> team</td><td></td><td></td><td></td></tr>");
			for (L2PcInstance p : getPlayersOfTeam(i))
				sb.append("<tr><td>" + p.getName() + "</td><td>lvl " + p.getLevel() + "</td><td>" + p.getTemplate().getClassName() + "</td><td>" + getScore(p) + "</td></tr>");
		}
		
		sb.append("</table></body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	protected void Spawn()
	{
		int[] z1pos = getPosition("Zone", 1);
		int[] z2pos = getPosition("Zone", 2);
		zones.put(spawnNPC(z1pos[0], z1pos[1], z1pos[2], getInt("zoneNpcId")), 0);
		zones.put(spawnNPC(z2pos[0], z2pos[1], z2pos[2], getInt("zoneNpcId")), 0);
	}
	
	@Override
	protected void start()
	{
		setStatus(EventState.START);
		schedule(1);
	}
	
	@Override
	protected boolean canAttack(L2PcInstance player, L2PcInstance target)
	{
		return (getPlayersTeam(player) != getPlayersTeam(target));
	}
	
	@Override
	protected String getStartingMsg()
	{
		return "Get as many team members as possible near both zones to score!";
	}

	protected void unSpawnZones()
	{
		for (L2Spawn s : zones.keySet())
		{
			s.getLastSpawn().deleteMe();
			s.stopRespawn();
			SpawnData.getInstance().deleteSpawn(s, true);
			zones.remove(s);
		}
	}
}
package com.l2jhellas.gameserver.model.entity.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.Event;
import com.l2jhellas.gameserver.model.entity.events.engines.EventManager;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class Korean extends Event
{
	protected EventState eventState;
	private Core task = new Core();
	private int round = 1;
	private int eventType;
	private Map<Integer, List<L2PcInstance>> fighters = new ConcurrentHashMap<>();
	private enum EventState
	{
		START, FIGHT, END, TELEPORT, INACTIVE
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
						divideIntoTeams(2);
						preparePlayers();
						teleportToTeamPos();
						createPartyOfTeam(1);
						createPartyOfTeam(2);
						forceSitAll();
						setStatus(EventState.FIGHT);
						schedule(35000);
						break;
						
					case FIGHT:
						selectNewPlayersOfTeam(1);
						selectNewPlayersOfTeam(2);
						setStatus(EventState.END);
						clock.startClock(getInt("matchTime"));
						break;
						
					case END:
						forceStandAll();
						clock.setTime(0);
						if (winnerTeam == 0)
							winnerTeam = getWinnerTeam();
						
						setStatus(EventState.INACTIVE);
						
						if (winnerTeam == 0)
							EventManager.getInstance().end("The event has ended in a tie!");
						else
						{
							giveReward(getPlayersOfTeam(winnerTeam), getInt("rewardId"), getInt("rewardAmmount"));
							EventManager.getInstance().end("Congratulation! The " + teams.get(winnerTeam).getName() + " team won the event!");
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
	
	public Korean()
	{
		super();
		eventId = 8;
		createNewTeam(1, "Blue", getColor("Blue"), getPosition("Blue", 1));
		createNewTeam(2, "Red", getColor("Red"), getPosition("Red", 1));
	}
	
	@Override
	protected void endEvent()
	{
		winnerTeam = getWinnerTeam();
		
		setStatus(EventState.END);
		clock.setTime(0);
	}
	
	@Override
	public void onDie(L2PcInstance victim, L2Character killer)
	{
		super.onDie(victim, killer);
				
		if (getTeam(victim) == 1)
		{
			teams.get(2).increaseScore();
			increasePlayersScore((L2PcInstance) killer);
			int[] nameColor = teams.get(1).getTeamColor();
			victim.getAppearance().setNameColor(nameColor[0], nameColor[1], nameColor[2]);
			setStatus(victim, -1);
			fighters.get(1).remove(victim);
			
			if (getPlayersFromTeamWithStatus(1, 0).size() == 0)
				endEvent();
			else if (fighters.get(1).size() == 0)
				selectNewPlayersOfTeam(1);
		}
		else
		{
			teams.get(1).increaseScore();
			increasePlayersScore((L2PcInstance) killer);
			int[] nameColor = teams.get(2).getTeamColor();
			victim.getAppearance().setNameColor(nameColor[0], nameColor[1], nameColor[2]);
			setStatus(victim, -1);
			fighters.get(2).remove(victim);
			
			if (getPlayersFromTeamWithStatus(2, 0).size() == 0)
				endEvent();
			else if (fighters.get(2).size() == 0)
				selectNewPlayersOfTeam(2);
		}
	}
	
	@Override
	protected void schedule(int time)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(task, time);
	}
	
	protected void selectNewPlayersOfTeam(int team)
	{
		if (eventState == EventState.END)
			round++;

		L2PcInstance p1 = null, p2 = null, p3 = null;
		if (eventType == 1)
		{
			p1 = getRandomPlayerFromTeamWithStatus(team, 0);
			setStatus(p1, -2);
			fighters.get(team).add(p1);
		}
		else if (eventType == 2)
		{
			p1 = getRandomPlayerFromTeamWithStatus(team, 0);
			fighters.get(team).add(p1);
			setStatus(p1, -2);
			do
			{
				p2 = getRandomPlayerFromTeamWithStatus(team, 0);
			} while (p2 == p1);
			setStatus(p2, -2);
			fighters.get(team).add(p2);
		}
		else
		{
			p1 = getRandomPlayerFromTeamWithStatus(team, 0);
			setStatus(p1, -2);
			fighters.get(team).add(p1);
			do
			{
				p2 = getRandomPlayerFromTeamWithStatus(team, 0);
			} while (p2 == p1);
			setStatus(p2, -2);
			fighters.get(team).add(p2);
			do
			{
				p3 = getRandomPlayerFromTeamWithStatus(team, 0);
			} while (p3 == p1 || p3 == p2);
			setStatus(p3, -2);
			fighters.get(team).add(p3);
		}

		if (team == 1)
		{
			int[] c = getColor("BlueFighters");
			p1.getAppearance().setNameColor(c[0], c[1], c[2]);
			if (p2 != null)
				p2.getAppearance().setNameColor(c[0], c[1], c[2]);
			if (p3 != null)
				p3.getAppearance().setNameColor(c[0], c[1], c[2]);
		}
		else if (team == 2)
		{
			int[] c = getColor("RedFighters");
			p1.getAppearance().setNameColor(c[0], c[1], c[2]);
			if (p2 != null)
				p2.getAppearance().setNameColor(c[0], c[1], c[2]);
			if (p3 != null)
				p3.getAppearance().setNameColor(c[0], c[1], c[2]);
		}

		for (L2PcInstance player : fighters.get(team))
		{
			player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
			player.setIsInvul(false);
			player.setIsParalyzed(false);
		}
		
		if (p1 != null)
		    p1.broadcastUserInfo();
		if (p2 != null)
			p2.broadcastUserInfo();
		if (p3 != null)
			p3.broadcastUserInfo();
		
		sendMsg();
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
		sb.append("<html><body><table width=270><tr><td width=200>Event Engine </td><td><a action=\"bypass -h eventstats 1\">Statistics</a></td></tr></table><br><center><table width=270 bgcolor=5A5A5A><tr><td width=70>Running</td><td width=130><center>" + getString("eventName") + "</td><td width=70>Time: " + clock.getTime() + "</td></tr></table><table width=270><tr><td><center><font color=" + teams.get(1).getHexaColor() + ">" + teams.get(1).getScore() + "</font> - " + "<font color=" + teams.get(2).getHexaColor() + ">" + teams.get(2).getScore() + "</font></td></tr></table><br><table width=270>");
		sb.append("</table></body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	@Override
	protected void start()
	{
		fighters.put(1, new CopyOnWriteArrayList<L2PcInstance>());
		fighters.put(2, new CopyOnWriteArrayList<L2PcInstance>());
		if (players.size() < 9)
			eventType = 1;
		else if (players.size() < 15)
			eventType = 2;
		else
			eventType = 3;
		round = 1;
		setStatus(EventState.START);
		schedule(1);
	}
	
	@Override 
	public void onLogout(L2PcInstance player) 
	{ 
		super.onLogout(player);
		
		int team = getTeam(player);
		
		fighters.get(team).remove(player);
		
		if (fighters.get(team).size() == 0)
			selectNewPlayersOfTeam(team);
	}
	
	@Override
	protected boolean canAttack(L2PcInstance player, L2PcInstance target)
	{
		return true;
	}
	
	@Override
	protected String getStartingMsg()
	{
		return "Round " + round + " has started, kill your enemies!";
	}
}
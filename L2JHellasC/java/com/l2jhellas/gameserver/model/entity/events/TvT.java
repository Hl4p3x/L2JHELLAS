package com.l2jhellas.gameserver.model.entity.events;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.Event;
import com.l2jhellas.gameserver.model.entity.events.engines.EventManager;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class TvT extends Event
{
	protected EventState eventState;
	private Core task = new Core();
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
						closeAllDoors();
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
						
						setStatus(EventState.INACTIVE);
						
						if (winnerTeam == 0)
							EventManager.getInstance().end("The event ended in a tie! both teams had " + teams.get(1).getScore() + " kills!");
						else
						{
							giveReward(getPlayersOfTeam(winnerTeam), getInt("rewardId"), getInt("rewardAmmount"));
							EventManager.getInstance().end("Congratulation! The " + teams.get(winnerTeam).getName() + " team won the event with " + teams.get(winnerTeam).getScore() + " kills!");
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
	
	public TvT()
	{
		super();
		eventId = 5;
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
		addToResurrector(victim);
	}
	
	@Override
	public void onKill(L2Character victim, L2PcInstance killer)
	{
		super.onKill(victim, killer);
		if (getPlayersTeam(killer) != getPlayersTeam((L2PcInstance) victim))
		{
			getPlayersTeam(killer).increaseScore();
			increasePlayersScore(killer);
		}
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
		sb.append("<html><body><table width=270><tr><td width=200>Event Engine </td><td><a action=\"bypass -h eventstats 1\">Statistics</a></td></tr></table><br><center><table width=270 bgcolor=000000><tr><td width=70>Running</td><td width=130><center>" + getString("eventName") + "</td><td width=70>Time: " + clock.getTime() + "</td></tr></table><center><table width=270><tr><td><center><font color=" + teams.get(1).getHexaColor() + ">" + teams.get(1).getScore() + "</font> - " + "<font color=" + teams.get(2).getHexaColor() + ">" + teams.get(2).getScore() + "</font></td></tr></table><br><table width=270>");
		sb.append("</table></body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
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
		return "Go and kill your enemies!";
	}
}
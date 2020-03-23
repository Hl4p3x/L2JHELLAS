package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.olympiad.Olympiad;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameManager;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameTask;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.util.StringUtil;

public final class RequestOlympiadMatchList extends L2GameClientPacket
{
	private static final String _C__D0_13_REQUESTOLYMPIADMATCHLIST = "[C] D0:13 RequestOlympiadMatchList";
	
	@Override
	protected void readImpl()
	{
		// trigger packet
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null || !activeChar.inObserverMode())
			return;
		
		int is = 0;
		final StringBuilder sb = new StringBuilder(1500);
		
		for (OlympiadGameTask task : OlympiadGameManager.getInstance().getOlympiadTasks())
		{
			StringUtil.append(sb, "<tr><td fixwidth=10><a action=\"bypass arenachange ", is, "\">", ++is, "</a></td><td fixwidth=80>");
			
			if (task.isGameStarted())
			{
				if (task.isInTimerTime())
					StringUtil.append(sb, "&$907;");
				
				else if (task.isBattleStarted())
					StringUtil.append(sb, "&$829;");
				
				else
					StringUtil.append(sb, "&$908;");
				
				StringUtil.append(sb, "</td><td>", task.getGame().getPlayerNames()[0], "&nbsp; / &nbsp;", task.getGame().getPlayerNames()[1]);
			}
			else
				StringUtil.append(sb, "&$906;", "</td><td>&nbsp;"); // Initial State
				
			StringUtil.append(sb, "</td><td><font color=\"aaccff\"></font></td></tr>");
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "olympiad_arena_observe_list.htm");
		html.replace("%list%", sb.toString());
		
		activeChar.sendPacket(html);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_13_REQUESTOLYMPIADMATCHLIST;
	}
}
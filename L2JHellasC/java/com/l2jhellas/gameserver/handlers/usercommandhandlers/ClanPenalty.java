package com.l2jhellas.gameserver.handlers.usercommandhandlers;

import java.text.SimpleDateFormat;

import com.l2jhellas.gameserver.handler.IUserCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class ClanPenalty implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		100
	};
	
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;
		
		boolean penalty = false;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder htmlContent = new StringBuilder("<html><body>");
		htmlContent.append("<center><table width=270 border=0 bgcolor=111111>");
		htmlContent.append("<tr><td width=170>Penalty</td>");
		htmlContent.append("<td width=100 align=center>Expiration Date</td></tr>");
		htmlContent.append("</table><table width=270 border=0><tr>");
		
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			htmlContent.append("<td width=170>Unable to join a clan.</td>");
			htmlContent.append("<td width=100 align=center>" + format.format(activeChar.getClanJoinExpiryTime()) + "</td>");
			penalty = true;
		}
		if (activeChar.getClanCreateExpiryTime() > System.currentTimeMillis())
		{
			htmlContent.append("<td width=170>Unable to create a clan.</td>");
			htmlContent.append("<td width=100 align=center>" + format.format(activeChar.getClanCreateExpiryTime()) + "</td>");
			penalty = true;
		}
		if (!penalty)
		{
			htmlContent.append("<td width=170>No penalty is imposed.</td>");
			htmlContent.append("<td width=100 align=center> </td>");
		}
		
		htmlContent.append("</tr></table><img src=\"L2UI.SquareWhite\" width=270 height=1>");
		htmlContent.append("</center></body></html>");
		
		NpcHtmlMessage penaltyHtml = new NpcHtmlMessage(0);
		penaltyHtml.setHtml(htmlContent.toString());
		activeChar.sendPacket(penaltyHtml);
		
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
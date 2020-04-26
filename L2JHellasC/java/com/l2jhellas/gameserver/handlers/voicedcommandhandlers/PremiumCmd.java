package com.l2jhellas.gameserver.handlers.voicedcommandhandlers;

import java.text.SimpleDateFormat;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.handler.IVoicedCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class PremiumCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"premium"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith(VOICED_COMMANDS[0]))
		{
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			if (activeChar.getPremiumService() == 0)
			{
				NpcHtmlMessage preReply = new NpcHtmlMessage(5);
				StringBuilder html3 = new StringBuilder("<html><body><title>Normal Account</title><center>");
				html3.append("<table>");
				html3.append("<tr><td><center>Your account :<font color=\"LEVEL\">Normal<br></font></td></tr>");
				html3.append("<tr><td><center>Details<br1></td></tr>");
				html3.append("<tr><td>Rate EXP: <font color=\"LEVEL\">" + Config.RATE_XP + "<br1></font></td></tr>");
				html3.append("<tr><td>Rate SP: <font color=\"LEVEL\">" + Config.RATE_SP + "<br1></font></td></tr>");
				html3.append("<tr><td>Rate Spoil: <font color=\"LEVEL\">" + Config.RATE_DROP_SPOIL + "<br1></font></td></tr><br>");
				html3.append("<tr><td>Expires : <font color=\"00A5FF\"> Never (Normal Account)<br1></font></td></tr>");
				html3.append("<tr><td>Current Date : <font color=\"70FFCA\"> :" + String.valueOf(format.format(System.currentTimeMillis())) + " <br><br></font></td></tr><br><br1><br1>");
				html3.append("<tr><td><font color=\"LEVEL\"><center>Premium Info & Rules<br1></font></td></tr>");
				html3.append("<tr><td>Upgrade to Premium Account :<br1></td></tr>");
				html3.append("<tr><td>Premium Account : <font color=\"70FFCA\"> Benefits<br1></font></td></tr>");
				html3.append("<tr><td>Rate EXP: <font color=\"LEVEL\"> " + Config.PREMIUM_RATE_XP + " (Account Premium )<br1></font></td></tr>");
				html3.append("<tr><td>Rate SP: <font color=\"LEVEL\"> " + Config.PREMIUM_RATE_SP + " (Account Premium )<br1></font></td></tr>");
				html3.append("<tr><td>Drop Spoil Rate: <font color=\"LEVEL\"> " + Config.PREMIUM_RATE_DROP_SPOIL + " (Account Premium )<br1></font></td></tr>");
				html3.append("<tr><td> <font color=\"70FFCA\">1.Premium  benefits CAN NOT BE TRANSFERED.<br1></font></td></tr><br>");
				html3.append("<tr><td> <font color=\"70FFCA\">2.Premium benefits effect ALL characters in same account.<br1></font></td></tr><br>");
				html3.append("<tr><td> <font color=\"70FFCA\">3.Does not effect Party members.</font></td></tr>");
				html3.append("</table>");
				html3.append("</center></body></html>");
				
				preReply.setHtml(html3.toString());
				activeChar.sendPacket(preReply);
			}
			else
			{
				long _end_prem_date = 0L;
				_end_prem_date = activeChar.getPremServiceData();
				NpcHtmlMessage preReply = new NpcHtmlMessage(5);
				
				StringBuilder html3 = new StringBuilder("<html><body><title>Premium Account Details</title><center>");
				html3.append("<table>");
				html3.append("<tr><td><center>Thank you for supporting YOUR server.<br><br></td></tr>");
				html3.append("<tr><td><center>Your account : <font color=\"LEVEL\">Premium<br></font></td></tr>");
				html3.append("<tr><td><center>Details<br1></center></td></tr>");
				html3.append("<tr><td>Rate EXP: <font color=\"LEVEL\"> x" + Config.PREMIUM_RATE_XP + " <br1></font></td></tr>");
				html3.append("<tr><td>Rate SP: <font color=\"LEVEL\"> x" + Config.PREMIUM_RATE_SP + "  <br1></font></td></tr>");
				html3.append("<tr><td>Rate Spoil: <font color=\"LEVEL\"> x" + Config.PREMIUM_RATE_DROP_SPOIL + " <br1></font></td></tr>");
				html3.append("<tr><td>Expires : <font color=\"00A5FF\"> " + String.valueOf(format.format(_end_prem_date)) + " (Premium added)</font></td></tr>");
				html3.append("<tr><td>Current Date : <font color=\"70FFCA\"> :" + String.valueOf(format.format(System.currentTimeMillis())) + " <br><br></font></td></tr>");
				html3.append("<tr><td><font color=\"LEVEL\"><center>Premium Info & Rules<br1></font></center></td></tr>");
				html3.append("<tr><td><font color=\"70FFCA\">1.Premium Account CAN NOT BE TRANSFERED.<br1></font></td></tr>");
				html3.append("<tr><td><font color=\"70FFCA\">2.Premium Account effects ALL characters in same account.<br1></font></td></tr>");
				html3.append("<tr><td><font color=\"70FFCA\">3.Does not effect Party members.<br><br></font></td></tr>");
				html3.append("</table>");
				html3.append("</center></body></html>");
				
				preReply.setHtml(html3.toString());
				activeChar.sendPacket(preReply);
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
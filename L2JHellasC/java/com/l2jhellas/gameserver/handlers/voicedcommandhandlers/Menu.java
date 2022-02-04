package com.l2jhellas.gameserver.handlers.voicedcommandhandlers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.handler.IVoicedCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author AbsolutePower
 */
public class Menu implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"menu"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith(VOICED_COMMANDS[0]))
			ShowMain(activeChar);
		return true;
	}
	
    public static void ShowMain(L2PcInstance player)
    {
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/Menu.htm");
		html.replace("%pmrefusal%", player.getMessageRefusal() ? Config.MENU_ACTIVATED_ICON : Config.MENU_DEACTIVATED_ICON);
		html.replace("%traderefusal%", player.getTradeRefusal() ? Config.MENU_ACTIVATED_ICON : Config.MENU_DEACTIVATED_ICON);
		html.replace("%partyrefusal%", player.getPartyRefusal() ? Config.MENU_ACTIVATED_ICON : Config.MENU_DEACTIVATED_ICON);
		html.replace("%ssrefusal%", player.getSSRefusal() ? Config.MENU_ACTIVATED_ICON : Config.MENU_DEACTIVATED_ICON);
		player.sendPacket(html);
    }
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
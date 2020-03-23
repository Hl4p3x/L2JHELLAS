package com.l2jhellas.gameserver.handlers.voicedcommandhandlers;

import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.handler.IVoicedCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoiceInfoCmd implements IVoicedCommandHandler
{
	private static String[] VOICED_COMMANDS =
	{
		"info"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		String htmFile = "data/html/mods/VoicedInfo.htm";
		String htmContent = HtmCache.getInstance().getHtm(htmFile);
		NpcHtmlMessage infoHtml = new NpcHtmlMessage(1);
		infoHtml.setHtml(htmContent);
		activeChar.sendPacket(infoHtml);
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
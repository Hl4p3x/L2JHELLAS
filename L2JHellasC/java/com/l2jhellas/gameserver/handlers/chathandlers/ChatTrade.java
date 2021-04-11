package com.l2jhellas.gameserver.handlers.chathandlers;

import java.util.Collection;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.handler.IChatHandler;
import com.l2jhellas.gameserver.model.BlockList;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;

public class ChatTrade implements IChatHandler
{
	private static final ChatType[] COMMAND_IDS =
	{
		ChatType.TRADE
	};
	
	@Override
	public void handleChat(ChatType type, L2PcInstance activeChar, String target, String text)
	{
		if (!FloodProtectors.performAction(activeChar.getClient(), FloodAction.TRADE_CHAT))
			return;
		
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		
		Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
		
		if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on") || (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm") && activeChar.isGM()))
		{
			for (L2PcInstance player : pls)
				if (!BlockList.isBlocked(player, activeChar))
					player.sendPacket(cs);
		}
		else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("limited"))
		{
			int region = MapRegionTable.getMapRegion(activeChar.getX(), activeChar.getY());
			for (L2PcInstance player : pls)
				if (region == MapRegionTable.getMapRegion(player.getX(), player.getY()) && !BlockList.isBlocked(player, activeChar) && player.getInstanceId() == activeChar.getInstanceId())
					player.sendPacket(cs);
		}
	}
	
	@Override
	public ChatType[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
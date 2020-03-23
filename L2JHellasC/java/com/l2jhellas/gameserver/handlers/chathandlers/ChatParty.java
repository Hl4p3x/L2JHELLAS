package com.l2jhellas.gameserver.handlers.chathandlers;

import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.handler.IChatHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;

public class ChatParty implements IChatHandler
{
	private static final ChatType[] COMMAND_IDS =
	{
		ChatType.PARTY
	};
	
	@Override
	public void handleChat(ChatType type, L2PcInstance activeChar, String target, String text)
	{
		if (activeChar.isInParty())
		{
			CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
			
			activeChar.getParty().broadcastCSToPartyMembers(cs, activeChar);
		}
	}
	
	@Override
	public ChatType[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
package com.l2jhellas.gameserver.handlers.chathandlers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.handler.IChatHandler;
import com.l2jhellas.gameserver.model.BlockList;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class ChatTell implements IChatHandler
{
	private static final ChatType[] COMMAND_IDS =
	{
		ChatType.WHISPER
	};
	
	@Override
	public void handleChat(ChatType type, L2PcInstance activeChar, String target, String text)
	{
		
		// Return if player is chat banned
		if (activeChar.isChatBanned())
		{
			activeChar.sendMessage("You are currently banned from chat.");
			return;
		}
		
		// return if player is in jail
		if (Config.JAIL_DISABLE_CHAT && activeChar.isInJail())
		{
			activeChar.sendMessage("You are currently in jail and cannot chat.");
			return;
		}
		
		// Return if no target is set
		if (target == null)
			return;
		
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		L2PcInstance receiver = null;
		
		receiver = L2World.getInstance().getPlayer(target);
		
		if (receiver != null && !BlockList.isBlocked(receiver, activeChar))
		{
			if (Config.JAIL_DISABLE_CHAT && receiver.isInJail())
			{
				activeChar.sendMessage("Player is in jail.");
				return;
			}
			
			if (receiver.isChatBanned())
			{
				activeChar.sendMessage("Player is chat banned.");
				return;
			}
			if (!receiver.getMessageRefusal())
			{
				receiver.sendPacket(cs);
				activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), type, "->" + receiver.getName(), text));
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
			}
		}
		else
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_ONLINE);
			sm.addString(target);
			activeChar.sendPacket(sm);
			sm = null;
		}
	}
	
	@Override
	public ChatType[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
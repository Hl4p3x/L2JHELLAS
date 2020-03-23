package com.l2jhellas.gameserver.handlers.chathandlers;

import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.handler.IChatHandler;
import com.l2jhellas.gameserver.model.BlockList;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;

public class ChatHeroVoice implements IChatHandler
{
	private static final ChatType[] COMMAND_IDS =
	{
		ChatType.HERO_VOICE
	};
	
	@Override
	public void handleChat(ChatType type, L2PcInstance activeChar, String target, String text)
	{
		if (activeChar.isHero() || activeChar.isGM())
		{
			if (!FloodProtectors.performAction(activeChar.getClient(), Action.HERO_VOICE))
				return;
			
			CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
			for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				if (!BlockList.isBlocked(player, activeChar))
					player.sendPacket(cs);
		}
	}
	
	@Override
	public ChatType[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
package com.l2jhellas.gameserver.handlers.chathandlers;

import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.handler.IChatHandler;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;


public class ChatPartyMatch implements IChatHandler
{
	private static final ChatType[] COMMAND_IDS =
	{
		ChatType.PARTYMATCH_ROOM
	};
	
	@Override
	public void handleChat(ChatType type, L2PcInstance activeChar, String target, String text)
	{
		if (!activeChar.isInPartyMatchRoom())
			return;
		
		final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(activeChar.getPartyRoom());
		if (room == null)
			return;

		room.broadcastPacket(new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text));
	}

	@Override
	public ChatType[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
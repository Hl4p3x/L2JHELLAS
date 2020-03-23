package com.l2jhellas.gameserver.handlers.usercommandhandlers;

import com.l2jhellas.gameserver.handler.IUserCommandHandler;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class PartyInfo implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		81
	};
	
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;
		
		if (!activeChar.isInParty())
		{
			SystemMessage sm = SystemMessage.sendString("You are not in a party.");
			activeChar.sendPacket(sm);
			return false;
		}
		
		L2Party playerParty = activeChar.getParty();
		int memberCount = playerParty.getMemberCount();
		String partyLeader = playerParty.getPartyMembers().get(0).getName();
		
		activeChar.sendPacket(SystemMessageId.PARTY_INFORMATION);

		switch (playerParty.getDistributionType())
		{
			case FINDERS_KEEPERS:
				activeChar.sendPacket(SystemMessageId.LOOTING_FINDERS_KEEPERS);
				break;
			case RANDOM:
				activeChar.sendPacket(SystemMessageId.LOOTING_RANDOM);
				break;
			case RANDOM_INCLUDING_SPOIL:
				activeChar.sendPacket(SystemMessageId.LOOTING_RANDOM_INCLUDE_SPOIL);
				break;
			case BY_TURN:
				activeChar.sendPacket(SystemMessageId.LOOTING_BY_TURN);
				break;
			case BY_TURN_INCLUDING_SPOIL:
				activeChar.sendPacket(SystemMessageId.LOOTING_BY_TURN_INCLUDE_SPOIL);
				break;
		}
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PARTY_LEADER_S1);
		sm.addString(partyLeader);
		activeChar.sendPacket(sm);
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
		sm.addString("Members: " + memberCount + "/9");
		
		activeChar.sendPacket(SystemMessageId.WAR_LIST);
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
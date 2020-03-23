package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.instancemanager.DuelManager;
import com.l2jhellas.gameserver.model.actor.group.party.L2CommandChannel;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestDuelAnswerStart extends L2GameClientPacket
{
	private static final String _C__D0_28_REQUESTDUELANSWERSTART = "[C] D0:28 RequestDuelAnswerStart";
	
	private int _partyDuel;
	@SuppressWarnings("unused")
	private int _unk1;
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_partyDuel = readD();
		_unk1 = readD();
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2PcInstance requestor = activeChar.getActiveRequester();
		
		if (requestor == null)
			return;
		
		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
		
		if (_response == 1)
		{
			if (!requestor.canDuel())
			{
				activeChar.sendPacket(requestor.getNoDuelReason());
				return;
			}
			
			if (!activeChar.canDuel())
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
				return;
			}
			
			if (!requestor.isInsideRadius(activeChar, 2000, false, false))
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY).addCharName(requestor));
				return;
			}
			
			if (_partyDuel == 1)
			{
				final L2Party requestorParty = requestor.getParty();
				if (requestorParty == null || !requestorParty.isLeader(requestor) || requestorParty.containsPlayer(activeChar))
				{
					activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
					return;
				}
				
				final L2Party activeCharParty = activeChar.getParty();
				if (activeCharParty == null)
				{
					activeChar.sendPacket(SystemMessageId.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
					return;
				}
				
				for (L2PcInstance member : requestorParty.getPartyMembers())
				{
					if (member != requestor && !member.canDuel())
					{
						activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
						return;
					}
				}
				
				for (L2PcInstance member : activeCharParty.getPartyMembers())
				{
					if (member != activeChar && !member.canDuel())
					{
						activeChar.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL);
						return;
					}
				}
				
				final L2CommandChannel requestorChannel = requestorParty.getCommandChannel();
				if (requestorChannel != null)
					requestorChannel.removeParty(requestorParty);
				
				final L2CommandChannel activeCharChannel = activeCharParty.getCommandChannel();
				if (activeCharChannel != null)
					activeCharChannel.removeParty(activeCharParty);
				
				for (L2PcInstance member : requestorParty.getPartyMembers())
					member.removeMeFromPartyMatch();
				
				for (L2PcInstance member : activeCharParty.getPartyMembers())
					member.removeMeFromPartyMatch();
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(requestor));
				requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(activeChar));
			}
			else
			{
				activeChar.removeMeFromPartyMatch();
				requestor.removeMeFromPartyMatch();
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_S1_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(requestor));
				requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS).addCharName(activeChar));
			}
			
			DuelManager.getInstance().addDuel(requestor, activeChar, _partyDuel == 1);
		}
		else
		{
			if (_partyDuel == 1)
				requestor.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
			else
				requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL).addCharName(activeChar));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_28_REQUESTDUELANSWERSTART;
	}
}
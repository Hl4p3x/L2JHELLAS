package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.group.party.L2CommandChannel;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExDuelAskStart;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestDuelStart extends L2GameClientPacket
{
	private static final String _C__D0_27_REQUESTDUELSTART = "[C] D0:27 RequestDuelStart";
	private String _player;
	private int _partyDuel;
	
	@Override
	protected void readImpl()
	{
		_player = readS();
		_partyDuel = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final L2PcInstance targetChar = L2World.getInstance().getPlayer(_player);
		if (targetChar == null || activeChar == targetChar)
		{
			activeChar.sendPacket(SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL);
			return;
		}
		
		if (!activeChar.canDuel())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
			return;
		}
		
		if (!targetChar.canDuel())
		{
			activeChar.sendPacket(targetChar.getNoDuelReason());
			return;
		}
		
		if (!activeChar.isInsideRadius(targetChar, 2000, false, false))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY).addCharName(targetChar));
			return;
		}
		
		if (_partyDuel == 1)
		{
			final L2Party activeCharParty = activeChar.getParty();
			if (activeCharParty == null || !activeCharParty.isLeader(activeChar) || activeCharParty.containsPlayer(targetChar))
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
				return;
			}
			
			final L2Party targetCharParty = targetChar.getParty();
			if (targetCharParty == null)
			{
				activeChar.sendPacket(SystemMessageId.SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY);
				return;
			}
			
			for (L2PcInstance member : activeCharParty.getPartyMembers())
			{
				if (member != activeChar && !member.canDuel())
				{
					activeChar.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
					return;
				}
			}
			
			for (L2PcInstance member : targetCharParty.getPartyMembers())
			{
				if (member != targetChar && !member.canDuel())
				{
					activeChar.sendPacket(SystemMessageId.THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL);
					return;
				}
			}
			
			final L2PcInstance partyLeader = targetCharParty.getLeader();
			
			if (!partyLeader.isProcessingRequest())
			{
				final L2CommandChannel activeCharChannel = activeCharParty.getCommandChannel();
				if (activeCharChannel != null)
					activeCharChannel.removeParty(activeCharParty);
				
				final L2CommandChannel targetCharChannel = targetCharParty.getCommandChannel();
				if (targetCharChannel != null)
					targetCharChannel.removeParty(targetCharParty);
				
				for (L2PcInstance member : activeCharParty.getPartyMembers())
					member.removeMeFromPartyMatch();
				
				for (L2PcInstance member : targetCharParty.getPartyMembers())
					member.removeMeFromPartyMatch();
				
				activeChar.onTransactionRequest(partyLeader);
				partyLeader.sendPacket(new ExDuelAskStart(activeChar.getName(), _partyDuel));
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_HAS_BEEN_CHALLENGED_TO_A_DUEL).addCharName(partyLeader));
				targetChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL).addCharName(activeChar));
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(partyLeader));
		}
		else
		{
			if (!targetChar.isProcessingRequest())
			{
				activeChar.removeMeFromPartyMatch();
				targetChar.removeMeFromPartyMatch();
				
				activeChar.onTransactionRequest(targetChar);
				targetChar.sendPacket(new ExDuelAskStart(activeChar.getName(), _partyDuel));
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_CHALLENGED_TO_A_DUEL).addCharName(targetChar));
				targetChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_CHALLENGED_YOU_TO_A_DUEL).addCharName(activeChar));
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(targetChar));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_27_REQUESTDUELSTART;
	}
}
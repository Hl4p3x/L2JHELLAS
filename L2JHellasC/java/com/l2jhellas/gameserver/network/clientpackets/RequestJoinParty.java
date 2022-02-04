package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.player.PartyLootType;
import com.l2jhellas.gameserver.model.BlockList;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AskJoinParty;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinParty extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestJoinParty.class.getName());
	private static final String _C__29_REQUESTJOINPARTY = "[C] 29 RequestJoinParty";
	
	private String _name;
	private int _itemDistribution;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_itemDistribution = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance requestor = getClient().getActiveChar();
		if (requestor == null)
			return;
		
		final L2PcInstance target = L2World.getInstance().getPlayer(_name);
		if (target == null)
		{
			requestor.sendPacket(SystemMessageId.FIRST_SELECT_USER_TO_INVITE_TO_PARTY);
			return;
		}
		
		if (BlockList.isBlocked(target, requestor))
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addPcName(target));
			return;
		}
		
		if (target.equals(requestor) || target.isCursedWeaponEquiped() || requestor.isCursedWeaponEquiped() || !target.getAppearance().isVisible())
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}

		if (target.getPartyRefusal())
		{
			requestor.sendMessage("You can't invite that player because he is in party refusal mode.");
			return;
		}
		
		if (target.isInParty())
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ALREADY_IN_PARTY).addPcName(target));
			return;
		}
		
		if (target.getClient().isDetached())
		{
			requestor.sendMessage("The player you tried to invite is in offline mode.");
			return;
		}
		
		if (target.isInJail() || requestor.isInJail())
		{
			requestor.sendMessage("The player you tried to invite is currently jailed.");
			return;
		}
		
		if (target.isInOlympiadMode() || requestor.isInOlympiadMode())
			return;
		
		if (!requestor.isInParty())
			createNewParty(target, requestor);
		else
		{
			if (!requestor.getParty().isInDimensionalRift())
				addTargetToParty(target, requestor);
		}
	}
	
	private static void addTargetToParty(L2PcInstance target, L2PcInstance requestor)
	{
		final L2Party party = requestor.getParty();
		if (party == null)
			return;
		
		if (!party.isLeader(requestor))
		{
			requestor.sendPacket(SystemMessageId.ONLY_LEADER_CAN_INVITE);
			return;
		}
		
		if (party.getMemberCount() >= 9)
		{
			requestor.sendPacket(SystemMessageId.PARTY_FULL);
			return;
		}
		
		if (party.getPendingInvitation() && !party.isInvitationRequestExpired())
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		
		if (!target.isProcessingRequest())
		{
			requestor.onTransactionRequest(target);
			
			// in case a leader change has happened, use party's mode
			target.sendPacket(new AskJoinParty(requestor.getName(), party.getDistributionType()));
			party.setPendingInvitation(true);
			
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY).addPcName(target));
			if (Config.DEBUG)
				_log.warning(RequestJoinParty.class.getSimpleName() + ": Sent out a party invitation to " + target.getName());
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addPcName(target));
			if (Config.DEBUG)
				_log.warning(RequestJoinParty.class.getName() + ": " + requestor.getName() + " already received a party invitation");
		}
	}
	
	private void createNewParty(L2PcInstance target, L2PcInstance requestor)
	{
		final PartyLootType DistributionType = PartyLootType.GetById(_itemDistribution);
		
		if (DistributionType == null)
			return;

		if (!target.isProcessingRequest())
		{
			requestor.setParty(new L2Party(requestor, DistributionType));		
			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(),DistributionType));
			requestor.getParty().setPendingInvitation(true);		
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_INVITED_S1_TO_PARTY).addPcName(target));
		}
		else
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
	}
	
	@Override
	public String getType()
	{
		return _C__29_REQUESTJOINPARTY;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchWaitingList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jhellas.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jhellas.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class AnswerJoinPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_15_ANSWERJOINPARTYROOM = "[C] D0:15 AnswerJoinPartyRoom";
	
	private int _requesterID;
	
	@Override
	protected void readImpl()
	{
		_requesterID = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		final L2PcInstance partner = player.getActiveRequester();
		if (partner == null || L2World.getInstance().getPlayer(partner.getObjectId()) == null)
		{
			// Partner hasn't be found, cancel the invitation
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.setActiveRequester(null);
			return;
		}
		
		// If answer is positive, join the requester's PartyRoom.
		if (_requesterID == 1 && !partner.isRequestExpired())
		{
			PartyMatchRoom _room = PartyMatchRoomList.getInstance().getRoom(partner.getPartyRoom());
			if (_room == null)
				return;
			
			if ((player.getLevel() >= _room.getMinLvl()) && (player.getLevel() <= _room.getMaxLvl()))
			{
				// Remove from waiting list
				PartyMatchWaitingList.getInstance().removePlayer(player);
				
				player.setPartyRoom(partner.getPartyRoom());
				
				player.sendPacket(new PartyMatchDetail(_room));
				player.sendPacket(new ExPartyRoomMember(_room, 0));
				
				for (L2PcInstance _member : _room.getPartyMembers())
				{
					if (_member == null)
						continue;
					
					_member.sendPacket(new ExManagePartyRoomMember(player, _room, 0));
					_member.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ENTERED_PARTY_ROOM).addPcName(player));
				}
				_room.addMember(player);
				
				// Info Broadcast
				player.broadcastUserInfo();
			}
			else
				player.sendPacket(SystemMessageId.CANT_ENTER_PARTY_ROOM);
		}
		// Else, send a message to requester.
		else
			partner.sendPacket(SystemMessageId.PARTY_MATCHING_REQUEST_NO_RESPONSE);
		
		// reset transaction timers
		player.setActiveRequester(null);
		partner.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__D0_15_ANSWERJOINPARTYROOM;
	}
}
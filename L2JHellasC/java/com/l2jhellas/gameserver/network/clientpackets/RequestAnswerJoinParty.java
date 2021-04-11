package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jhellas.gameserver.network.serverpackets.JoinParty;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerJoinParty extends L2GameClientPacket
{
	private static final String _C__2A_REQUESTANSWERPARTY = "[C] 2A RequestAnswerJoinParty";
	
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player != null)
		{
			final L2PcInstance requestor = player.getActiveRequester();
			if (requestor == null)
				return;
			
			final JoinParty join = new JoinParty(_response);
			requestor.sendPacket(join);
			
			if (_response == 1)
			{
				if (requestor.isInParty())
				{
					if (requestor.getParty().getMemberCount() >= 9)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PARTY_FULL);
						player.sendPacket(sm);
						requestor.sendPacket(sm);
						return;
					}
				}
				player.joinParty(requestor.getParty());
				
				if (requestor.isInPartyMatchRoom() && player.isInPartyMatchRoom())
				{
					final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
					if (list != null && (list.getPlayerRoomId(requestor) == list.getPlayerRoomId(player)))
					{
						final PartyMatchRoom room = list.getPlayerRoom(requestor);
						if (room != null)
						{
							final ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
							for (L2PcInstance member : room.getPartyMembers())
							{
								if (member != null)
									member.sendPacket(packet);
							}
						}
					}
				}
				else if (requestor.isInPartyMatchRoom() && !player.isInPartyMatchRoom())
				{
					final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
					if (list != null)
					{
						final PartyMatchRoom room = list.getPlayerRoom(requestor);
						if (room != null)
						{
							room.addMember(player);
							ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
							for (L2PcInstance member : room.getPartyMembers())
							{
								if (member != null)
									member.sendPacket(packet);
							}
							player.setPartyRoom(room.getId());
							player.broadcastUserInfo();
						}
					}
				}
			}
			else
			{
				requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PLAYER_DECLINED));

				// activate garbage collection if there are no other members in party (happens when we were creating new one)
				if (requestor.getParty() != null && requestor.getParty().getMemberCount() == 1)
					requestor.setParty(null);
			}
			if (requestor.getParty() != null)
				requestor.getParty().setPendingInvitation(false); // if party is null, there is no need of decreasing
				
			player.setActiveRequester(null);
			requestor.onTransactionResponse();
			requestor.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__2A_REQUESTANSWERPARTY;
	}
}
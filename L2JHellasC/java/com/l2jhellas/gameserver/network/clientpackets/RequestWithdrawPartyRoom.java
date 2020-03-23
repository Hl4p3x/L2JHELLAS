package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExClosePartyRoom;

public final class RequestWithdrawPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_02_REQUESTWITHDRAWPARTYROOM = "[C] D0:02 RequestWithdrawPartyRoom";
	
	private int _roomid;
	
	// @SuppressWarnings("unused")
	// private int _unk1;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD();
		// _unk1 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_roomid);
		
		if (room == null || room.getId() != _roomid)
			return;
		
		if ((activeChar.isInParty() && room.getOwner().isInParty()) && (activeChar.getParty().getPartyLeaderOID() == room.getOwner().getParty().getPartyLeaderOID()))
		{
		}
		else
		{
			room.deleteMember(activeChar);
			activeChar.setPartyRoom(0);
			activeChar.broadcastUserInfo();
			
			activeChar.sendPacket(ExClosePartyRoom.STATIC_PACKET);
			activeChar.sendPacket(SystemMessageId.PARTY_ROOM_EXITED);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_02_REQUESTWITHDRAWPARTYROOM;
	}
}
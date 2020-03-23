package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RequestDismissPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_02_REQUESTDISMISSPARTYROOM = "[C] D0:02 RequestDismissPartyRoom";
	private int _roomid;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_roomid);
		
		if (room == null)
			return;
		
		PartyMatchRoomList.getInstance().deleteRoom(_roomid);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_02_REQUESTDISMISSPARTYROOM;
	}
}
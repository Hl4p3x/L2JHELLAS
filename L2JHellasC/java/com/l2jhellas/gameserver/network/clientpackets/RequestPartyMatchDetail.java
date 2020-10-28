package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchWaitingList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jhellas.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jhellas.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestPartyMatchDetail extends L2GameClientPacket
{
	private static final String _C__71_REQUESTPARTYMATCHDETAIL = "[C] 71 RequestPartyMatchDetail";

	private int _roomid;
	private int _location;
	private int _level;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD(); // room id > 0 = player select room otherwise autojoin condition AbsolutePower.
		_location = readD(); // -1 all , -2 near me . AbsolutePower.
		_level = readD();  // 1 = all level range , 0 my level range AbsolutePower.
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		PartyMatchRoom room;

		if (_roomid > 0)
			room = PartyMatchRoomList.getInstance().getRoom(_roomid);
		else
			room = PartyMatchRoomList.getInstance().getAvailableRoom(activeChar,_location,_level == 1);
		
		if (room == null || !room.allowToEnter(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.CANT_ENTER_PARTY_ROOM);
			return;
		}

		PartyMatchWaitingList.getInstance().removePlayer(activeChar);

		activeChar.setPartyRoom(_roomid);

		activeChar.sendPacket(new PartyMatchDetail(room));
		activeChar.sendPacket(new ExPartyRoomMember(room, 0));

		for (L2PcInstance member : room.getPartyMembers())
		{
			if (member == null)
				continue;

			member.sendPacket(new ExManagePartyRoomMember(activeChar, room, 0));
			member.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ENTERED_PARTY_ROOM).addPcName(activeChar));
		}
		room.addMember(activeChar);

		activeChar.broadcastUserInfo();	
	}
	
	@Override
	public String getType()
	{
		return _C__71_REQUESTPARTYMATCHDETAIL;
	}
}
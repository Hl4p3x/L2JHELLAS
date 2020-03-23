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
	@SuppressWarnings("unused")
	private int _unk1;
	@SuppressWarnings("unused")
	private int _unk2;
	@SuppressWarnings("unused")
	private int _unk3;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD();
		
		_unk1 = readD();
		_unk2 = readD();
		_unk3 = readD();
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
		
		if ((activeChar.getLevel() >= room.getMinLvl()) && (activeChar.getLevel() <= room.getMaxLvl()))
		{
			// Remove from waiting list
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
			
			// Info Broadcast
			activeChar.broadcastUserInfo();
		}
		else
			activeChar.sendPacket(SystemMessageId.CANT_ENTER_PARTY_ROOM);
	}
	
	@Override
	public String getType()
	{
		return _C__71_REQUESTPARTYMATCHDETAIL;
	}
}
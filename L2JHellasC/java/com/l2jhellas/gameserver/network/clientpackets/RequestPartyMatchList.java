package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchWaitingList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jhellas.gameserver.network.serverpackets.PartyMatchDetail;

public class RequestPartyMatchList extends L2GameClientPacket
{
	private static final String _C__70_REQUESTPARTYMATCHLIST = "[C] 70 RequestPartyMatchList";
	
	private int _roomid;
	private int _membersmax;
	private int _lvlmin;
	private int _lvlmax;
	private int _loot;
	private String _roomtitle;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD();
		_membersmax = readD();
		_lvlmin = readD();
		_lvlmax = readD();
		_loot = readD();
		_roomtitle = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (_roomid > 0)
		{
			PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_roomid);
			if (room != null)
			{
				room.setMaxMembers(_membersmax);
				room.setMinLvl(_lvlmin);
				room.setMaxLvl(_lvlmax);
				room.setLootType(_loot);
				room.setTitle(_roomtitle);
				
				for (L2PcInstance member : room.getPartyMembers())
				{
					if (member == null)
						continue;
					
					member.sendPacket(new PartyMatchDetail(room));
					member.sendPacket(SystemMessageId.PARTY_ROOM_REVISED);
				}
			}
		}
		else
		{
			int maxid = PartyMatchRoomList.getInstance().getNewId();
			
			PartyMatchRoom room = new PartyMatchRoom(maxid, _roomtitle, _loot, _lvlmin, _lvlmax, _membersmax, activeChar);
						
			// Remove from waiting list, and add to current room
			PartyMatchWaitingList.getInstance().removePlayer(activeChar);
			PartyMatchRoomList.getInstance().addPartyMatchRoom(maxid, room);
			
			if (activeChar.isInParty())
			{
				for (L2PcInstance ptmember : activeChar.getParty().getPartyMembers())
				{
					if (ptmember == null || ptmember == activeChar)
						continue;
					
					ptmember.setPartyRoom(maxid);
					
					room.addMember(ptmember);
				}
			}
			
			activeChar.sendPacket(new PartyMatchDetail(room));
			activeChar.sendPacket(new ExPartyRoomMember(room, 1));
			
			activeChar.sendPacket(SystemMessageId.PARTY_ROOM_CREATED);
			
			activeChar.setPartyRoom(maxid);
			activeChar.broadcastUserInfo();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__70_REQUESTPARTYMATCHLIST;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchWaitingList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExClosePartyRoom;
import com.l2jhellas.gameserver.network.serverpackets.PartyMatchList;

public final class RequestOustFromPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_01_REQUESTOUSTFROMPARTYROOM = "[C] D0:01 RequestOustFromPartyRoom";
	
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2PcInstance member = L2World.getInstance().getPlayer(_id);
		
		if (member == null)
			return;
		
		final PartyMatchRoom _room = PartyMatchRoomList.getInstance().getPlayerRoom(member);
		
		if (_room == null)
			return;
		
		if (_room.getOwner() != activeChar)
			return;
		
		if (activeChar.isInParty() && member.isInParty() && activeChar.getParty().getPartyLeaderOID() == member.getParty().getPartyLeaderOID())
			activeChar.sendPacket(SystemMessageId.CANNOT_DISMISS_PARTY_MEMBER);
		else
		{
			_room.deleteMember(member);
			member.setPartyRoom(0);
			member.sendPacket(ExClosePartyRoom.STATIC_PACKET);
			
			PartyMatchWaitingList.getInstance().addPlayer(member);
			member.sendPacket(new PartyMatchList(member, 0, MapRegionTable.getClosestLocation(member.getX(), member.getY()), member.getLevel()));
			member.broadcastUserInfo();
			
			member.sendPacket(SystemMessageId.OUSTED_FROM_PARTY_ROOM);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_01_REQUESTOUSTFROMPARTYROOM;
	}
}
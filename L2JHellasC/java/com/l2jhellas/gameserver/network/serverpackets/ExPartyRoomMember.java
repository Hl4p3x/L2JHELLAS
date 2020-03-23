package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ExPartyRoomMember extends L2GameServerPacket
{
	private final PartyMatchRoom _room;
	private final int _mode;
	
	public ExPartyRoomMember(PartyMatchRoom room, int mode)
	{
		_room = room;
		_mode = mode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x0e);
		writeD(_mode);
		writeD(_room.getMembers());
		for (L2PcInstance _member : _room.getPartyMembers())
		{
			writeD(_member.getObjectId());
			writeS(_member.getName());
			writeD(_member.getActiveClass());
			writeD(_member.getLevel());
			writeD(MapRegionTable.getClosestLocation(_member.getX(), _member.getY()));
			if (_room.getOwner().equals(_member))
				writeD(1);
			else
			{
				if ((_room.getOwner().isInParty() && _member.isInParty()) && (_room.getOwner().getParty().getPartyLeaderOID() == _member.getParty().getPartyLeaderOID()))
					writeD(2);
				else
					writeD(0);
			}
		}
	}
}
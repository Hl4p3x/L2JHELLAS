package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PartyMatchList extends L2GameServerPacket
{
	private static final String _S__AF_PARTYMATCHLIST = "[S] 96 PartyMatchList";
	private final L2PcInstance _cha;
	private final int _loc;
	private final int _lim;
	private final List<PartyMatchRoom> _rooms;
	
	public PartyMatchList(L2PcInstance player, int auto, int location, int limit)
	{
		_cha = player;
		_loc = location;
		_lim = limit;
		_rooms = new ArrayList<>();
	}
	
	@Override
	protected final void writeImpl()
	{
		if (getClient().getActiveChar() == null)
			return;
		
		for (PartyMatchRoom room : PartyMatchRoomList.getInstance().getRooms())
		{
			if (room.getMembers() < 1 || room.getOwner() == null || room.getOwner().isOnline() == 0 || room.getOwner().getPartyRoom() != room.getId())
			{
				PartyMatchRoomList.getInstance().deleteRoom(room.getId());
				continue;
			}
			
			if (_loc > 0 && _loc != room.getLocation())
				continue;
			
			if (_lim == 0 && ((_cha.getLevel() < room.getMinLvl()) || (_cha.getLevel() > room.getMaxLvl())))
				continue;
			
			_rooms.add(room);
		}
		
		writeC(0x96);
		writeD((!_rooms.isEmpty()) ? 1 : 0);
		writeD(_rooms.size());
		for (PartyMatchRoom room : _rooms)
		{
			writeD(room.getId());
			writeS(room.getTitle());
			writeD(room.getLocation());
			writeD(room.getMinLvl());
			writeD(room.getMaxLvl());
			writeD(room.getMembers());
			writeD(room.getMaxMembers());
			writeS(room.getOwner().getName());
		}
	}
	
	@Override
	public String getType()
	{
		return _S__AF_PARTYMATCHLIST;
	}
}
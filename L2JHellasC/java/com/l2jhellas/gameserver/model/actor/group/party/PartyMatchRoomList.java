package com.l2jhellas.gameserver.model.actor.group.party;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExClosePartyRoom;

public class PartyMatchRoomList
{
	private final Map<Integer, PartyMatchRoom> _rooms;
	private AtomicInteger _currentId = new AtomicInteger();
	
	protected PartyMatchRoomList()
	{
		_rooms = new HashMap<>();
	}
	
	public int getNewId()
	{
		return _currentId.incrementAndGet();
	}
	
	public void addPartyMatchRoom(int id, PartyMatchRoom room)
	{
		_rooms.put(id, room);
	}
	
	public void deleteRoom(int id)
	{
		for (L2PcInstance _member : getRoom(id).getPartyMembers())
		{
			if (_member == null)
				continue;
			
			_member.sendPacket(ExClosePartyRoom.STATIC_PACKET);
			_member.sendPacket(SystemMessageId.PARTY_ROOM_DISBANDED);
			
			_member.setPartyRoom(0);
			_member.broadcastUserInfo();
		}
		_rooms.remove(id);
	}
	
	public PartyMatchRoom getRoom(int id)
	{
		return _rooms.get(id);
	}
	
	public PartyMatchRoom[] getRooms()
	{
		return _rooms.values().toArray(new PartyMatchRoom[_rooms.size()]);
	}
	
	public int getPartyMatchRoomCount()
	{
		return _rooms.size();
	}

	public PartyMatchRoom getAvailableRoom(L2PcInstance player,int location,boolean level)
	{		
		return _rooms.values().stream().filter(r -> (location == -1 ? true : location == -2 ? r.getLocation() == MapRegionTable.getClosestLocation(player.getX(), player.getY()) : r.getLocation() == location)
		&& (level ? true : player.getLevel() >= r.getMinLvl() && player.getLevel() <= r.getMaxLvl()) && !r.isFull()).findFirst().orElse(null);
	}
	
	public PartyMatchRoom getPlayerRoom(L2PcInstance player)
	{
		for (PartyMatchRoom _room : _rooms.values())
			for (L2PcInstance member : _room.getPartyMembers())
				if (member.equals(player))
					return _room;
		
		return null;
	}
	
	public int getPlayerRoomId(L2PcInstance player)
	{
		for (PartyMatchRoom _room : _rooms.values())
			for (L2PcInstance member : _room.getPartyMembers())
				if (member.equals(player))
					return _room.getId();
		
		return -1;
	}
	
	public static PartyMatchRoomList getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PartyMatchRoomList _instance = new PartyMatchRoomList();
	}
}
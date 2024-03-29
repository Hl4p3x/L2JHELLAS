package com.l2jhellas.gameserver.model.actor.group.party;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class PartyMatchRoom
{
	private final int _id;
	private String _title;
	private int _loot;
	private int _location;
	private int _minlvl;
	private int _maxlvl;
	private int _maxmem;
	private final List<L2PcInstance> _members = new CopyOnWriteArrayList<>();
	
	public PartyMatchRoom(int id, String title, int loot, int minlvl, int maxlvl, int maxmem, L2PcInstance owner)
	{
		_id = id;
		_title = title;
		_loot = loot;
		_location = MapRegionTable.getClosestLocation(owner.getX(), owner.getY());
		_minlvl = minlvl;
		_maxlvl = maxlvl;
		_maxmem = maxmem;
		_members.add(owner);
	}
	
	public List<L2PcInstance> getPartyMembers()
	{
		return _members;
	}
	
	public void addMember(L2PcInstance player)
	{
		_members.add(player);
	}
	
	public void deleteMember(L2PcInstance player)
	{
		if (player == null || !_members.contains(player))
			return;
		
		if (player != getOwner())
		{
			_members.remove(player);
			notifyMembersAboutExit(player);
		}
		else if (_members.size() == 1)
		{
			PartyMatchRoomList.getInstance().deleteRoom(_id);
		}
		else
		{
			changeLeader(_members.get(1));
			deleteMember(player);
		}
	}
	
	public void notifyMembersAboutExit(L2PcInstance player)
	{
		for (L2PcInstance _member : getPartyMembers())
		{
			_member.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_LEFT_PARTY_ROOM).addPcName(player));
			_member.sendPacket(new ExManagePartyRoomMember(player, this, 2));
		}
	}
	
	public void changeLeader(L2PcInstance newLeader)
	{
		// Get current leader
		L2PcInstance oldLeader = _members.get(0);
		// Remove new leader
		_members.remove(newLeader);
		// Move him to first position
		_members.set(0, newLeader);
		// Add old leader as normal member
		_members.add(oldLeader);
		// Broadcast change
		for (L2PcInstance member : getPartyMembers())
		{
			member.sendPacket(new ExManagePartyRoomMember(newLeader, this, 1));
			member.sendPacket(new ExManagePartyRoomMember(oldLeader, this, 1));
			member.sendPacket(SystemMessageId.PARTY_ROOM_LEADER_CHANGED);
		}
	}
	
	public void broadcastPacket(L2GameServerPacket packet)
	{
		for (L2PcInstance member : getPartyMembers())
			member.sendPacket(packet);
	}
	
	public int getId()
	{
		return _id;
	}
	
	public L2PcInstance getOwner()
	{
		return _members.get(0);
	}
	
	public boolean isFull()
	{
		return _members.size() >= getMaxMembers();
	}
	
	public int getMembers()
	{
		return _members.size();
	}
	
	public int getLootType()
	{
		return _loot;
	}
	
	public void setLootType(int loot)
	{
		_loot = loot;
	}
	
	public int getMinLvl()
	{
		return _minlvl;
	}
	
	public void setMinLvl(int minlvl)
	{
		_minlvl = minlvl;
	}
	
	public int getMaxLvl()
	{
		return _maxlvl;
	}
	
	public void setMaxLvl(int maxlvl)
	{
		_maxlvl = maxlvl;
	}
	
	public int getLocation()
	{
		return _location;
	}
	
	public void setLocation(int loc)
	{
		_location = loc;
	}
	
	public int getMaxMembers()
	{
		return _maxmem;
	}
	
	public void setMaxMembers(int maxmem)
	{
		_maxmem = maxmem;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public void setTitle(String title)
	{
		_title = title;
	}
	
	public boolean allowToEnter(L2PcInstance player)
	{
		return player.getLevel() >= _minlvl && player.getLevel() <= _maxlvl && _members.size() < _maxmem;
	}
}
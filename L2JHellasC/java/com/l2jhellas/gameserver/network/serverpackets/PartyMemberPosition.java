package com.l2jhellas.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;

public class PartyMemberPosition extends L2GameServerPacket
{
	Map<Integer, Location> _locations = new HashMap<>();
	
	public PartyMemberPosition(L2Party party)
	{
		reuse(party);
	}
	
	public void reuse(L2Party party)
	{
		_locations.clear();
		for (L2PcInstance member : party.getPartyMembers())
			_locations.put(member.getObjectId(), new Location(member.getX(), member.getY(), member.getZ()));
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xa7);
		writeD(_locations.size());
		for (Map.Entry<Integer, Location> entry : _locations.entrySet())
		{
			final Location loc = entry.getValue();
			
			writeD(entry.getKey());
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}
	
	@Override
	public String getType()
	{
		return null;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoom;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchRoomList;
import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchWaitingList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _minlvl;
	private final int _maxlvl;
	private final int _mode;
	private final List<L2PcInstance> _members;
	
	public ExListPartyMatchingWaitingRoom(L2PcInstance player, int page, int minlvl, int maxlvl, int mode)
	{
		_activeChar = player;
		_minlvl = minlvl;
		_maxlvl = maxlvl;
		_mode = mode;
		_members = new ArrayList<>();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x35);
		
		if (_mode == 0)
		{
			final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_activeChar.getPartyRoom());
			if (room == null || !room.getOwner().equals(_activeChar))
			{
				writeD(0);
				writeD(0);
				return;
			}
		}
		
		for (L2PcInstance cha : PartyMatchWaitingList.getInstance().getPlayers())
		{
			// Don't add yourself in the list
			if (cha == null || cha == _activeChar)
				continue;
			
			if (cha.getLevel() < _minlvl || cha.getLevel() > _maxlvl)
				continue;
			
			_members.add(cha);
		}
		
		writeD(1);
		writeD(_members.size());
		for (L2PcInstance member : _members)
		{
			writeS(member.getName());
			writeD(member.getActiveClass());
			writeD(member.getLevel());
		}
	}
}
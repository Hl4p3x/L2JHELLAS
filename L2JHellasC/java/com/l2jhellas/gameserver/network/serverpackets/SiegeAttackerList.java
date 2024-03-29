package com.l2jhellas.gameserver.network.serverpackets;

import java.util.List;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2SiegeClan;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.scrips.siegable.SiegableHall;


public class SiegeAttackerList extends L2GameServerPacket
{
	private static final String _S__CA_SiegeAttackerList = "[S] ca SiegeAttackerList";	
	private final int _id;
	private final List<L2SiegeClan> _attackers;
	
	public SiegeAttackerList(Castle castle)
	{
		_id = castle.getCastleId();
		_attackers = castle.getSiege().getAttackerClans();
	}
	
	public SiegeAttackerList(SiegableHall hall)
	{
		_id = hall.getId();
		_attackers = hall.getSiege().getAttackerClans();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xca);
		writeD(_id);
		writeD(0x00); // 0
		writeD(0x01); // 1
		writeD(0x00); // 0
		int size = _attackers.size();
		if (size > 0)
		{
			L2Clan clan;
			
			writeD(size);
			writeD(size);
			for (L2SiegeClan siegeclan : _attackers)
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				if (clan == null)
					continue;
				
				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); // signed time (seconds) (not stored by L2J)
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); // AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__CA_SiegeAttackerList;
	}
}
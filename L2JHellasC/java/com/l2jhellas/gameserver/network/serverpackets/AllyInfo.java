package com.l2jhellas.gameserver.network.serverpackets;

import java.util.Collection;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanInfo;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class AllyInfo extends L2GameServerPacket
{
	private static final String _S__7A_FRIENDLIST = "[S] 7a AllyInfo";
	
	private final String _name;
	private final int _total;
	private final int _online;
	private final String _leaderC;
	private final String _leaderP;
	private final L2ClanInfo[] _allies;
	
	public AllyInfo(int allianceId)
	{
		final L2Clan leader = ClanTable.getInstance().getClan(allianceId);

		_name = leader.getAllyName();
		_leaderC = leader.getName();
		_leaderP = leader.getLeaderName();
		
		final Collection<L2Clan> allies = ClanTable.getInstance().getClanAllies(allianceId);
		_allies = new L2ClanInfo[allies.size()];
		int idx = 0, total = 0, online = 0;
		for (final L2Clan clan : allies)
		{
			final L2ClanInfo ci = new L2ClanInfo(clan);
			_allies[idx++] = ci;
			total += ci.getTotal();
			online += ci.getOnline();
		}
		
		_total = total;
		_online = online;
	}
	
	@Override
	protected final void writeImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		writeC(0xb4);
		
		writeS(_name);
		writeD(_total);
		writeD(_online);
		writeS(_leaderC);
		writeS(_leaderP);
		
		writeD(_allies.length);
		for (final L2ClanInfo aci : _allies)
		{
			writeS(aci.getClan().getName());
			writeD(0x00);
			writeD(aci.getClan().getLevel());
			writeS(aci.getClan().getLeaderName());
			writeD(aci.getTotal());
			writeD(aci.getOnline());
		}
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getTotal()
	{
		return _total;
	}
	
	public int getOnline()
	{
		return _online;
	}
	
	public String getLeaderC()
	{
		return _leaderC;
	}
	
	public String getLeaderP()
	{
		return _leaderP;
	}
	
	public L2ClanInfo[] getAllies()
	{
		return _allies;
	}
	
	@Override
	public String getType()
	{
		return _S__7A_FRIENDLIST;
	}
}
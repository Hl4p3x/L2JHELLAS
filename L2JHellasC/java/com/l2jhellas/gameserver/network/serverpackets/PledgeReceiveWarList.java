package com.l2jhellas.gameserver.network.serverpackets;

import java.util.List;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.L2Clan;

public class PledgeReceiveWarList extends L2GameServerPacket
{
	private static final String _S__FE_3E_PLEDGERECEIVEWARELIST = "[S] FE:3E PledgeReceiveWarList";
	private final List<Integer> _clanList;
	private final int _tab;
	private final int _page;
	
	public PledgeReceiveWarList(List<Integer> clanList, int tab, int page)
	{
		_clanList = clanList;
		_tab = tab;
		_page = page;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3e);
		writeD(_tab);
		writeD(_page);
		writeD((_tab == 0) ? _clanList.size() : (_page == 0) ? (_clanList.size() >= 13) ? 13 : _clanList.size() : _clanList.size() % (13 * _page));
		
		int index = 0;
		
		for (int clanId : _clanList)
		{
			L2Clan clan = ClanTable.getInstance().getClan(clanId);
			if (clan == null)
				continue;
			
			if (_tab != 0)
			{
				if (index < _page * 13)
				{
					index++;
					continue;
				}
				
				if (index == (_page + 1) * 13)
					break;
				
				index++;
			}
			
			writeS(clan.getName());
			writeD(_tab);
			writeD(_page);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_3E_PLEDGERECEIVEWARELIST;
	}
}
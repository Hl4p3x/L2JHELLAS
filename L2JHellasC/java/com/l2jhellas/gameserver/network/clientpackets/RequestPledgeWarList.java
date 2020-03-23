package com.l2jhellas.gameserver.network.clientpackets;

import java.util.List;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.PledgeReceiveWarList;

public final class RequestPledgeWarList extends L2GameClientPacket
{
	private static final String _C__D0_1E_REQUESTPLEDGEWARLIST = "[C] D0:1E RequestPledgeWarList";
	private int _page;
	private int _tab;
	
	@Override
	protected void readImpl()
	{
		_page = readD();
		_tab = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null || player.getClan() == null)
			return;
		
		final L2Clan clan = player.getClan();
		
		final List<Integer> list;
		if (_tab == 0)
		{
			list = clan.getWarList();
		}
		else
		{
			list = clan.getAttackerList();
			_page = Math.max(0, (_page > list.size() / 13) ? 0 : _page);
		}
		
		player.sendPacket(new PledgeReceiveWarList(list, _tab, _page));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_1E_REQUESTPLEDGEWARLIST;
	}
}
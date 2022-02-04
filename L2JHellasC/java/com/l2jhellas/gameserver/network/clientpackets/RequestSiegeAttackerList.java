package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.ClanHallSiegeManager;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.serverpackets.SiegeAttackerList;
import com.l2jhellas.gameserver.scrips.siegable.SiegableHall;

public final class RequestSiegeAttackerList extends L2GameClientPacket
{
	private static final String _C__A2_RequestSiegeAttackerList = "[C] a2 RequestSiegeAttackerList";
	
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Castle castle = CastleManager.getInstance().getCastleById(_id);
		if (castle != null)
		{
			sendPacket(new SiegeAttackerList(castle));
			return;
		}
				
		final SiegableHall sh = ClanHallSiegeManager.getInstance().getSiegableHall(_id);
		if (sh != null)
			sendPacket(new SiegeAttackerList(sh));
	}
	
	@Override
	public String getType()
	{
		return _C__A2_RequestSiegeAttackerList;
	}
}
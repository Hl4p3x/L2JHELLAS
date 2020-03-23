package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.serverpackets.SiegeDefenderList;

public final class RequestConfirmSiegeWaitingList extends L2GameClientPacket
{
	private static final String _C__A5_RequestConfirmSiegeWaitingList = "[C] a5 RequestConfirmSiegeWaitingList";
	
	private int _approved;
	private int _castleId;
	private int _clanId;
	
	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_clanId = readD();
		_approved = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		// Check if the player has a clan
		if (activeChar.getClan() == null)
			return;
		
		final Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle == null)
			return;
		
		// Check if leader of the clan who owns the castle?
		if ((castle.getOwnerId() != activeChar.getClanId()) || (!activeChar.isClanLeader()))
			return;
		
		final L2Clan clan = ClanTable.getInstance().getClan(_clanId);
		if (clan == null)
			return;
		
		if (!castle.getSiege().getIsRegistrationOver())
		{
			if (_approved == 1)
			{
				if (castle.getSiege().checkIsDefenderWaiting(clan))
					castle.getSiege().approveSiegeDefenderClan(_clanId);
				else
					return;
			}
			else
			{
				if ((castle.getSiege().checkIsDefenderWaiting(clan)) || (castle.getSiege().checkIsDefender(clan)))
					castle.getSiege().removeSiegeClan(_clanId);
			}
		}
		// Update the defender list
		activeChar.sendPacket(new SiegeDefenderList(castle));
	}
	
	@Override
	public String getType()
	{
		return _C__A5_RequestConfirmSiegeWaitingList;
	}
}
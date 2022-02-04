package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.ClanHallSiegeManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SiegeInfo;
import com.l2jhellas.gameserver.scrips.siegable.SiegableHall;

public final class RequestJoinSiege extends L2GameClientPacket
{
	private static final String _C__A4_RequestJoinSiege = "[C] a4 RequestJoinSiege";
	
	private int _id;
	private int _isAttacker;
	private int _isJoining;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_isAttacker = readD();
		_isJoining = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (!activeChar.isClanLeader())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastleById(_id);
		
		if (castle != null)
		{		
			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < activeChar.getClan().getDissolvingExpiryTime())
				{
					activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
					return;
				}
				if (_isAttacker == 1)
					castle.getSiege().registerAttacker(activeChar);
				else
					castle.getSiege().registerDefender(activeChar);
			}
			else
				castle.getSiege().removeSiegeClan(activeChar);

			castle.getSiege().listRegisterClan(activeChar);
			return;
		}
		
		final SiegableHall sh = ClanHallSiegeManager.getInstance().getSiegableHall(_id);
		if (sh != null)
		{
			final L2Clan clan = activeChar.getClan();
			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < activeChar.getClan().getDissolvingExpiryTime())
				{
					activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
					return;
				}
				
				ClanHallSiegeManager.getInstance().registerClan(clan, sh, activeChar);
			}
			else
				ClanHallSiegeManager.getInstance().unRegisterClan(clan, sh);
			
			activeChar.sendPacket(new SiegeInfo(sh));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__A4_RequestJoinSiege;
	}
}
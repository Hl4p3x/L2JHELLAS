package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.SystemMessageId;

public final class RequestJoinSiege extends L2GameClientPacket
{
	private static final String _C__A4_RequestJoinSiege = "[C] a4 RequestJoinSiege";
	
	private int _castleId;
	private int _isAttacker;
	private int _isJoining;
	
	@Override
	protected void readImpl()
	{
		_castleId = readD();
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
		
		final Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		
		if (castle == null)
			return;
		
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
	}
	
	@Override
	public String getType()
	{
		return _C__A4_RequestJoinSiege;
	}
}
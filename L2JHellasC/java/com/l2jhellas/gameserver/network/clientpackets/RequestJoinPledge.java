package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AskJoinPledge;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinPledge extends L2GameClientPacket
{
	private static final String _C__24_REQUESTJOINPLEDGE = "[C] 24 RequestJoinPledge";
	
	private int _target;
	private int _pledgeType;
	
	@Override
	protected void readImpl()
	{
		_target = readD();
		_pledgeType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2PcInstance target = L2World.getInstance().getPlayer(_target);
		
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		
		if (!clan.checkClanJoinCondition(activeChar, target, _pledgeType))
			return;
		
		if (!activeChar.getRequest().setRequest(target, this))
			return;
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_S2);
		sm.addString(activeChar.getName());
		sm.addString(activeChar.getClan().getName());
		target.sendPacket(sm);
		sm = null;
		AskJoinPledge ap = new AskJoinPledge(activeChar.getObjectId(), activeChar.getClan().getName());
		target.sendPacket(ap);
	}
	
	public int getPledgeType()
	{
		return _pledgeType;
	}
	
	@Override
	public String getType()
	{
		return _C__24_REQUESTJOINPLEDGE;
	}
}
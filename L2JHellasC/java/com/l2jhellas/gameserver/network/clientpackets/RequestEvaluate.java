package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;

public final class RequestEvaluate extends L2GameClientPacket
{
	private static final String _C__B9_REQUESTEVALUATE = "[C] B9 RequestEvaluate";
	
	private int _targetId;
	
	@Override
	protected void readImpl()
	{
		_targetId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null || _targetId <= 0)
			return;
		
		final L2PcInstance target = (activeChar.getTargetId() == _targetId) ? activeChar.getTarget().getActingPlayer() : L2World.getInstance().getPlayer(_targetId);
		
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		
		if (activeChar.equals(target))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
			return;
		}
		
		if (activeChar.getTarget() != target)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}
		
		if (activeChar.getLevel() < 10)
		{
			activeChar.sendPacket(SystemMessageId.ONLY_LEVEL_SUP_10_CAN_RECOMMEND);
			return;
		}
				
		if (activeChar.getRecomLeft() <= 0)
		{
			activeChar.sendPacket(SystemMessageId.NO_MORE_RECOMMENDATIONS_TO_HAVE);
			return;
		}
		
		if (target.getRecomHave() >= 255)
		{
			activeChar.sendPacket(SystemMessageId.YOUR_TARGET_NO_LONGER_RECEIVE_A_RECOMMENDATION);
			return;
		}
		
		if (!activeChar.canRecom(target))
		{
			activeChar.sendPacket(SystemMessageId.THAT_CHARACTER_IS_RECOMMENDED);
			return;
		}
		
		activeChar.giveRecom(target);
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_S1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT).addCharName(target).addNumber(activeChar.getRecomLeft()));
		target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_S1).addCharName(activeChar));
		
		activeChar.sendPacket(new UserInfo(activeChar));
		target.broadcastUserInfo();
	}
	
	@Override
	public String getType()
	{
		return _C__B9_REQUESTEVALUATE;
	}
}
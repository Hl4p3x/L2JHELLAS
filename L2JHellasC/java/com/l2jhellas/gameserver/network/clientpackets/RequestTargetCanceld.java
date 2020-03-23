package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestTargetCanceld extends L2GameClientPacket
{
	private static final String _C__37_REQUESTTARGETCANCELD = "[C] 37 RequestTargetCanceld";
	
	private int _unselect;
	
	@Override
	protected void readImpl()
	{
		_unselect = readH();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar != null)
		{
			if (_unselect == 0)
			{
				if (activeChar.isCastingNow() && activeChar.canAbortCast())
					activeChar.abortCast();
				else if (activeChar.getTarget() != null)
					activeChar.setTarget(null);
			}
			else if (activeChar.getTarget() != null)
				activeChar.setTarget(null);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__37_REQUESTTARGETCANCELD;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.StartRotation;

public final class StartRotating extends L2GameClientPacket
{
	private static final String _C__4A_STARTROTATING = "[C] 4A StartRotating";
	
	private int _degree;
	
	// -1 = left 1 = right
	private int _side;
	
	@Override
	protected void readImpl()
	{
		_degree = readD();
		_side = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		StartRotation br = new StartRotation(activeChar.getObjectId(), _degree, _side, 0);
		activeChar.broadcastPacket(br);
	}
	
	@Override
	public String getType()
	{
		return _C__4A_STARTROTATING;
	}
}
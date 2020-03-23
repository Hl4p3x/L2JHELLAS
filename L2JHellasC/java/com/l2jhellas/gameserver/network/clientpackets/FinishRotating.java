package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.network.serverpackets.StopRotation;

public final class FinishRotating extends L2GameClientPacket
{
	private static final String _C__4B_FINISHROTATING = "[C] 4B FinishRotating";
	
	private int _degree;
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_degree = readD();
		_unknown = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null)
			return;
		StopRotation sr = new StopRotation(getClient().getActiveChar().getObjectId(), _degree, 0);
		getClient().getActiveChar().broadcastPacket(sr);
	}
	
	@Override
	public String getType()
	{
		return _C__4B_FINISHROTATING;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class ChangeMoveType2 extends L2GameClientPacket
{
	private static final String _C__1C_CHANGEMOVETYPE2 = "[C] 1C ChangeMoveType2";
	
	private boolean _typeRun;
	
	@Override
	protected void readImpl()
	{
		_typeRun = readD() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (!player.isDead())
		{
			if (_typeRun)
				player.setRunning();
			else
				player.setWalking();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__1C_CHANGEMOVETYPE2;
	}
}
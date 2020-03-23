package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;

public final class ChangeWaitType2 extends L2GameClientPacket
{
	private static final String _C__1D_CHANGEWAITTYPE2 = "[C] 1D ChangeWaitType2";
	private boolean _typeStand;
	
	@Override
	protected void readImpl()
	{
		_typeStand = (readD() == 1);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isDead() || player.getMountType() != 0 || player.isOutOfControl()) // prevent sit/stand if you riding-outOFControl-dead
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.SitStand(player.getTarget(), _typeStand);
	}
	
	@Override
	public String getType()
	{
		return _C__1D_CHANGEWAITTYPE2;
	}
}
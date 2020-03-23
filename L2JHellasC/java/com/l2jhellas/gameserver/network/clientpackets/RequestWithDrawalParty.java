package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestWithDrawalParty extends L2GameClientPacket
{
	private static final String _C__2B_REQUESTWITHDRAWALPARTY = "[C] 2B RequestWithDrawalParty";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isInParty())
		{
			if (player.getParty().isInDimensionalRift() && !player.getParty().getDimensionalRift().getRevivedAtWaitingRoom().contains(player))
				player.sendMessage("You can't exit party when you are in Dimensional Rift.");
			else
				player.getParty().removePartyMember(player);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__2B_REQUESTWITHDRAWALPARTY;
	}
}
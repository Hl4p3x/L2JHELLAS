package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2EffectPointInstance extends L2Npc
{
	private final L2Character _owner;
	
	public L2EffectPointInstance(int objectId, L2NpcTemplate template, L2Character owner)
	{
		super(objectId, template);
		_owner = owner;
	}
	
	public L2Character getOwner()
	{
		return _owner;
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player == null)
			return;
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
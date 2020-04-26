package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public final class L2DecoInstance extends L2Npc
{
	
	public L2DecoInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	@Override
	public boolean isAttackable()
	{
		return false;
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (getObjectId() != player.getTargetId())
		{
			// Set the player AI Intention to AI_INTENTION_IDLE
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			// The color to display in the select window is White
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
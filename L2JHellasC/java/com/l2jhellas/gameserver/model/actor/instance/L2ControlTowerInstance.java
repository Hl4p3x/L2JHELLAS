package com.l2jhellas.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2ControlTowerInstance extends L2Npc
{
	private List<L2Spawn> _guards;
	
	public L2ControlTowerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAttackable()
	{
		// Attackable during siege by attacker only
		return (getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress());
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Attackable during siege by attacker only
		return (attacker != null && attacker instanceof L2PcInstance && getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan()));
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
		}
		else
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100 // Less then max height difference, delete check when geo
				&& GeoEngine.canSeeTarget(player, this, false))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	public void onDeath()
	{
		if (getCastle().getSiege().getIsInProgress())
		{
			getCastle().getSiege().killedCT(this);
			
			if ((getGuards() != null) && (getGuards().size() > 0))
			{
				for (L2Spawn spawn : getGuards())
				{
					if (spawn == null)
						continue;
					spawn.stopRespawn();
					// spawn.getLastSpawn().doDie(spawn.getLastSpawn());
				}
			}
		}
	}
	
	public void registerGuard(L2Spawn guard)
	{
		getGuards().add(guard);
	}
	
	public final List<L2Spawn> getGuards()
	{
		if (_guards == null)
			_guards = new ArrayList<>();
		return _guards;
	}
}
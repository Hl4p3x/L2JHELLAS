package com.l2jhellas.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MoveToPawn;
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
		return getCastle() != null && getCastle().getSiege().getIsInProgress();
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker.isPlayer() && getCastle() != null && getCastle().getSiege().getIsInProgress() && getCastle().getSiege().checkIsAttacker(((L2PcInstance) attacker).getClan());
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
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100 && ((Config.GEODATA) ? GeoEngine.canSeeTarget(player, this, isFlying()) : GeoEngine.canSeeTarget(player, this)))
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			else
			{
				if (player.isMoving() || player.isInCombat())
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				
				if(!player.isSitting())
				    player.sendPacket(new MoveToPawn(player, this, L2Npc.INTERACTION_DISTANCE));			
			}
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (getCastle() != null && getCastle().getSiege().getIsInProgress())
		{
			getCastle().getSiege().killedCT(this);
			
			if ((getGuards() != null) && (getGuards().size() > 0))
			{
				for (L2Spawn spawn : getGuards())
				{
					if (spawn == null)
						continue;
					
					spawn.stopRespawn();
				}
			}
		}
		
		return super.doDie(killer);
	}

	@Override
	public void onSpawn()
	{
		 setIsInvul(false);
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
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
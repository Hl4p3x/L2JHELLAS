package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.model.L2SiegeClan;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.entity.Siege;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2SiegeFlagInstance extends L2Npc
{
	private final L2PcInstance _player;
	private final Siege _siege;
	
	public L2SiegeFlagInstance(L2PcInstance player, int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		_player = player;
		_siege = SiegeManager.getSiege(_player.getX(), _player.getY(), _player.getZ());
		if ((_player.getClan() == null) || (_siege == null))
		{
			deleteMe();
		}
		else
		{
			L2SiegeClan sc = _siege.getAttackerClan(_player.getClan());
			if (sc == null)
				deleteMe();
			else
				sc.addFlag(this);
		}
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
		return (attacker != null && attacker instanceof L2PcInstance && getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress());
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		L2SiegeClan sc = _siege.getAttackerClan(_player.getClan());
		if (sc != null)
			sc.removeFlag(this);
		return true;
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if ((player == null) || !canTarget(player))
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
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			else
			{
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
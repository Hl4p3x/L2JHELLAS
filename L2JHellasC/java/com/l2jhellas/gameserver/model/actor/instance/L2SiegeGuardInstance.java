package com.l2jhellas.gameserver.model.actor.instance;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.ai.L2CharacterAI;
import com.l2jhellas.gameserver.ai.L2SiegeGuardAI;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public final class L2SiegeGuardInstance extends L2Attackable
{
	private static Logger _log = Logger.getLogger(L2SiegeGuardInstance.class.getName());
	
	private int _homeX;
	private int _homeY;
	private int _homeZ;
	
	public L2SiegeGuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		synchronized (this)
		{
			if (_ai == null)
				_ai = new L2SiegeGuardAI(this);
		}
		return _ai;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Attackable during siege by all except defenders
		
		return (attacker != null && attacker instanceof L2PcInstance && getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress() && !getCastle().getSiege().checkIsDefender(((L2PcInstance) attacker).getClan()));
		
	}
	
	public void getHomeLocation()
	{
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();
		
		if (Config.DEBUG)
			_log.finer(getObjectId() + ": Home location set to" + " X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
	}
	
	public int getHomeX()
	{
		return _homeX;
	}
	
	public int getHomeY()
	{
		return _homeY;
	}
	
	@Override
	public boolean returnHome()
	{
		if (!isInsideRadius(_homeX, _homeY, 40, false))
		{
			if (Config.DEBUG)
				_log.fine(getObjectId() + ": moving home");
			setIsReturningToSpawnPoint(true);
			clearAggroList();
			
			if (hasAI())
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_homeX, _homeY, _homeZ, 0));
			
			return true;
		}
		return false;
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			if (Config.DEBUG)
				_log.fine("new target selected:" + getObjectId());
			
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
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
				else
				{
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			if (!isAutoAttackable(player))
			{
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					showChatWindow(player, 0);
				}
			}
		}
	}
	
	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;
		
		if (!(attacker instanceof L2SiegeGuardInstance))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}
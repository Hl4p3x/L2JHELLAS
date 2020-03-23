package com.l2jhellas.gameserver.handlers.usercommandhandlers;

import com.l2jhellas.gameserver.handler.IUserCommandHandler;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.Ride;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.Broadcast;

public class Mount implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		61
	};
	
	@Override
	public synchronized boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;
		
		L2Summon pet = activeChar.getPet();
		
		if (pet != null && pet.isMountable() && !activeChar.isMounted())
		{
			if (activeChar.isDead())
			{
				// A strider cannot be ridden when player is dead.
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
				activeChar.sendPacket(msg);
			}
			else if (pet.isDead())
			{
				// A dead strider cannot be ridden.
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
				activeChar.sendPacket(msg);
			}
			else if (pet.isInCombat())
			{
				// A strider in battle cannot be ridden.
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
				activeChar.sendPacket(msg);
			}
			else if (activeChar.isInCombat())
			{
				// A pet cannot be ridden while player is in battle.
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				activeChar.sendPacket(msg);
			}
			else if (activeChar.isSitting() || activeChar.isMoving())
			{
				// A strider can be ridden only when player is standing.
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
				activeChar.sendPacket(msg);
			}
			else if (!pet.isDead() && !activeChar.isMounted())
			{
				if (!activeChar.disarmWeapons())
					return false;
				
				if (activeChar.getActiveTradeList() != null)
					activeChar.cancelActiveTrade();
				
				Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().npcId);
				activeChar.setMountType(mount.getMountType());
				activeChar.setMountObjectID(pet.getControlItemId());
				Broadcast.toSelfAndKnownPlayersInRadius(activeChar, mount, 810000);
				pet.unSummon(activeChar);
			}
		}
		else if (activeChar.isRentedPet())
		{
			activeChar.stopRentPet();
		}
		else if (activeChar.isMounted())
		{
			// Dismount
			if (activeChar.setMountType(0))
			{
				if (activeChar.getActiveTradeList() != null)
					activeChar.cancelActiveTrade();
				
				if (activeChar.isFlying())
					activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
				Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
				activeChar.setMountObjectID(0);
				Broadcast.toSelfAndKnownPlayers(activeChar, dismount);
			}
		}
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
package com.l2jhellas.gameserver.handlers.voicedcommandhandlers;

import com.l2jhellas.gameserver.handler.IVoicedCommandHandler;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.serverpackets.Ride;

public class CastleCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"open doors",
		"close doors",
		"ride wyvern"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar.isClanLeader())
		{
			if (command.startsWith(VOICED_COMMANDS[0]) && target.equals("castle"))
			{
				final L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
				final Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().hasCastle());
				if (door == null || castle == null)
					return false;
				if (castle.checkIfInZone(door.getX(), door.getY(), door.getZ()))
					door.openMe();
			}
			else if (command.startsWith(VOICED_COMMANDS[1]) && target.equals("castle"))
			{
				final L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
				final Castle castle = CastleManager.getInstance().getCastleById(activeChar.getClan().hasCastle());
				if (door == null || castle == null)
					return false;
				if (castle.checkIfInZone(door.getX(), door.getY(), door.getZ()))
					door.closeMe();
			}
			else if (command.startsWith(VOICED_COMMANDS[2]) && target.equals("castle"))
			{
				if (activeChar.getClan().hasCastle() > 0)
				{
					if (!activeChar.disarmWeapons())
						return false;
					
					if (activeChar.getActiveTradeList() != null)
						activeChar.cancelActiveTrade();
					
					final Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, 12621);
					activeChar.sendPacket(mount);
					activeChar.broadcastPacket(mount);
					activeChar.setMountType(mount.getMountType());
				}
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
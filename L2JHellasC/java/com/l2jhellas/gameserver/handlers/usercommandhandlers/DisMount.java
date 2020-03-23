package com.l2jhellas.gameserver.handlers.usercommandhandlers;

import com.l2jhellas.gameserver.handler.IUserCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.Ride;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.Broadcast;

public class DisMount implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		62
	};
	
	@Override
	public synchronized boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;
		
		if (activeChar.isRentedPet())
		{
			activeChar.stopRentPet();
		}
		else if (activeChar.isMounted())
		{
			if (activeChar.setMountType(0))
			{
				if (activeChar.getActiveTradeList() != null)
					activeChar.cancelActiveTrade();
				
				if (activeChar.isFlying())
					activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
				Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
				Broadcast.toSelfAndKnownPlayersInRadius(activeChar, dismount, 810000);
				activeChar.setMountObjectID(0);
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
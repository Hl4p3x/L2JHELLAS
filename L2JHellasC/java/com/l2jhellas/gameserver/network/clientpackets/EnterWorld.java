package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.TaskPriority;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.L2GameClient.GameClientState;

public class EnterWorld extends L2GameClientPacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_URGENT;
	}
	
	@Override
	protected void readImpl()
	{
		// this is just a trigger packet. it has no content
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			_log.warning(EnterWorld.class.getName() + ": EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}
		
		getClient().setState(GameClientState.IN_GAME);
		
		activeChar.EnterWolrd();
	}
	
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
}
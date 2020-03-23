package com.l2jhellas.gameserver.model.actor.status;

import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;

public class DoorStatus extends CharStatus
{
	public DoorStatus(L2DoorInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public L2DoorInstance getActiveChar()
	{
		return (L2DoorInstance) super.getActiveChar();
	}
}
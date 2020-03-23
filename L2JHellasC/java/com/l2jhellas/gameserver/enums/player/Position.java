package com.l2jhellas.gameserver.enums.player;

import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.position.Location;

public enum Position
{
	FRONT,
	SIDE,
	BACK;
	
	public static Position getPosition(L2Character activeChar, L2Character target)
	{
		final int heading = Math.abs(target.getHeading() - activeChar.calculateHeadingTo(target));
		if (((heading >= 0x2000) && (heading <= 0x6000)) || (Integer.toUnsignedLong(heading - 0xA000) <= 0x4000))
			return SIDE;
		else if (Integer.toUnsignedLong(heading - 0x2000) <= 0xC000)
			return FRONT;
		else
			return BACK;
	}
	
	public static Position getPosition(Location activeloc, L2Character target)
	{
		final int heading = Math.abs(target.getHeading() - activeloc.calculateHeadingTo(target));
		if (((heading >= 0x2000) && (heading <= 0x6000)) || (Integer.toUnsignedLong(heading - 0xA000) <= 0x4000))
			return SIDE;
		else if (Integer.toUnsignedLong(heading - 0x2000) <= 0xC000)
			return FRONT;
		else
			return BACK;
	}

	public static Position getPosition(Location activeloc, L2Object target)
	{
		final int heading = Math.abs(target.getHeading() - activeloc.calculateHeadingTo(target));
		if (((heading >= 0x2000) && (heading <= 0x6000)) || (Integer.toUnsignedLong(heading - 0xA000) <= 0x4000))
			return SIDE;
		else if (Integer.toUnsignedLong(heading - 0x2000) <= 0xC000)
			return FRONT;
		else
			return BACK;
	}
}

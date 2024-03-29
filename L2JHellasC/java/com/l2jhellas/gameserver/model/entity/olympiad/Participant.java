package com.l2jhellas.gameserver.model.entity.olympiad;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.templates.StatsSet;

public final class Participant
{
	public final int objectId;
	public L2PcInstance player;
	public final String name;
	public final int side;
	public final int baseClass;
	public boolean disconnected = false;
	public boolean defaulted = false;
	public final StatsSet stats;
	
	public Participant(L2PcInstance plr, int olympiadSide)
	{
		objectId = plr.getObjectId();
		player = plr;
		name = plr.getName();
		side = olympiadSide;
		baseClass = plr.getBaseClass();
		stats = Olympiad.getNobleStats(objectId);
	}
	
	public Participant(int objId, int olympiadSide)
	{
		objectId = objId;
		player = null;
		name = "-";
		side = olympiadSide;
		baseClass = 0;
		stats = null;
	}
	
	public final void updatePlayer()
	{
		if (player == null || !player.isOnline())
			player = L2World.getInstance().getPlayer(objectId);
	}
	
	public final void updateStat(String statName, int increment)
	{
		stats.set(statName, Math.max(stats.getInteger(statName) + increment, 0));
	}
}
package com.l2jhellas.gameserver.model.zone.type;

import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.L2SpawnZone;
import com.l2jhellas.gameserver.network.SystemMessageId;

public class L2ArenaZone extends L2SpawnZone
{
	public L2ArenaZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		
		character.setInsideZone(ZoneId.PVP, true);
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		
		if (character instanceof L2PcInstance)
		   ((L2PcInstance) character).sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(ZoneId.PVP, false);
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		
		if (character instanceof L2PcInstance)
		   ((L2PcInstance) character).sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
}
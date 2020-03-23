package com.l2jhellas.gameserver.model.zone.type;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;
import com.l2jhellas.gameserver.network.serverpackets.TutorialShowQuestionMark;

public class L2FishingZone extends L2ZoneType
{
	public L2FishingZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (Config.ALLOWFISHING && character instanceof L2PcInstance)
		{
			character.setInsideZone(ZoneId.FISHING, true);
			character.sendPacket(new TutorialShowQuestionMark(1994));
			((L2PcInstance) character).sendMessage("You have entered a fishing zone.");
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (Config.ALLOWFISHING && character instanceof L2PcInstance)
		{
			character.setInsideZone(ZoneId.FISHING, false);
			((L2PcInstance) character).sendMessage("You have exit a fishing zone.");
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}
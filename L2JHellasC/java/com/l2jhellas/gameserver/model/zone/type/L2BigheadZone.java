package com.l2jhellas.gameserver.model.zone.type;

import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;

public class L2BigheadZone extends L2ZoneType
{
	public L2BigheadZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
			character.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
			character.stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
		onExit(character);
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		onEnter(character);
	}
}
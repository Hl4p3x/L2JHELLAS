package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.skills.Env;

public final class EffectManaHealOverTime extends L2Effect
{
	public EffectManaHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.MANA_HEAL_OVER_TIME;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;
		
		double mp = getEffected().getCurrentMp();
		double maxmp = getEffected().getMaxMp();
		mp += calc();
		if (mp > maxmp)
		{
			mp = maxmp;
		}
		getEffected().setCurrentMp(mp);
		StatusUpdate sump = new StatusUpdate(getEffected().getObjectId());
		sump.addAttribute(StatusUpdate.CUR_MP, (int) mp);
		getEffected().sendPacket(sump);
		return true;
	}
}
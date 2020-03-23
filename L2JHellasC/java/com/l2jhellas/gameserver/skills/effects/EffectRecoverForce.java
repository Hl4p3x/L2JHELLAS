package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jhellas.gameserver.skills.Env;

public class EffectRecoverForce extends L2Effect
{
	public EffectRecoverForce(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SIGNET_EFFECT;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			EffectCharge effect = (EffectCharge) getEffected().getFirstEffect(EffectType.CHARGE);
			if (effect != null)
			{
				effect.addNumCharges(1);
				getEffected().sendPacket(new EtcStatusUpdate((L2PcInstance) getEffected()));
			}
		}
		return true;
	}
}
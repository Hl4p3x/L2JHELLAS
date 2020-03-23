package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jhellas.gameserver.network.serverpackets.StartRotation;
import com.l2jhellas.gameserver.network.serverpackets.StopRotation;
import com.l2jhellas.gameserver.skills.Env;

public final class EffectBluff extends L2Effect
{
	
	public EffectBluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BLUFF; // test for bluff effect
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2Npc)
			return false;
		if (getEffected() instanceof L2NpcInstance)
			return false;
		if (getEffected() instanceof L2Npc && ((L2Npc) getEffected()).getNpcId() == 35062 || getSkill().getId() != 358)
			return false;
		if (getEffected() instanceof L2SiegeSummonInstance)
			return false;
		
		getEffected().setTarget(null);
		getEffected().abortAttack();
		getEffected().abortCast();
		getEffected().broadcastPacket(new StartRotation(getEffected().getObjectId(), getEffected().getHeading(), 1, 65535));
		getEffected().setHeading(getEffector().getHeading());
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		getEffected().broadcastPacket(new StopRotation(getEffected().getObjectId(), getEffector().getHeading(), 65535));
		return false;
	}
}
package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.skills.Env;

public class EffectGrow extends L2Effect
{
	public EffectGrow(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2Npc)
		{
			L2Npc npc = (L2Npc) getEffected();
			npc.setCollisionHeight((int) (npc.getCollisionHeight() * 1.24));
			npc.setCollisionRadius((int) (npc.getCollisionRadius() * 1.19));
			getEffected().startAbnormalEffect(AbnormalEffect.GROW);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected() instanceof L2Npc)
		{
			L2Npc npc = (L2Npc) getEffected();
			npc.setCollisionHeight((int)npc.getTemplate().collisionHeight);
			npc.setCollisionRadius((int)npc.getTemplate().collisionRadius);
			
			getEffected().stopAbnormalEffect(AbnormalEffect.GROW);
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2Npc)
		{
			L2Npc npc = (L2Npc) getEffected();
			npc.setCollisionHeight((int)npc.getTemplate().collisionHeight);
			npc.setCollisionRadius((int)npc.getTemplate().collisionRadius);
			
			getEffected().stopAbnormalEffect(AbnormalEffect.GROW);
		}
	}
}
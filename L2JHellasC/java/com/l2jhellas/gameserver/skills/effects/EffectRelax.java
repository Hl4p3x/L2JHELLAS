package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Env;

public final class EffectRelax extends L2Effect
{
	public EffectRelax(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.RELAXING;
	}
	
	@Override
	public boolean onStart()
	{
		
		if (getEffected() instanceof L2PcInstance)
		{
			setRelax(true);
			((L2PcInstance) getEffected()).sitDown();
		}
		else
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		return super.onStart();
	}
	
	@Override
	public void onExit()
	{
		setRelax(false);
		super.onExit();
	}
	
	@Override
	public boolean onActionTime()
	{
		boolean retval = true;
		if (getEffected().isDead())
			retval = false;
		
		if (getEffected() instanceof L2PcInstance)
		{
			if (!((L2PcInstance) getEffected()).isSitting())
				retval = false;
		}
		
		if (getEffected().getCurrentHp() + 1 > getEffected().getMaxHp())
		{
			if (getSkill().isToggle())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
				sm.addString("Fully rested. Effect of " + getSkill().getName() + " has been removed.");
				getEffected().sendPacket(sm);
				retval = false;
			}
		}
		
		double manaDam = calc();
		
		if (manaDam > getEffected().getCurrentMp())
		{
			if (getSkill().isToggle())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
				getEffected().sendPacket(sm);
				// if (getEffected() instanceof L2PcInstance)
				// ((L2PcInstance)getEffected()).standUp();
				retval = false;
			}
		}
		
		if (!retval)
			setRelax(retval);
		else
			getEffected().reduceCurrentMp(manaDam);
		
		return retval;
	}
	
	private void setRelax(boolean val)
	{
		if (getEffected() instanceof L2PcInstance)
			((L2PcInstance) getEffected()).setRelax(val);
	}
}
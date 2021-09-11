package com.l2jhellas.gameserver.skills.effects;

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
		final L2PcInstance player = getEffected().getActingPlayer();
		if (player != null)
		{
			player.setRelax(true);
			player.sitDown();		
		}

		return super.onStart();
	}
	
	@Override
	public void onExit()
	{
		final L2PcInstance player = getEffected().getActingPlayer();
		if (player != null)
			player.setRelax(false);
		super.onExit();
	}
	
	@Override
	public boolean onActionTime()
	{
		final L2PcInstance player = getEffected().getActingPlayer();
		boolean retval = true;

		if (player != null)
		{		
			if (player.isDead() || !player.isSitting())
				retval = false;
			
			if (player.getCurrentHp() + 1 > player.getMaxHp())
			{
				if (getSkill().isToggle())
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2).addString("Fully rested. Effect of " + getSkill().getName() + " has been removed."));
					retval = false;
				}
			}
			
			double manaDam = calc();
			
			if (manaDam > player.getCurrentMp())
			{
				if (getSkill().isToggle())
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
					retval = false;
				}
			}
			
			if (!retval)
				player.setRelax(retval);
			else
				player.reduceCurrentMp(manaDam);			
		}
		
		return retval;
	}
}
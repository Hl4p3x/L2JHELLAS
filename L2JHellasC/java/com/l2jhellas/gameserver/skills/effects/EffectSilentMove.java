package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Env;

public final class EffectSilentMove extends L2Effect
{
	public EffectSilentMove(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		super.onStart();
		
		L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
		{
			((L2PcInstance) effected).setSilentMoving(true);
			return true;
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		
		L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
			((L2PcInstance) effected).setSilentMoving(false);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SILENT_MOVE;
	}
	
	@Override
	public boolean onActionTime()
	{
		// Only cont skills shouldn't end
		if (getSkill().getSkillType() != L2SkillType.CONT)
			return false;
		
		if (getEffected().isDead())
			return false;
		
		double manaDam = calc();
		
		if (manaDam > getEffected().getCurrentMp())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			getEffected().sendPacket(sm);
			return false;
		}
		
		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
}
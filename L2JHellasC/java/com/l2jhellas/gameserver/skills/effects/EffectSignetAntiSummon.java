package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.ai.CtrlEvent;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Env;

final class EffectSignetAntiSummon extends EffectSignet
{
	public EffectSignetAntiSummon(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SIGNET_GROUND;
	}
	
	@Override
	public boolean onActionTime()
	{
		int mpConsume = getSkill().getMpConsume();
		
		L2PcInstance caster = (L2PcInstance) getEffected();
		
		for (L2Character cha : zone.getCharactersInZone())
		{
			if (cha == null)
				continue;
			
			if (cha instanceof L2Playable)
			{
				L2PcInstance owner = null;
				
				if (cha instanceof L2Summon)
					owner = ((L2Summon) cha).getOwner();
				else
					owner = (L2PcInstance) cha;
				
				if ((owner != null) && (owner.getPet() != null))
				{
					if (mpConsume > caster.getCurrentMp())
					{
						caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
						return false;
					}
					caster.reduceCurrentMp(mpConsume);
					
					owner.getPet().unSummon(owner);
					owner.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
				}
			}
		}
		return true;
	}
}
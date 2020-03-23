package com.l2jhellas.gameserver.skills.l2skills;

import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.effects.EffectCharge;
import com.l2jhellas.gameserver.templates.StatsSet;

public class L2SkillCharge extends L2Skill
{
	public L2SkillCharge(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead())
			return;
		
		// get the effect
		EffectCharge effect = null;
		if (caster instanceof L2PcInstance)
		{
			effect = ((L2PcInstance) caster).getChargeEffect();
		}
		else
		{
			effect = (EffectCharge) caster.getFirstEffect(this);
		}
		if (effect != null)
		{
			if (effect.numCharges < getNumCharges())
			{
				effect.numCharges++;
				if (caster instanceof L2PcInstance)
				{
					caster.sendPacket(new EtcStatusUpdate((L2PcInstance) caster));
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
					sm.addNumber(effect.numCharges);
					caster.sendPacket(sm);
				}
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FORCE_MAXIMUM);
				caster.sendPacket(sm);
			}
			return;
		}
		getEffects(caster, caster);
		
		// effect self :]
		// L2Effect seffect = caster.getEffect(getId());
		// if (effect != null && seffect.isSelfEffect())
		// {
		// Replace old effect with new one.
		// seffect.exit();
		// }
		// cast self effect if any
		getEffectsSelf(caster);
	}
}
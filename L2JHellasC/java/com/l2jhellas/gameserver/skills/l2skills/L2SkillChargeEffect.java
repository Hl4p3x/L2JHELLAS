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

public class L2SkillChargeEffect extends L2Skill
{
	final int chargeSkillId;
	
	public L2SkillChargeEffect(StatsSet set)
	{
		super(set);
		chargeSkillId = set.getInteger("charge_skill_id");
	}
	
	@Override
	public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
	{
		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) activeChar;
			EffectCharge e = (EffectCharge) player.getFirstEffect(chargeSkillId);
			if (e == null || e.numCharges < getNumCharges())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(getId());
				activeChar.sendPacket(sm);
				return false;
			}
		}
		return super.checkCondition(activeChar, target, itemOrWeapon);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		// get the effect
		EffectCharge effect = (EffectCharge) activeChar.getFirstEffect(chargeSkillId);
		if (effect == null || effect.numCharges < getNumCharges())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(getId());
			activeChar.sendPacket(sm);
			return;
		}
		
		// decrease?
		effect.numCharges -= getNumCharges();
		
		// update icons
		// activeChar.updateEffectIcons();
		
		// maybe exit? no charge
		if (effect.numCharges == 0)
			effect.exit();
		
		// apply effects
		if (hasEffects())
			for (L2Object target : targets)
				getEffects(activeChar, (L2Character) target);
		if (activeChar instanceof L2PcInstance)
		{
			activeChar.sendPacket(new EtcStatusUpdate((L2PcInstance) activeChar));
		}
	}
}
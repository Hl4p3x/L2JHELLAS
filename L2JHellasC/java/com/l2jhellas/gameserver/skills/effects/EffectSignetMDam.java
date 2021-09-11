package com.l2jhellas.gameserver.skills.effects;

import java.util.ArrayList;

import com.l2jhellas.gameserver.ai.CtrlEvent;
import com.l2jhellas.gameserver.enums.items.ShotType;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AbstractNpcInfo.SummonInfo;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Formulas;

final class EffectSignetMDam extends EffectSignet
{
	private int _state = 0;
	
	public EffectSignetMDam(Env env, EffectTemplate template)
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
		// on offi the zone get created and the first wave starts later
		// there is also an first hit animation to the caster
		switch (_state)
		{
			case 0:
			case 2:
				_state++;
				return true;
			case 1:
				getEffected().broadcastPacket(new MagicSkillLaunched(getEffected(), getSkill().getId(), getSkill().getLevel(), new L2Object[]
				{
					getEffected()
				}));
				_state++;
				return true;
		}
		
		int mpConsume = getSkill().getMpConsume();
		
		L2PcInstance caster = (L2PcInstance) getEffected();
		
		final boolean sps = caster.isChargedShot(ShotType.SPIRITSHOT);
		final boolean bss = caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT);	

		if (bss)
			caster.setChargedShot(ShotType.BLESSED_SPIRITSHOT, false);
		else if (sps)
			caster.setChargedShot(ShotType.SPIRITSHOT, false);
		
		if (!bss && !sps)
			caster.rechargeShots(false, true);
		
		ArrayList<L2Character> targets = new ArrayList<>();
		
		for (L2Character cha : zone.getCharactersInZone())
		{
			if ((cha == null) || cha == getEffected())
				continue;
			
			if (cha instanceof L2Attackable || cha instanceof L2Playable)
			{
				if (cha.isAlikeDead())
					continue;
				
				if (mpConsume > caster.getCurrentMp())
				{
					caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
					return false;
				}
				
				caster.reduceCurrentMp(mpConsume);
				
				targets.add(cha);
			}
		}
		
		if (targets.size() > 0)
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getId(), getSkill().getLevel(), targets.toArray(new L2Character[targets.size()])));
			for (L2Character target : targets)
			{
				boolean mcrit = Formulas.calcMCrit(caster.getMCriticalHit(target, getSkill()));
				int mdam = (int) Formulas.calcMagicDam(caster, target, getSkill(), sps, bss, mcrit);
				
				if (target instanceof L2Summon)
				{
					caster.equals(((L2Summon) target).getOwner());
					caster.sendPacket(new SummonInfo((L2Summon) target, caster,0));
				}
				
				if (mdam > 0)
				{
					if (!target.isRaid() && !target.isBoss() && Formulas.calcAtkBreak(target, mdam))
					{
						target.breakAttack();
						target.breakCast();
					}
					caster.sendDamageMessage(target, mdam, mcrit, false, false);
					target.reduceCurrentHp(mdam, caster);
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
			}
		}
		return true;
	}
}
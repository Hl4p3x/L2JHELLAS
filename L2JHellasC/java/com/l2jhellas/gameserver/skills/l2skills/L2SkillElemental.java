package com.l2jhellas.gameserver.skills.l2skills;

import com.l2jhellas.gameserver.enums.items.ShotType;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.templates.StatsSet;

public class L2SkillElemental extends L2Skill
{
	private final int[] _seeds;
	private final boolean _seedAny;
	
	public L2SkillElemental(StatsSet set)
	{
		super(set);
		
		_seeds = new int[5];
		_seeds[0] = set.getInteger("seed1", 0);
		_seeds[1] = set.getInteger("seed2", 0);
		_seeds[2] = set.getInteger("seed3", 0);
		_seeds[3] = set.getInteger("seed4", 0);
		_seeds[4] = set.getInteger("seed5", 0);
		
		if (set.getInteger("seed_any", 0) == 1)
			_seedAny = true;
		else
			_seedAny = false;
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		
		if (activeChar instanceof L2PcInstance)
		{
			if (weaponInst == null)
			{
				SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
				sm2.addString("You must equip one weapon before cast spell.");
				activeChar.sendPacket(sm2);
				return;
			}
		}
		
		final boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
		final boolean bss = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);	

		if (bss)
			activeChar.setChargedShot(ShotType.BLESSED_SPIRITSHOT, false);
		else if (sps)
			activeChar.setChargedShot(ShotType.SPIRITSHOT, false);
		
		for (L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;
			if (target.isAlikeDead())
				continue;
			
			boolean charged = true;
			if (!_seedAny)
			{
				for (int _seed : _seeds)
				{
					if (_seed != 0)
					{
						L2Effect e = target.getFirstEffect(_seed);
						if (e == null || !e.getInUse())
						{
							charged = false;
							break;
						}
					}
				}
			}
			else
			{
				charged = false;
				for (int _seed : _seeds)
				{
					if (_seed != 0)
					{
						L2Effect e = target.getFirstEffect(_seed);
						if (e != null && e.getInUse())
						{
							charged = true;
							break;
						}
					}
				}
			}
			if (!charged)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
				sm.addString("Target is not charged by elements.");
				activeChar.sendPacket(sm);
				continue;
			}
			
			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			
			int damage = (int) Formulas.calcMagicDam(activeChar, target, this, sps, bss, mcrit);
			
			if (damage > 0)
			{
				target.reduceCurrentHp(damage, activeChar);
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && !target.isBoss() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				activeChar.sendDamageMessage(target, damage, false, false, false);
			}
			// activate attacked effects, if any
			target.stopSkillEffects(getId());
			getEffects(activeChar, target);
		}
	}
}
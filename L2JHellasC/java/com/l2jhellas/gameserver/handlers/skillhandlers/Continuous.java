package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.ai.CtrlEvent;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.items.ShotType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.instancemanager.DuelManager;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;

public class Continuous implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.BUFF,
		L2SkillType.DEBUFF,
		L2SkillType.DOT,
		L2SkillType.MDOT,
		L2SkillType.POISON,
		L2SkillType.BLEED,
		L2SkillType.HOT,
		L2SkillType.CPHOT,
		L2SkillType.MPHOT,
		L2SkillType.FEAR,
		L2SkillType.CONT,
		L2SkillType.WEAKNESS,
		L2SkillType.REFLECT,
		L2SkillType.UNDEAD_DEFENSE,
		L2SkillType.AGGDEBUFF,
		L2SkillType.FORCE_BUFF
	};
	private L2PcInstance player;
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2Character target = null;
		
		player = null;
		if (activeChar instanceof L2PcInstance)
			player = (L2PcInstance) activeChar;
		
		for (L2Object target2 : targets)
		{
			target = (L2Character) target2;
			
			if (target == null)
				continue;
			
			if (Formulas.calcSkillReflect(target, skill) == 1)
				target = activeChar;
			
			// Walls and Door should not be buffed
			if (target instanceof L2DoorInstance && (skill.getSkillType() == L2SkillType.BUFF || skill.getSkillType() == L2SkillType.HOT))
				continue;
			
			// Player holding a cursed weapon can't be buffed and can't buff
			if (skill.getSkillType() == L2SkillType.BUFF)
			{
				if (target != activeChar)
				{
					if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquiped())
						continue;
					else if (player != null && player.isCursedWeaponEquiped())
						continue;
				}
			}
			
			if (skill.isOffensive())
			{				
				final boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
				final boolean bss = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);						
				boolean acted = Formulas.calcSkillSuccess(activeChar, target, skill, false, sps, bss);
				
				if(sps && skill.getId() != 1020)
					activeChar.setChargedShot(ShotType.SPIRITSHOT, false);
				else if (bss && skill.getId() != 1020)
					activeChar.setChargedShot(ShotType.BLESSED_SPIRITSHOT , false);
				
				if (!acted)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
					continue;
				}							
			}
			boolean stopped = false;
			
			if (target == null)
				continue;
			
			if (target.getAllEffects() != null)
			{
				for (L2Effect e : target.getAllEffects())
				{
					if (e != null)
						if (e.getSkill().getId() == skill.getId())
						{
							e.exit();
							stopped = true;
						}
				}
			}
			if (skill.isToggle() && stopped)
				return;
			
			// if this is a debuff let the duel manager know about it
			// so the debuff can be removed after the duel
			// (player & target must be in the same duel)
			if (target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() && (skill.getSkillType() == L2SkillType.DEBUFF || skill.getSkillType() == L2SkillType.BUFF) && player.getDuelId() == ((L2PcInstance) target).getDuelId())
			{
				DuelManager dm = DuelManager.getInstance();
				for (L2Effect buff : skill.getEffects(activeChar, target))
					if (buff != null)
						dm.onBuff(((L2PcInstance) target), buff);
			}
			else
				skill.getEffects(activeChar, target);
			
			if (skill.getSkillType() == L2SkillType.AGGDEBUFF)
			{
				if (target instanceof L2Attackable)
					target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
				else if (target instanceof L2Playable)
				{
					if (target.getTarget() == activeChar)
						target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
					else
						target.setTarget(activeChar);
				}
			}
		}
		// self Effect :]
		if (skill.hasSelfEffects())
		{
			final L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
				effect.exit();
			
			skill.getEffectsSelf(activeChar);
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
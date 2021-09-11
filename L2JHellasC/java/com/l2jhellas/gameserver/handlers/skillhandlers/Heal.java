package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.enums.items.ShotType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.handler.SkillHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Stats;

public class Heal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.HEAL,
		L2SkillType.HEAL_PERCENT,
		L2SkillType.HEAL_STATIC
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		// L2Character activeChar = activeChar;
		// check for other effects
		try
		{
			ISkillHandler handler = SkillHandler.getInstance().getHandler(L2SkillType.BUFF);
			
			if (handler != null)
				handler.useSkill(activeChar, skill, targets);
		}
		catch (Exception e)
		{
		}
		
		L2Character target = null;
		
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
			player = (L2PcInstance) activeChar;
		
		for (L2Object target2 : targets)
		{
			target = (L2Character) target2;
			
			// We should not heal if char is dead
			if (target == null || target.isDead())
				continue;
			
			// We should not heal walls and door
			if (target instanceof L2DoorInstance)
				continue;
			
			// We should not heal siege flags
			if (target instanceof L2Npc && ((L2Npc) target).getNpcId() == 35062)
			{
				activeChar.getActingPlayer().sendMessage("You cannot heal siege flags!");
				continue;
			}
			// Zodiac Flag
			if (target instanceof L2Npc && ((L2Npc) target).getNpcId() == 36006)
			{
				activeChar.getActingPlayer().sendMessage("You cannot heal event flags!");
				continue;
			}
			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar)
			{
				if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquiped())
					continue;
				else if (player != null && player.isCursedWeaponEquiped())
					continue;
			}
			
			double hp = skill.getPower();
			
			if (skill.getSkillType() == L2SkillType.HEAL_PERCENT)
			{
				hp = target.getMaxHp() * hp / 100.0;
			}
			else
			{
				final boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
				final boolean bss = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);	

				if (bss)
				{
					hp *= 1.5;
					activeChar.setChargedShot(ShotType.BLESSED_SPIRITSHOT, false);
				}
				else if (sps)
				{
					hp *= 1.3;
					activeChar.setChargedShot(ShotType.SPIRITSHOT, false);
				}
			}
			
			if(target.isDead())
				continue;
			
			// int cLev = activeChar.getLevel();
			// hp += skill.getPower();
			if (skill.getSkillType() == L2SkillType.HEAL_STATIC)
				hp = skill.getPower();
			else if (skill.getSkillType() != L2SkillType.HEAL_PERCENT)
				hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
			hp *= activeChar.calcStat(Stats.HEAL_PROFICIENCY, 100, null, null) / 100; // Healer proficiency (since CT1)
			
			target.setCurrentHp(hp + target.getCurrentHp());
			target.setLastHealAmount((int) hp);
			StatusUpdate su = new StatusUpdate(target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
			
			if (target instanceof L2PcInstance)
			{
				if (skill.getId() == 4051)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REJUVENATING_HP);
					target.sendPacket(sm);
				}
				else
				{
					if (activeChar instanceof L2PcInstance && activeChar != target)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1);
						sm.addString(activeChar.getName());
						sm.addNumber((int) hp);
						target.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
						sm.addNumber((int) hp);
						target.sendPacket(sm);
					}
				}
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;

public class Manadam implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.MANADAM
	};
	private L2Character target;
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		target = null;
		
		if (activeChar.isAlikeDead())
			return;
		
		boolean ss = false;
		boolean bss = false;
		
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
		}
		for (L2Object target2 : targets)
		{
			target = (L2Character) target2;
			
			if(target==null)
				continue;
			
			if (Formulas.calcSkillReflect(target, skill) == 1)
				target = activeChar;
			
			boolean acted = Formulas.calcMagicAffected(activeChar, target, skill);
			if (target.isInvul() || !acted)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MISSED_TARGET));
			}
			else
			{
				double damage = Formulas.calcManaDam(activeChar, target, skill, ss, bss);
				
				double mp = (damage > target.getCurrentMp() ? target.getCurrentMp() : damage);
				target.reduceCurrentMp(mp);
				if (damage > 0)
					if (target.isSleeping())
						target.stopSleeping(null);
				
				StatusUpdate sump = new StatusUpdate(target.getObjectId());
				sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
				target.sendPacket(sump);
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_S1);
				if (activeChar instanceof L2Npc)
				{
					int mobId = ((L2Npc) activeChar).getNpcId();
					sm.addNpcName(mobId);
				}
				else if (activeChar instanceof L2Summon)
				{
					int mobId = ((L2Summon) activeChar).getNpcId();
					sm.addNpcName(mobId);
				}
				else
				{
					sm.addString(activeChar.getName());
				}
				sm.addNumber((int) mp);
				target.sendPacket(sm);
				if (activeChar instanceof L2PcInstance)
				{
					SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1);
					sm2.addNumber((int) mp);
					activeChar.sendPacket(sm2);
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
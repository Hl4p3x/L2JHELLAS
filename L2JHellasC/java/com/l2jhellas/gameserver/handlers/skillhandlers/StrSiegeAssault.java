package com.l2jhellas.gameserver.handlers.skillhandlers;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;

public class StrSiegeAssault implements ISkillHandler
{
	private static Logger _log = Logger.getLogger(StrSiegeAssault.class.getName());
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.STRSIEGEASSAULT
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{		
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (!player.isRiding())
			return;
		
		if (!(player.getTarget() instanceof L2DoorInstance))
			return;
		
		Castle castle = CastleManager.getInstance().getCastle(player);
		if (castle == null || !player.checkIfOkToUseStriderSiegeAssault(player, castle, true))
			return;
		
		try
		{
			L2ItemInstance itemToTake = player.getInventory().getItemByItemId(skill.getItemConsumeId());
			if (!player.destroyItem("Consume", itemToTake.getObjectId(), skill.getItemConsume(), null, true))
				return;
			
			// damage calculation
			int damage = 0;
			
			for (L2Object target2 : targets)
			{
				L2Character target = (L2Character) target2;
				
				if(target == null)
				   continue;
				
				if (target.isDead())
					continue;
				
				L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				
				if (target.isPlayer() && target.isFakeDeath())
					target.getActingPlayer().stopFakeDeath(null);
				
				boolean dual = activeChar.isUsingDualWeapon();
				byte shld = Formulas.calcShldUse(activeChar, target);
				boolean crit = Formulas.calcCrit(activeChar.getCriticalHit(target, skill));
				boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER);
				
				if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
					damage = 0;
				else
					damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);
				
				if (damage > 0)
				{
					target.reduceCurrentHp(damage, activeChar);
					if (soul && weapon != null)
						weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
					
					activeChar.sendDamageMessage(target, damage, false, false, false);
					
				}
				else
					activeChar.sendPacket(SystemMessage.sendString(skill.getName() + " failed."));
			}
		}
		catch (Exception e)
		{
			_log.warning(StrSiegeAssault.class.getName() + ": Error using siege assault:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
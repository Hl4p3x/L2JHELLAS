package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Fishing;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;

public class FishingSkill implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.PUMPING,
		L2SkillType.REELING
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return;
		
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		final boolean isReelingSkill = skill.getSkillType() == L2SkillType.REELING;
		
		final L2Fishing fish = player.GetFishCombat();
		
		if (fish == null)
		{
			player.sendPacket((isReelingSkill) ? SystemMessageId.CAN_USE_REELING_ONLY_WHILE_FISHING : SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2ItemInstance fishingRod = activeChar.getActiveWeaponInstance();
		
		if (fishingRod == null || fishingRod.getItem().getItemType() != L2WeaponType.ROD)
			return;
		
		final int ssBonus = (fishingRod.getChargedFishshot()) ? 2 : 1;
		final double gradeBonus = 1 + fishingRod.getItem().getCrystalType() * 0.1;
		int damage = (int) (skill.getPower() * gradeBonus * ssBonus);
		int penalty = 0;
		
		if (skill.getLevel() - player.getSkillLevel(1315) >= 3)
		{
			penalty = 50;
			damage -= penalty;
			
			player.sendPacket(SystemMessageId.REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY);
		}
		
		if (ssBonus > 1)
			fishingRod.setChargedFishshot(false);
		
		if (isReelingSkill)
			fish.useRealing(damage, penalty);
		else
			fish.usePomping(damage, penalty);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.controllers.RecipeController;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;

public class Craft implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.COMMON_CRAFT,
		L2SkillType.DWARVEN_CRAFT
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return;
		
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		if (player.getPrivateStoreType() != StoreType.NONE)
		{
			player.sendPacket(SystemMessageId.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING);
			return;
		}
		
		RecipeController.getInstance().requestBookOpen(player, (skill.getSkillType() == L2SkillType.DWARVEN_CRAFT) ? true : false);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
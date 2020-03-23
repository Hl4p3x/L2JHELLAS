package com.l2jhellas.gameserver.handlers.skillhandlers;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.holder.IntIntHolder;
import com.l2jhellas.gameserver.model.L2ExtractableProductItem;
import com.l2jhellas.gameserver.model.L2ExtractableSkill;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.util.Rnd;

public class Extractable implements ISkillHandler
{
	private static Logger _log = Logger.getLogger(Extractable.class.getName());
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.EXTRACTABLE,
		L2SkillType.EXTRACTABLE_FISH
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		final L2ExtractableSkill exItem = skill.getExtractableSkill();
		if (exItem == null || exItem.getProductItemsArray().isEmpty())
		{
			_log.warning("Missing informations for extractable skill id: " + skill.getId() + ".");
			return;
		}
		
		final L2PcInstance player = activeChar.getActingPlayer();
		final int chance = Rnd.get(100000);
		
		boolean created = false;
		int chanceIndex = 0;
		
		for (L2ExtractableProductItem expi : exItem.getProductItemsArray())
		{
			chanceIndex += (int) (expi.getChance() * 1000);
			if (chance <= chanceIndex)
			{
				for (IntIntHolder item : expi.getItems())
					player.addItem("extract", item.getId(), item.getValue(), targets[0], true);
				
				created = true;
				break;
			}
		}
		
		if (!created)
		{
			player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
			return;
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
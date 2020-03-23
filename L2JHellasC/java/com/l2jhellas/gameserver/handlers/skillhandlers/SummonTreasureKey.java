package com.l2jhellas.gameserver.handlers.skillhandlers;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.util.Rnd;

public class SummonTreasureKey implements ISkillHandler
{
	protected static final Logger _log = Logger.getLogger(SummonTreasureKey.class.getName());
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SUMMON_TREASURE_KEY
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance) activeChar;
		try
		{
			int item_id = 0;
			
			switch (skill.getLevel())
			{
				case 1:
				{
					item_id = Rnd.get(6667, 6669);
					break;
				}
				case 2:
				{
					item_id = Rnd.get(6668, 6670);
					break;
				}
				case 3:
				{
					item_id = Rnd.get(6669, 6671);
					break;
				}
				case 4:
				{
					item_id = Rnd.get(6670, 6672);
					break;
				}
			}
			player.addItem("Skill", item_id, Rnd.get(2, 3), player, false);
		}
		catch (Exception e)
		{
			_log.warning(SummonTreasureKey.class.getName() + ": Error using skill summon Treasure Key:");
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
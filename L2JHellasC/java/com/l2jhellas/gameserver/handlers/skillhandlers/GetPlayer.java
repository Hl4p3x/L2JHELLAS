package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.FlyToLocation;
import com.l2jhellas.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.l2jhellas.gameserver.network.serverpackets.ValidateLocation;

public class GetPlayer implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.GET_PLAYER
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		for (L2Object target : targets)
		{
			if (target instanceof L2PcInstance)
			{
				L2PcInstance trg = (L2PcInstance) target;
				if (trg.isAlikeDead())
					continue;
				
				trg.stopMove(null);
				trg.abortAttack();
				trg.abortCast();
				
				trg.broadcastPacket(new FlyToLocation(trg, activeChar, FlyType.DUMMY));
				trg.setXYZ(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				trg.broadcastPacket(new ValidateLocation(trg));
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
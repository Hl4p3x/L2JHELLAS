package com.l2jhellas.gameserver.handler;

import java.io.IOException;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;

public interface ISkillHandler
{
	
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets) throws IOException;
	
	public L2SkillType[] getSkillIds();
}
package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Util;

public class TakeCastle implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.TAKECASTLE
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId())
			return;
		
		Castle castle = CastleManager.getInstance().getCastle(player);
		if (castle == null || !checkIfOkToCastSealOfRule(player, castle, true))
			return;
		
		try
		{
			if (targets[0] instanceof L2ArtefactInstance)
				castle.Engrave(player.getClan(), targets[0].getObjectId());
		}
		catch (Exception e)
		{
		}
	}
	
	public static boolean checkIfOkToCastSealOfRule(L2Character activeChar, boolean isCheckOnly)
	{
		return checkIfOkToCastSealOfRule(activeChar, CastleManager.getInstance().getCastle(activeChar), isCheckOnly);
	}
	
	public static boolean checkIfOkToCastSealOfRule(L2Character activeChar, Castle castle, boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return false;
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (castle == null || castle.getCastleId() <= 0)
			sm.addString("You must be on castle ground to use this skill");
		else if (player.getTarget() == null && !(player.getTarget() instanceof L2ArtefactInstance))
			sm.addString("You can only use this skill on an artifact");
		else if (!castle.getSiege().getIsInProgress())
			sm.addString("You can only use this skill during a siege.");
		else if (!Util.checkIfInRange(200, player, player.getTarget(), true))
			sm.addString("You are not in range of the artifact.");
		else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
			sm.addString("You must be an attacker to use this skill");
		else
		{
			if (!isCheckOnly)
				castle.getSiege().announceToPlayer("Clan " + player.getClan().getName() + " has begun to engrave the ruler.", true);
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
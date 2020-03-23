package com.l2jhellas.gameserver.handlers.skillhandlers;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jhellas.gameserver.model.entity.Castle;

public class SiegeFlag implements ISkillHandler
{
	private static Logger _log = Logger.getLogger(SiegeFlag.class.getName());
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SIEGEFLAG
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
		
		if (castle == null || !activeChar.checkIfOkToPlaceFlag(player, castle, true))
			return;
		
		try
		{
			// Spawn a new flag
			L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcData.getInstance().getTemplate(35062));
			flag.setTitle(player.getClan().getName());
			flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			flag.setHeading(player.getHeading());
			flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			castle.getSiege().getFlag(player.getClan()).add(flag);
		}
		catch (Exception e)
		{
			_log.warning(SiegeFlag.class.getName() + ": Error placing flag:");
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
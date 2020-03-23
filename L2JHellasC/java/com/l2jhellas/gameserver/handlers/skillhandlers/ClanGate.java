package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class ClanGate implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.CLAN_GATE
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}
		else
		{
			return;
		}
		
		if (player.isInFunEvent() || player.isInsideZone(ZoneId.NO_LANDING) || player.isInOlympiadMode() || player.isInsideZone(ZoneId.PVP) || GrandBossManager.getZone(player) != null)
		{
			player.sendMessage("Cannot open the portal here.");
			return;
		}
		
		L2Clan clan = player.getClan();
		if (clan != null)
		{
			if (CastleManager.getInstance().getCastleByOwner(clan) != null)
			{
				Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
				if (player.isCastleLord(castle.getCastleId()))
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new RemoveClanGate(castle.getCastleId(), player), skill.getTotalLifeTime());
					castle.createClanGate(player.getX(), player.getY(), player.getZ() + 20);
					player.getClan().broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.COURT_MAGICIAN_CREATED_PORTAL));
					player.setIsParalyzed(true);
				}
			}
		}
		
		L2Effect effect = player.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			effect.exit();
		}
		skill.getEffectsSelf(player);
	}
	
	private class RemoveClanGate implements Runnable
	{
		private final int castle;
		private final L2PcInstance player;
		
		RemoveClanGate(int castle, L2PcInstance player)
		{
			this.castle = castle;
			this.player = player;
		}
		
		@Override
		public void run()
		{
			if (player != null)
			{
				player.setIsParalyzed(false);
			}
			
			CastleManager.getInstance().getCastleById(castle).destroyClanGate();
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class Recall implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.RECALL
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar instanceof L2PcInstance)
		{
			if (((L2PcInstance) activeChar).isInOlympiadMode())
			{
				((L2PcInstance) activeChar).sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
				return;
			}
		}
		
		try
		{
			for (int index = 0; index < targets.length; index++)
			{
				if (!(targets[index] instanceof L2Character))
					continue;
				
				L2Character target = (L2Character) targets[index];
				
				if (target instanceof L2PcInstance)
				{
					L2PcInstance targetChar = (L2PcInstance) target;
					
					// Check to see if the current player target is in a
					// festival.
					if (targetChar.isFestivalParticipant())
					{
						targetChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
						continue;
					}
					
					// Check to see if the current player target is in TvT , CTF
					// or ViP events.
					if (targetChar.isInFunEvent())
					{
						targetChar.sendMessage("You may not use an escape skill in a Event.");
						continue;
					}
					
					// Check to see if player is in jail
					if (targetChar.isInJail())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
						continue;
					}
					
					// Check to see if player is in a duel
					if (targetChar.isInDuel())
					{
						targetChar.sendPacket(SystemMessage.sendString("You cannot use escape skills during a duel."));
						continue;
					}
				}
				
				target.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
			}
		}
		catch (Throwable e)
		{
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
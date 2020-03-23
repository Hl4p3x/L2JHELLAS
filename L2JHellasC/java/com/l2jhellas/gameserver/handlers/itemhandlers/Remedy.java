package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;

public class Remedy implements IItemHandler
{
	private static int[] ITEM_IDS =
	{
		1831,
		1832,
		1833,
		1834,
		3889
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance) playable;
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance) playable).getOwner();
		else
			return;
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}
		
		int itemId = item.getItemId();
		if (itemId == 1831) // antidote
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
			{
				if (e.getSkill().getSkillType() == L2SkillType.POISON && e.getSkill().getLevel() <= 3)
				{
					e.exit();
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2042, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1832) // advanced antidote
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
			{
				if (e.getSkill().getSkillType() == L2SkillType.POISON && e.getSkill().getLevel() <= 7)
				{
					e.exit();
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2043, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1833) // bandage
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
			{
				if (e.getSkill().getSkillType() == L2SkillType.BLEED && e.getSkill().getLevel() <= 3)
				{
					e.exit();
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 34, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1834) // emergency dressing
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
			{
				if (e.getSkill().getSkillType() == L2SkillType.BLEED && e.getSkill().getLevel() <= 7)
				{
					e.exit();
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2045, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 3889) // potion of recovery
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
			{
				if (e.getSkill().getId() == 4082)
					e.exit();
			}
			activeChar.setIsImmobilized(false);
			if (activeChar.getFirstEffect(L2Effect.EffectType.ROOT) == null)
				activeChar.stopRooting(null);
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2042, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
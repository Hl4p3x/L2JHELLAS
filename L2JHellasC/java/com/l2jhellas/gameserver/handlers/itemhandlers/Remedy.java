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
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class Remedy implements IItemHandler 
{
	private static int[] ITEM_IDS = { 1831, 1832, 1833, 1834, 3889 };

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

		if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null,false)) 
		{
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return;
		}

		int itemId = item.getItemId();
		L2Effect[] effects = activeChar.getAllEffects();

		switch (itemId)
		{
		case 1831: // antidote
			for (L2Effect e : effects)
			{
				if (e.getSkill().getSkillType() == L2SkillType.POISON && e.getSkill().getLevel() <= 3)
				{
					e.exit();
					break;
				}
			}
			activeChar.broadcastPacket(new MagicSkillUse(activeChar,activeChar, 2042, 1, 0, 0));
			break;
		case 1832: // advanced antidote
			for (L2Effect e : effects)
			{
				if (e.getSkill().getSkillType() == L2SkillType.POISON && e.getSkill().getLevel() <= 7)
				{
					e.exit();
					break;
				}
			}
			activeChar.broadcastPacket(new MagicSkillUse(activeChar,activeChar, 2043, 1, 0, 0));
			break;
		case 1833: // bandage
			for (L2Effect e : effects)
			{
				if (e.getSkill().getSkillType() == L2SkillType.BLEED && e.getSkill().getLevel() <= 3)
				{
					e.exit();
					break;
				}
			}
			activeChar.broadcastPacket(new MagicSkillUse(activeChar,activeChar, 34, 1, 0, 0));
			break;
		case 1834: // emergency dressing
			for (L2Effect e : effects) 
			{
				if (e.getSkill().getSkillType() == L2SkillType.BLEED && e.getSkill().getLevel() <= 7)
				{
					e.exit();
					break;
				}
			}
			activeChar.broadcastPacket(new MagicSkillUse(activeChar,activeChar, 2045, 1, 0, 0));
			break;
		case 3889: // potion of recovery
			for (L2Effect e : effects) 
			{
				if (e.getSkill().getId() == 4082)
					e.exit();
			}
			activeChar.setIsImmobilized(false);

			if (activeChar.getFirstEffect(L2Effect.EffectType.ROOT) == null)
				activeChar.stopRooting(null);

			activeChar.broadcastPacket(new MagicSkillUse(activeChar,activeChar, 2042, 1, 0, 0));
			break;
		}

		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED).addItemName(itemId));
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
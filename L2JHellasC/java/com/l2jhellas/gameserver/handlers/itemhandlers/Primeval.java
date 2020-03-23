package com.l2jhellas.gameserver.handlers.itemhandlers;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;

public class Primeval implements IItemHandler
{
	protected static final Logger _log = Logger.getLogger(Crystals.class.getName());
	
	private static final int[] ITEM_IDS =
	{
		8786,
		8787
	};
	
	@Override
	public synchronized void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		
		if (playable instanceof L2PcInstance)
		{
			activeChar = (L2PcInstance) playable;
		}
		else if (playable instanceof L2PetInstance)
		{
			activeChar = ((L2PetInstance) playable).getOwner();
		}
		else
		{
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		if (activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		int itemId = item.getItemId();
		L2Skill skill = null;
		
		switch (itemId)
		{
			case 8786:
				skill = SkillTable.getInstance().getInfo(2305, 1);
				break;
			// Springant's Fruit need correct skill id (effect primeval potions is a temp fix).
			case 8787:
				skill = SkillTable.getInstance().getInfo(2305, 1);
				break;
			default:
		}
		
		if (skill != null)
		{
			activeChar.doCast(skill);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
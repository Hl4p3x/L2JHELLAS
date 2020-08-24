package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class CrystalCarol implements IItemHandler
{
	private static final int[] ITEM_IDS = 
	{ 
		5562, 5563, 5564, 5565, 5566, 5583,
		5584, 5585, 5586, 5587, 4411, 4412, 4413, 4414, 4415, 4416, 4417,
		5010, 6903, 7061, 7062, 8555 
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable.isPlayer()))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		int itemId = item.getItemId();	

		if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null,false)) 
		{
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return;
		}
		
		switch (itemId)
		{
		    case 5562:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2140, 1, 1, 0), 1500);
			    break;
		    case 5563:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2141, 1, 1, 0), 1500);
		        break;
		    case 5564:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2142, 1, 1, 0), 1500);
		        break;
		    case 5565:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2143, 1, 1, 0), 1500);
		        break;
		    case 5566:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2144, 1, 1, 0), 1500);
		        break;
		    case 5583:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2145, 1, 1, 0), 1500);
		        break;
		    case 5584:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2146, 1, 1, 0), 1500);
		    	break;
		    case 5585:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2147, 1, 1, 0), 1500);
		    	break;
		    case 5586:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2148, 1, 1, 0), 1500);
		    	break;
		    case 5587:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2149, 1, 1, 0), 1500);
		    	break;
		    case 4411:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2069, 1, 1, 0), 1500);
		    	break;
		    case 4412:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2068, 1, 1, 0), 1500);
		    	break;
		    case 4413:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2070, 1, 1, 0), 1500);
		    	break;
		    case 4414:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2072, 1, 1, 0), 1500);
		    	break;
		    case 4415:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2071, 1, 1, 0), 1500);
		    	break;
		    case 4416:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2073, 1, 1, 0), 1500);
		    	break;
		    case 4417:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2067, 1, 1, 0), 1500);
		    	break;
		    case 5010:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2066, 1, 1, 0), 1500);
		    	break;
		    case 6903:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2187, 1, 1, 0), 1500);
		    	break;
		    case 7061:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2073, 1, 1, 0), 1500);
		    	break;
		    case 7062:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2230, 1, 1, 0), 1500);
		    	break;
		    case 8555:
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, 2272, 1, 1, 0), 1500);
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
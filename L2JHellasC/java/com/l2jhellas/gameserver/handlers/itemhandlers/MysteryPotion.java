package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class MysteryPotion implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5234
	};
	private static final int BIGHEAD_EFFECT = 0x2000;
	private static final int MYSTERY_POTION_SKILL = 2103;
	private static final int EFFECT_DURATION = 1200000; // 20 mins
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance) playable;
		// item.getItem().getEffects(item, activeChar);
		
		// Use a summon skill effect for fun ;)
		MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2103, 1, 0, 0);
		activeChar.sendPacket(MSU);
		activeChar.broadcastPacket(MSU, 1000);
		
		activeChar.startAbnormalEffect(BIGHEAD_EFFECT);
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1);
		sm.addSkillName(MYSTERY_POTION_SKILL);
		activeChar.sendPacket(sm);
		
		MysteryPotionStop mp = new MysteryPotionStop(playable);
		ThreadPoolManager.getInstance().scheduleEffect(mp, EFFECT_DURATION);
	}
	
	public class MysteryPotionStop implements Runnable
	{
		private final L2Playable _playable;
		
		public MysteryPotionStop(L2Playable playable)
		{
			_playable = playable;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (!(_playable instanceof L2PcInstance))
					return;
				
				((L2PcInstance) _playable).stopAbnormalEffect(BIGHEAD_EFFECT);
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
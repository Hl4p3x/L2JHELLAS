package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;

public class SoulCrystals implements IItemHandler
{
	// First line is for Red Soul Crystals, second is Green and third is Blue
	// Soul Crystals,
	// ordered by ascending level, from 0 to 13...
	private static final int[] ITEM_IDS =
	{
		4629,
		4630,
		4631,
		4632,
		4633,
		4634,
		4635,
		4636,
		4637,
		4638,
		4639,
		5577,
		5580,
		5908,
		4640,
		4641,
		4642,
		4643,
		4644,
		4645,
		4646,
		4647,
		4648,
		4649,
		4650,
		5578,
		5581,
		5911,
		4651,
		4652,
		4653,
		4654,
		4655,
		4656,
		4657,
		4658,
		4659,
		4660,
		4661,
		5579,
		5582,
		5914
	};
	
	// Our main method, where everything goes on
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2Object target = activeChar.getTarget();
		if (!(target instanceof L2MonsterInstance))
		{
			// Send a System Message to the caster
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(sm);
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			
			return;
		}
		
		if (activeChar.isParalyzed())
		{
			activeChar.sendMessage("You cannot use this while you are paralyzed.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// u can use soul crystal only when target hp goes below 50%
		if (((L2MonsterInstance) target).getCurrentHp() > ((L2MonsterInstance) target).getMaxHp() / 2.0)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		int crystalId = item.getItemId();
		
		// Soul Crystal Casting section
		L2Skill skill = SkillTable.getInstance().getInfo(2096, 1);
		activeChar.useMagic(skill, false, true);
		// End Soul Crystal Casting section
		
		// Continue execution later
		CrystalFinalizer cf = new CrystalFinalizer(activeChar, target, crystalId);
		ThreadPoolManager.getInstance().scheduleEffect(cf, skill.getHitTime());
		
	}
	
	static class CrystalFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2Attackable _target;
		
		CrystalFinalizer(L2PcInstance activeChar, L2Object target, int crystalId)
		{
			_activeChar = activeChar;
			_target = (L2Attackable) target;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead() || _target.isDead())
				return;
			_activeChar.enableAllSkills();
			try
			{
				_target.addAbsorber(_activeChar);
				_activeChar.setTarget(_target);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
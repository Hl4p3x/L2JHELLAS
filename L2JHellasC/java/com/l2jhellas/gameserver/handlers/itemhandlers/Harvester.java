package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.instancemanager.CastleManorManager;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.skills.SkillTable;

public class Harvester implements IItemHandler
{
	
	private static final int[] ITEM_IDS =
	{
		5125
	};
	L2PcInstance _activeChar;
	L2MonsterInstance _target;
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance _item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		if (CastleManorManager.getInstance().isDisabled())
			return;
		
		_activeChar = (L2PcInstance) playable;
		
		if (_activeChar.getTarget() == null || !(_activeChar.getTarget() instanceof L2MonsterInstance))
		{
			_activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		_target = (L2MonsterInstance) _activeChar.getTarget();
		
		if (_target == null || !_target.isDead())
		{
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Skill skill = SkillTable.getInstance().getInfo(2098, 1); // harvesting skill
		_activeChar.useMagic(skill, false, false);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
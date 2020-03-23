package com.l2jhellas.gameserver.skills.l2skills;

import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.Rnd;

public class L2SkillCreateItem extends L2Skill
{
	private final int[] _createItemId;
	private final int _createItemCount;
	private final int _randomCount;
	private L2ItemInstance _item;
	
	public L2SkillCreateItem(StatsSet set)
	{
		super(set);
		_createItemId = set.getIntegerArray("create_item_id");
		_createItemCount = set.getInteger("create_item_count", 0);
		_randomCount = set.getInteger("random_count", 1);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		if ((_createItemId == null) || (_createItemCount == 0))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			activeChar.sendPacket(sm);
			return;
		}
		L2PcInstance player = (L2PcInstance) activeChar;
		if (activeChar instanceof L2PcInstance)
		{
			int rnd = Rnd.nextInt(_randomCount) + 1;
			int count = _createItemCount * rnd;
			int rndid = Rnd.nextInt(_createItemId.length);
			giveItems(player, _createItemId[rndid], count);
		}
	}
	
	public void giveItems(L2PcInstance activeChar, int itemId, int count)
	{
		_item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		
		if (_item != null)
		{
			_item.setCount(count);
			activeChar.getInventory().addItem("Skill", _item, activeChar, activeChar);
			
			if (count > 1)
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(_item.getItemId());
				smsg.addNumber(count);
				activeChar.sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				smsg.addItemName(_item.getItemId());
				activeChar.sendPacket(smsg);
			}
			ItemList il = new ItemList(activeChar, false);
			activeChar.sendPacket(il);
		}
	}
}
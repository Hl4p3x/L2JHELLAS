package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Rnd;

public class Harvest implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.HARVEST
	};
	
	private L2PcInstance _activeChar;
	private L2MonsterInstance _target;
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		_activeChar = (L2PcInstance) activeChar;
		
		L2Object[] targetList = skill.getTargetList(activeChar);
		
		InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		
		if (targetList == null)
		{
			return;
		}
		
		for (int index = 0; index < targetList.length; index++)
		{
			if (!(targetList[index] instanceof L2MonsterInstance))
				continue;
			
			_target = (L2MonsterInstance) targetList[index];
			
			if (_activeChar.getObjectId() != _target.getSeederId())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
				_activeChar.sendPacket(sm);
				continue;
			}
			
			boolean send = false;
			int total = 0;
			int cropId = 0;
			
			if (_target.isSeeded())
			{
				if (calcSuccess())
				{
					L2Attackable.RewardItem[] items = _target.takeHarvest();
					if (items != null && items.length > 0)
					{
						for (L2Attackable.RewardItem ritem : items)
						{
							cropId = ritem.getItemId(); // always got 1 type of
							// crop as reward
							if (_activeChar.isInParty())
								_activeChar.getParty().distributeItem(_activeChar, ritem, true, _target);
							else
							{
								L2ItemInstance item = _activeChar.getInventory().addItem("Manor", ritem.getItemId(), ritem.getCount(), _activeChar, _target);
								if (iu != null)
									iu.addItem(item);
								send = true;
								total += ritem.getCount();
							}
						}
						if (send)
						{
							SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
							smsg.addNumber(total);
							smsg.addItemName(cropId);
							_activeChar.sendPacket(smsg);
							if (_activeChar.getParty() != null)
							{
								smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_HARVESTED_S3_S2S);
								smsg.addString(_activeChar.getName());
								smsg.addNumber(total);
								smsg.addItemName(cropId);
								_activeChar.getParty().broadcastToPartyMembers(_activeChar, smsg);
							}
							
							if (iu != null)
								_activeChar.sendPacket(iu);
							else
								_activeChar.sendPacket(new ItemList(_activeChar, false));
						}
					}
				}
				else
				{
					_activeChar.sendPacket(SystemMessageId.THE_HARVEST_HAS_FAILED);
				}
			}
			else
			{
				_activeChar.sendPacket(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
			}
		}
	}
	
	private boolean calcSuccess()
	{
		int basicSuccess = 100;
		int levelPlayer = _activeChar.getLevel();
		int levelTarget = _target.getLevel();
		
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
			diff = -diff;
		
		// apply penalty, target <=> player levels
		// 5% penalty for each level
		if (diff > 5)
		{
			basicSuccess -= (diff - 5) * 5;
		}
		
		// success rate can't be less than 1%
		if (basicSuccess < 1)
			basicSuccess = 1;
		
		int rate = Rnd.nextInt(99);
		
		if (rate < basicSuccess)
			return true;
		return false;
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.skills.effects.EffectCharge;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillCharge;

public class EnergyStone implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5589
	};
	private EffectCharge _effect;
	private L2SkillCharge _skill;
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		
		L2PcInstance activeChar;
		if (playable.isPlayer())
			activeChar = (L2PcInstance) playable;
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance) playable).getOwner();
		else
			return;
		
		if (item.getItemId() != 5589)
			return;
		
		int classid = activeChar.getClassId().getId();
		
		if (classid == 2 || classid == 48 || classid == 88 || classid == 114)
		{
			
			if (activeChar.isAllSkillsDisabled())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (activeChar.isSitting())
			{
				activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
				return;
			}
			
			_skill = getChargeSkill(activeChar);
			if (_skill == null)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(5589));
				return;
			}
			
			_effect = activeChar.getChargeEffect();
			
			if (_effect == null)
			{
				L2Skill dummy = SkillTable.getInstance().getInfo(_skill.getId(), _skill.getLevel());
				if (dummy != null)
				{
					dummy.getEffects(activeChar, activeChar);
					activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
					return;
				}
				return;
			}
			
			if (_effect.getLevel() < 2)
			{
				activeChar.broadcastPacket(new MagicSkillUse(playable, activeChar, _skill.getId(), 1, 1, 0));
				_effect.addNumCharges(1);
				activeChar.sendPacket(new EtcStatusUpdate(activeChar));
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
			}
			else if (_effect.getLevel() == 2)
				activeChar.sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);

			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(_effect.getLevel()));
			return;
		}

		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(5589));
		return;
	}
	
	private static L2SkillCharge getChargeSkill(L2PcInstance activeChar)
	{
		L2Skill[] skills = activeChar.getAllSkills();
		for (L2Skill s : skills)
		{
			if (s.getId() == 50 || s.getId() == 8)
				return (L2SkillCharge) s;
		}
		return null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;

public class ScrollOfResurrection implements IItemHandler
{
	// all the items ids that this handler knows
	private static final int[] ITEM_IDS =
	{
		737,
		3936,
		3959,
		6387
	};
	private L2PetInstance targetPet;
	private L2PcInstance targetPlayer;
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.inObserverMode())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isMovementDisabled())
			return;
		
		if (activeChar.isCursedWeaponEquiped())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		int itemId = item.getItemId();
		boolean humanScroll = (itemId == 3936 || itemId == 3959 || itemId == 737);
		boolean petScroll = (itemId == 6387 || itemId == 737);
		
		// SoR Animation section
		L2Character target = (L2Character) activeChar.getTarget();
		
		if (target != null && target.isDead())
		{
			targetPlayer = null;
			
			if (target instanceof L2PcInstance)
				targetPlayer = (L2PcInstance) target;
			
			targetPet = null;
			
			if (target instanceof L2PetInstance)
				targetPet = (L2PetInstance) target;
			
			if (targetPlayer != null || targetPet != null)
			{
				boolean condGood = true;
				
				// check target is not in a active siege zone
				Castle castle = null;
				
				if (targetPlayer != null)
					castle = CastleManager.getInstance().getCastle(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
				else
					castle = CastleManager.getInstance().getCastle(targetPet.getX(), targetPet.getY(), targetPet.getZ());
				
				if (castle != null && castle.getSiege().getIsInProgress())
				{
					condGood = false;
					activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
				}
				
				if (targetPet != null)
				{
					if (targetPet.getOwner() != activeChar)
					{
						if (targetPet.getOwner().isReviveRequested())
						{
							if (targetPet.getOwner().isRevivingPet())
								activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
							else
								activeChar.sendPacket(SystemMessageId.CANNOT_RES_PET2); // A pet cannot be resurrected while it's owner is in the process of
							// resurrecting.
							condGood = false;
						}
					}
					else if (!petScroll)
					{
						condGood = false;
						activeChar.sendMessage("You dont have the correct scroll.");
					}
				}
				else
				{
					if (targetPlayer.isFestivalParticipant()) // Check to see if the current player target is in a festival.
					{
						condGood = false;
						activeChar.sendPacket(SystemMessage.sendString("You may not resurrect participants in a festival."));
					}
					if (targetPlayer.isReviveRequested())
					{
						if (targetPlayer.isRevivingPet())
							activeChar.sendPacket(SystemMessageId.MASTER_CANNOT_RES); // While a pet is attempting to resurrect, it cannot help in resurrecting
						// its master.
						else
							activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
						condGood = false;
					}
					else if (!humanScroll)
					{
						condGood = false;
						activeChar.sendMessage("You dont have the correct scroll.");
					}
				}
				
				if (condGood)
				{
					if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
						return;
					
					int skillId = 0;
					int skillLevel = 1;
					
					switch (itemId)
					{
						case 737:
							skillId = 2014;
							break; // Scroll of Resurrection
						case 3936:
							skillId = 2049;
							break; // Blessed Scroll of Resurrection
						case 3959:
							skillId = 2062;
							break; // L2Day - Blessed Scroll of Resurrection
						case 6387:
							skillId = 2179;
							break; // Blessed Scroll of Resurrection: For Pets
					}
					
					if (skillId != 0)
					{
						L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
						activeChar.useMagic(skill, true, true);
						
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(itemId);
						activeChar.sendPacket(sm);
					}
				}
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
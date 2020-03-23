package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlEvent;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.ai.NextAction;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.enums.items.L2ArmorType;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.handler.ItemHandler;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.PetItemList;
import com.l2jhellas.gameserver.network.serverpackets.ShowCalculator;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;

public final class UseItem extends L2GameClientPacket
{
	private static final String _C__14_USEITEM = "[C] 14 UseItem";
	
	protected int _objectId;
	private int _itemId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		// Flood protect UseItem
		if (!FloodProtectors.performAction(getClient(),Action.USE_ITEM))
			return;
		
		if (activeChar.getPrivateStoreType() != StoreType.NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.cancelActiveTrade();
			return;
		}
		
		if (activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessageId.ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if (item == null)
			return;
		
		_itemId = item.getItemId();
		
		// The player can't use an item in those special conditions
		if (activeChar.isAlikeDead() || activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAlikeDead() || activeChar.isAfraid())
		{
			activeChar.sendMessage(item.getItemName() + " cannot be used right now!");
			return;
		}
		
		if (item.getItem().getType2() == L2Item.TYPE2_QUEST)
		{
			activeChar.sendMessage("You can't use quest items.");
			return;
		}
		
		if (item.getItemId() == 57)
			return;
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0)
		{
			switch (_itemId)
			{
				case 736:
				case 1538:
				case 1829:
				case 1830:
				case 3958:
				case 5858:
				case 5859:
				case 6663:
				case 6664:
				case 7618:
				case 7619:
					return;
			}
			
			if ((_itemId >= 7117 && _itemId <= 7135) || (_itemId >= 7554 && _itemId <= 7559))
				return;
		}
		
		final int itemId = item.getItemId();
		
		if (itemId == 0)
			return;
		
		final L2Clan cl = activeChar.getClan();
		
		if (((cl == null) || cl.hasCastle() == 0) && itemId == 7015 && Config.CASTLE_SHIELD)
		{
			// A shield that can only be used by the members of a clan that owns a castle.
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if ((itemId >= 7860 && itemId <= 7879) && Config.APELLA_ARMORS && (cl == null || activeChar.getPledgeClass() < 5))
		{
			// Apella armor used by clan members may be worn by a Baron or a higher level Aristocrat.
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if ((itemId >= 7850 && itemId <= 7859) && Config.OATH_ARMORS && (cl == null))
		{
			// Clan Oath armor used by all clan members
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (itemId == 6841 && Config.CASTLE_CROWN && (cl == null || (cl.hasCastle() == 0 || !activeChar.isClanLeader())))
		{
			// The Lord's Crown used by castle lords only
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		// Castle circlets used by the members of a clan that owns a castle, academy members are excluded.
		if (Config.CASTLE_CIRCLETS && ((itemId >= 6834 && itemId <= 6840) || itemId == 8182 || itemId == 8183))
		{
			if (cl == null)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
				activeChar.sendPacket(sm);
				sm = null;
				return;
			}
			int circletId = CastleManager.getInstance().getCircletByCastleId(cl.hasCastle());
			if (activeChar.getPledgeType() == -1 || circletId != itemId)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
				activeChar.sendPacket(sm);
				sm = null;
				return;
			}
		}
		final L2Weapon curwep = activeChar.getActiveWeaponItem();
		if (curwep != null)
		{
			if ((curwep.getItemType() == L2WeaponType.DUAL) && (item.getItemType() == L2WeaponType.NONE))
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
			else if ((curwep.getItemType() == L2WeaponType.BOW) && (item.getItemType() == L2WeaponType.NONE))
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
			else if ((curwep.getItemType() == L2WeaponType.BIGBLUNT) && (item.getItemType() == L2WeaponType.NONE))
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
			else if ((curwep.getItemType() == L2WeaponType.BIGSWORD) && (item.getItemType() == L2WeaponType.NONE))
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
			else if ((curwep.getItemType() == L2WeaponType.POLE) && (item.getItemType() == L2WeaponType.NONE))
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
			else if ((curwep.getItemType() == L2WeaponType.DUALFIST) && (item.getItemType() == L2WeaponType.NONE))
			{
				activeChar.sendMessage("You are not allowed to do this.");
				return;
			}
		}
		
		if (activeChar.isFishing() && (_itemId < 6535 || _itemId > 6540))
		{
			// You cannot do anything else while fishing
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		// Char cannot use pet items
		if (item.getItem().isForWolf() || item.getItem().isForHatchling() || item.getItem().isForStrider() || item.getItem().isForBabyPet())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_PET_ITEM); // You cannot equip a pet item.
			sm.addItemName(item.getItemId());
			getClient().getActiveChar().sendPacket(sm);
			sm = null;
			return;
		}
		if (activeChar.isFishing() && (item.getItemId() < 6535 || item.getItemId() > 6540))
		{
			// You cannot do anything else while fishing
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			getClient().getActiveChar().sendPacket(sm);
			sm = null;
			return;
		}
		
		activeChar.onActionRequest();
		
		if (item.isPetItem())
		{
			L2Summon summon = activeChar.getPet();
			
			// If no summon, cancels the use
			if (summon == null || !(summon instanceof L2PetInstance))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_PET_ITEM);
				return;
			}
			
			final L2PetInstance pet = ((L2PetInstance) summon);
			
			if (!(pet.canWear(item.getItem())))
			{
				activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				return;
			}
			
			// Transfer the item from owner to pet inventory.
			if (pet.isDead())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
				return;
			}
			
			if (!pet.getInventory().validateCapacity(item))
			{
				activeChar.sendMessage("Pet can't carry any more items");
				return;
			}
			
			if (!pet.getInventory().validateWeight(item, 1))
			{
				activeChar.sendMessage("Unable to place item your pet is too encumbered!");
				return;
			}
			
			activeChar.transferItem("Transfer", _objectId, 1, pet.getInventory(), pet);
			
			// Equip it, removing first the previous item.
			if (item.isEquipped())
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
			else
				pet.getInventory().equipItem(item);
			
			activeChar.sendPacket(new PetItemList(pet));
			pet.updateAndBroadcastStatus(1);
			return;
		}
		
		if (Config.DEBUG)
			_log.finest(activeChar.getName() + ": use item " + _objectId);
		
		if (!item.isEquipped())
		{
			if ((item.isHeroItem()) && (activeChar.isInOlympiadMode()))
				return;
		}
		
		if (item.isEquipable())
		{
			if (activeChar._haveFlagCTF)
			{
				activeChar.sendMessage("You can't equip an item while holding the flag");
				return;
			}
			if (!activeChar.isGM() && (item.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_WEAPON) || (item.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_ARMOR) || (item.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_JEWELRY))
			{
				activeChar.sendMessage("You have been kicked for using an item overenchanted!");
				activeChar.closeNetConnection(false);
				return;
			}
			switch (item.getItem().getBodyPart())
			{
				case L2Item.SLOT_LR_HAND:
				case L2Item.SLOT_L_HAND:
				case L2Item.SLOT_R_HAND:
				{
					if ((item.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_WEAPON) && !activeChar.isGM())
					{
						activeChar.setAccountAccesslevel(-100); // ban
						activeChar.sendMessage("You have been banned for using an item wich is over enchanted!"); // message
						activeChar.closeNetConnection(false); // kick
						return;
					}
					
					if (activeChar.isMounted())
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					// Don't allow weapon/shield equipment if a cursed weapon is equipped
					if (activeChar.isCursedWeaponEquiped())
						return;
					
					break;
				}
				case L2Item.SLOT_CHEST:
				case L2Item.SLOT_BACK:
				case L2Item.SLOT_GLOVES:
				case L2Item.SLOT_FEET:
				case L2Item.SLOT_HEAD:
				case L2Item.SLOT_FULL_ARMOR:
				case L2Item.SLOT_LEGS:
				{
					if ((item.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_ARMOR) && !activeChar.isGM())
					{
						activeChar.setAccountAccesslevel(-100); // ban
						activeChar.sendMessage("You have been banned for using an item wich is over enchanted!"); // message
						activeChar.closeNetConnection(false); // kick
						return;
					}
					break;
				}
				case L2Item.SLOT_R_EAR:
				case L2Item.SLOT_L_EAR:
				case L2Item.SLOT_NECK:
				case L2Item.SLOT_R_FINGER:
				case L2Item.SLOT_L_FINGER:
				{
					if ((item.getEnchantLevel() > Config.ENCHANT_MAX_ALLOWED_JEWELRY) && !activeChar.isGM())
					{
						activeChar.setAccountAccesslevel(-100); // ban
						activeChar.sendMessage("You have been banned for using an item wich is over enchanted!"); // message
						activeChar.closeNetConnection(false); // kick
						return;
					}
					break;
				}
				
			}
			if (!Config.ALLOW_DAGGERS_WEAR_HEAVY)
				if ((activeChar.getClassId().getId() == 93) || (activeChar.getClassId().getId() == 108) || (activeChar.getClassId().getId() == 101) || (activeChar.getClassId().getId() == 8) || (activeChar.getClassId().getId() == 23) || (activeChar.getClassId().getId() == 36))
				{
					if (item.getItemType() == L2ArmorType.HEAVY)
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
				}
			
			if (!Config.ALLOW_ARCHERS_WEAR_HEAVY)
				if ((activeChar.getClassId().getId() == 9) || (activeChar.getClassId().getId() == 92) || (activeChar.getClassId().getId() == 24) || (activeChar.getClassId().getId() == 102) || (activeChar.getClassId().getId() == 37) || (activeChar.getClassId().getId() == 109))
				{
					if (item.getItemType() == L2ArmorType.HEAVY)
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
				}
			final int bodyPart = item.getItem().getBodyPart();
			// Don't allow weapon/shield hero equipment during Olympiads
			if (activeChar.isInOlympiadMode() && (bodyPart == L2Item.SLOT_LR_HAND || bodyPart == L2Item.SLOT_L_HAND || bodyPart == L2Item.SLOT_R_HAND) && ((item.getItemId() >= 6611 && item.getItemId() <= 6621) || (item.getItemId() == 6842)))
				return;
			
			if (activeChar.isCursedWeaponEquiped() && _itemId == 6408) // Don't allow to put formal wear
				return;

			if (activeChar.isCastingNow())
				activeChar.getAI().setNextAction(new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.AI_INTENTION_CAST, () -> activeChar.useEquippableItem(_objectId, true)));
			else if (activeChar.isAttackingNow())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(() ->
				{			
					activeChar.useEquippableItem(_objectId, false);				
				}, (activeChar.getAttackEndTime() - GameTimeController.getInstance().getGameTicks()) * GameTimeController.MILLIS_IN_TICK);
		    }
			else
				activeChar.useEquippableItem(_objectId, true);
		}
		else
		{
			if (activeChar.isCastingNow() && !(item.isPotion()))
				return;
			
			final L2Weapon weaponItem = activeChar.getActiveWeaponItem();
			int itemid = item.getItemId();
			
			if (itemid == 4393)
				activeChar.sendPacket(new ShowCalculator(4393));
			
			if ((weaponItem != null && weaponItem.getItemType() == L2WeaponType.ROD) && ((itemid >= 6519 && itemid <= 6527) || (itemid >= 7610 && itemid <= 7613) || (itemid >= 7807 && itemid <= 7809) || (itemid >= 8484 && itemid <= 8486) || (itemid >= 8505 && itemid <= 8513)))
			{
				activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				activeChar.broadcastUserInfo();
				
				sendPacket(new ItemList(activeChar, false));
				return;
			}

			final IItemHandler handler = ItemHandler.getInstance().getHandler(item.getItem().getItemId());
			if (handler != null)
				handler.useItem(activeChar, item);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__14_USEITEM;
	}
}
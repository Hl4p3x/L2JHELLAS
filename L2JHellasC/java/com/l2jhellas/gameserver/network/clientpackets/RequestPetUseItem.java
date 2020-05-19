package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.PetData;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.handler.ItemHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.PetItemList;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetUseItem extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestPetUseItem.class.getName());
	private static final String _C__8A_REQUESTPETUSEITEM = "[C] 8a RequestPetUseItem";
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		L2PetInstance pet = (L2PetInstance) activeChar.getPet();
		
		if (pet == null)
			return;
		
		L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
		
		if (item == null)
			return;
		
		if (item.isWear())
			return;
		
		int itemId = item.getItemId();
		
		if (activeChar.isAlikeDead() || pet.isDead())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item.getItemId());
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (Config.DEBUG)
			_log.finest(activeChar.getObjectId() + ": pet use item " + _objectId);
		
		// check if the item matches the pet
		if (item.isEquipable())
		{
			if (PetData.isWolf(pet.getNpcId()) && // wolf
			item.getItem().isForWolf())
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (PetData.isHatchling(pet.getNpcId()) && // hatchlings
			item.getItem().isForHatchling())
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (PetData.isStrider(pet.getNpcId()) && // striders
			item.getItem().isForStrider())
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (PetData.isBaby(pet.getNpcId()) && // baby pets (buffalo, cougar, kookaboora)
			item.getItem().isForBabyPet())
			{
				useItem(pet, item, activeChar);
				return;
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
				return;
			}
		}
		else if (PetData.isPetFood(itemId))
		{
			if (PetData.isWolf(pet.getNpcId()) && PetData.isWolfFood(itemId))
			{
				feed(activeChar, pet, item);
				return;
			}
			if (PetData.isSinEater(pet.getNpcId()) && PetData.isSinEaterFood(itemId))
			{
				feed(activeChar, pet, item);
				return;
			}
			else if (PetData.isHatchling(pet.getNpcId()) && PetData.isHatchlingFood(itemId))
			{
				feed(activeChar, pet, item);
				return;
			}
			else if (PetData.isStrider(pet.getNpcId()) && PetData.isStriderFood(itemId))
			{
				feed(activeChar, pet, item);
				return;
			}
			else if (PetData.isWyvern(pet.getNpcId()) && PetData.isWyvernFood(itemId))
			{
				feed(activeChar, pet, item);
				return;
			}
			else if (PetData.isBaby(pet.getNpcId()) && PetData.isBabyFood(itemId))
			{
				feed(activeChar, pet, item);
			}
		}
		
		IItemHandler handler = ItemHandler.getInstance().getHandler(item.getItemId());
		
		if (handler != null)
		{
			useItem(pet, item, activeChar);
		}
		else
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS);
			activeChar.sendPacket(sm);
		}
		
		return;
	}
	
	private synchronized static void useItem(L2PetInstance pet, L2ItemInstance item, L2PcInstance activeChar)
	{
		if (item.isEquipable())
		{
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
				switch (item.getItem().getBodyPart())
				{
					case L2Item.SLOT_R_HAND:
						pet.setWeapon(0);
						break;
					case L2Item.SLOT_CHEST:
						pet.setArmor(0);
						break;
					case L2Item.SLOT_NECK:
						pet.setJewel(0);
						break;
				}
			}
			else
			{
				pet.getInventory().equipItem(item);
				switch (item.getItem().getBodyPart())
				{
					case L2Item.SLOT_R_HAND:
						pet.setWeapon(item.getItemId());
						break;
					case L2Item.SLOT_CHEST:
						pet.setArmor(item.getItemId());
						break;
					case L2Item.SLOT_NECK:
						pet.setJewel(item.getItemId());
						break;
				}
				
			}
			
			PetItemList pil = new PetItemList(pet);
			activeChar.sendPacket(pil);
			
			pet.updateAndBroadcastStatus(1);
		}
		else
		{
			IItemHandler handler = ItemHandler.getInstance().getHandler(item.getItemId());
			if (handler == null)
				_log.warning(RequestPetUseItem.class.getName() + ": no itemhandler registered for itemId:" + item.getItemId());
			else
			{
				handler.useItem(pet, item);
				pet.updateAndBroadcastStatus(1);
			}
		}
	}
	
	private static void feed(L2PcInstance player, L2PetInstance pet, L2ItemInstance item)
	{
		// if pet has food in inventory
		if (pet.destroyItem("Feed", item.getObjectId(), 1, pet, false))
			pet.setCurrentFed(pet.getCurrentFed() + 100);
		pet.broadcastStatusUpdate();
	}
	
	@Override
	public String getType()
	{
		return _C__8A_REQUESTPETUSEITEM;
	}
}
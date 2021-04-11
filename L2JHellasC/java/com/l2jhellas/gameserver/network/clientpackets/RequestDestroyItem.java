package com.l2jhellas.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.PetData;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class RequestDestroyItem extends L2GameClientPacket
{
	private static final String _C__59_REQUESTDESTROYITEM = "[C] 59 RequestDestroyItem";
	
	private int _objectId;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (!FloodProtectors.performAction(getClient(), FloodAction.USE_ITEM))
		{
			activeChar.sendMessage("You are using this action too fast!");
			return;
		}
		
		if (_count <= 0)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DESTROY_NUMBER_INCORRECT);
			return;
		}
		
		int count = _count;

		if (activeChar.getActiveEnchantItem() != null || activeChar.getActiveWarehouse() != null || activeChar.getActiveTradeList() != null)
		{
			activeChar.sendMessage("You can't destroy items when you are enchanting, got active warehouse or active trade.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.getPrivateStoreType() != StoreType.NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		
		final L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		// if we can't find requested item, its actually a cheat!
		if (itemToRemove == null)
			return;
		
		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingNow())
		{
			if ((activeChar.getCurrentSkill() != null) && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getItemId())
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		}
		
		final int itemId = itemToRemove.getItemId();
		if (itemToRemove.isWear() || !itemToRemove.isDestroyable() || CursedWeaponsManager.getInstance().isCursed(itemId))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		
		if (!itemToRemove.isStackable() && count > 1)
			return;
		
		if (_count > itemToRemove.getCount())
			count = itemToRemove.getCount();
		
		if (itemToRemove.isEquipped())
		{
			final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getEquipSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped)
			{
				activeChar.checkSSMatch(null, element);
				
				iu.addModifiedItem(element);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
		}
		
		if (PetData.isPetItem(itemId))
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				if (activeChar.getPet() != null && activeChar.getPet().getControlItemId() == _objectId)
				{
					activeChar.getPet().unSummon(activeChar);
				}
				
				// if it's a pet control item, delete the pet
				PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
				statement.setInt(1, _objectId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warning(RequestDestroyItem.class.getSimpleName() + ": could not delete pet objectid: ");
			}
		}
		
		final L2ItemInstance removedItem = activeChar.getInventory().destroyItem("Destroy", _objectId, count, activeChar, null);
		
		if (removedItem == null)
			return;
		
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			if (removedItem.getCount() == 0)
				iu.addRemovedItem(removedItem);
			else
				iu.addModifiedItem(removedItem);
			
			// client.getConnection().sendPacket(iu);
			activeChar.sendPacket(iu);
		}
		else
			sendPacket(new ItemList(activeChar, true));
		
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
		
		// L2World world = L2World.getInstance();
		// world.removeObject(removedItem);
	}
	
	@Override
	public String getType()
	{
		return _C__59_REQUESTDESTROYITEM;
	}
}
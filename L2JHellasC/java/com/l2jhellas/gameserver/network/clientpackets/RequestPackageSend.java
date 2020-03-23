package com.l2jhellas.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.PcFreight;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.ItemContainer;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;

public final class RequestPackageSend extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestPackageSend.class.getName());
	private static final String _C_9F_REQUESTPACKAGESEND = "[C] 9F RequestPackageSend";
	private final List<Item> _items = new ArrayList<>();
	private int _objectID;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_objectID = readD();
		_count = readD();
		if (_count < 0 || _count > 500)
		{
			_count = -1;
			return;
		}
		for (int i = 0; i < _count; i++)
		{
			int id = readD(); // this is some id sent in PackageSendableList
			int count = readD();
			_items.add(new Item(id, count));
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_count == -1 || _items == null)
			return;
		
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.getObjectId() == _objectID)
			return;
		
		if (!FloodProtectors.performAction(getClient(), Action.MANUFACTURE))
		{
			player.sendMessage("You using freight too fast.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getActiveTradeList() != null || player.getActiveEnchantItem() != null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getAccountChars().size() < 1)
		{
			return;
		}
		else if (!player.getAccountChars().containsKey(_objectID))
		{
			return;
		}
		
		if (L2World.getInstance().getPlayer(player.getName().toLowerCase()) != null)
			return;
		
		if (!FloodProtectors.performAction(getClient(), Action.MANUFACTURE))
		{
			player.sendMessage("You using freight too fast.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2PcInstance target = L2PcInstance.load(_objectID);
		final PcFreight freight = target.getFreight();
		getClient().getActiveChar().setActiveWarehouse(freight);
		target.deleteMe();
		final ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
			return;
		final L2NpcInstance manager = player.getLastFolkNPC();
		if ((manager == null || !player.isInsideRadius(manager, L2Npc.INTERACTION_DISTANCE, false, false)) && !player.isGM())
			return;
		
		if (warehouse instanceof PcFreight && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
			return;
		
		// Freight price from config or normal price per item slot (30)
		int fee = _count * Config.ALT_GAME_FREIGHT_PRICE;
		int currentAdena = player.getAdena();
		int slots = 0;
		
		for (Item i : _items)
		{
			int objectId = i.id;
			int count = i.count;
			
			// Check validity of requested item
			final L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");
			if (item == null)
			{
				_log.warning(RequestPackageSend.class.getName() + ": Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				i.id = 0;
				i.count = 0;
				continue;
			}
			if (item.isAugmented())
			{
				_log.warning(RequestPackageSend.class.getName() + ": Error depositing a warehouse object for char " + player.getName() + " (item is augmented)");
				return;
			}
			if (!item.isTradeable() || item.getItemType() == L2EtcItemType.QUEST)
				return;
			
			// Calculate needed adena and slots
			if (item.getItemId() == 57)
				currentAdena -= count;
			if (!item.isStackable())
				slots += count;
			else if (warehouse.getItemByItemId(item.getItemId()) == null)
				slots++;
		}
		
		// Item Max Limit Check
		if (!warehouse.validateCapacity(slots))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		
		// Check if enough adena and charge the fee
		if (currentAdena < fee || !player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		
		// Proceed to the transfer
		final InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (Item i : _items)
		{
			int objectId = i.id;
			int count = i.count;
			
			// check for an invalid item
			if (objectId == 0 && count == 0)
				continue;
			
			final L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);
			if (oldItem == null)
			{
				_log.warning(RequestPackageSend.class.getName() + ": Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				continue;
			}
			
			final int itemId = oldItem.getItemId();
			
			if ((itemId >= 6611 && itemId <= 6621) || itemId == 6842)
				continue;
			
			final L2ItemInstance newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());
			if (newItem == null)
			{
				_log.warning(RequestPackageSend.class.getName() + ": Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
				continue;
			}
			
			if (playerIU != null)
			{
				if (oldItem.getCount() > 0 && oldItem != newItem)
					playerIU.addModifiedItem(oldItem);
				else
					playerIU.addRemovedItem(oldItem);
			}
		}
		
		// Send updated item list to the player
		if (playerIU != null)
			player.sendPacket(playerIU);
		else
			player.sendPacket(new ItemList(player, false));
		
		// Update current load status on player
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
	
	private class Item
	{
		public int id;
		public int count;
		
		public Item(int i, int c)
		{
			id = i;
			count = c;
		}
	}
	
	@Override
	public String getType()
	{
		return _C_9F_REQUESTPACKAGESEND;
	}
}
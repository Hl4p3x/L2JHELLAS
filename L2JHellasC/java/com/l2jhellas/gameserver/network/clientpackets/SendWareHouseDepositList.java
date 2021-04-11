package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.ClanWarehouse;
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
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;
import com.l2jhellas.util.IllegalPlayerAction;
import com.l2jhellas.util.Util;

public final class SendWareHouseDepositList extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(SendWareHouseDepositList.class.getName());
	private static final String _C__31_SENDWAREHOUSEDEPOSITLIST = "[C] 31 SendWareHouseDepositList";
	
	private int _count;
	private int[] _items;
	
	@Override
	protected void readImpl()
	{
		_count = readD();
		
		// check packet list size
		if ((_count < 0) || (_count * 8 > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
		{
			_count = 0;
		}
		
		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			int objectId = readD();
			_items[i * 2 + 0] = objectId;
			long cnt = readD();
			if (cnt > Integer.MAX_VALUE || cnt < 0)
			{
				_count = 0;
				_items = null;
				return;
			}
			_items[i * 2 + 1] = (int) cnt;
		}
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (!FloodProtectors.performAction(getClient(), FloodAction.MANUFACTURE))
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}
		ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
			return;
		L2NpcInstance manager = player.getLastFolkNPC();
		if (((manager == null) || !player.isInsideRadius(manager, L2Npc.INTERACTION_DISTANCE, false, false)) && !player.isGM())
			return;
		
		if ((warehouse instanceof ClanWarehouse) && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			return;
		}
		if (player.getActiveTradeList() != null)
		{
			player.sendMessage("You can't deposit items when you are trading.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.isDead() || player.isFakeDeath())
		{
			player.sendMessage("You can't deposit items while you are dead or fakedeath.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.isCastingNow())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.getPrivateStoreType() != StoreType.NONE)
		{
			player.sendMessage("You can't deposit items when you are trading.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getKarma() > 0))
			return;
		
		if (player.getActiveEnchantItem() != null)
		{
			player.setAccessLevel(-100);
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " Tried To Use Enchant Exploit , And Got Banned!", IllegalPlayerAction.PUNISH_KICKBAN);
			return;
		}
		
		// Freight price from config or normal price per item slot (30)
		int fee = _count * 30;
		int currentAdena = player.getAdena();
		int slots = 0;
		
		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];
			
			// Check validity of requested item
			L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");
			if (item == null)
			{
				_log.warning(SendWareHouseDepositList.class.getName() + ": Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				_items[i * 2 + 0] = 0;
				_items[i * 2 + 1] = 0;
				continue;
			}
			
			if ((warehouse instanceof ClanWarehouse) && !item.isTradeable() || item.getItemType() == L2EtcItemType.QUEST)
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
		if ((currentAdena < fee) || !player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		
		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];
			
			// check for an invalid item
			if (objectId == 0 && count == 0)
				continue;
			
			L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);
			if (oldItem == null)
			{
				_log.warning(SendWareHouseDepositList.class.getName() + ": Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				continue;
			}
			
			int itemId = oldItem.getItemId();
			
			if ((itemId >= 6611 && itemId <= 6621) || itemId == 6842)
				continue;
			
			L2ItemInstance newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());
			if (newItem == null)
			{
				_log.warning(SendWareHouseDepositList.class.getName() + ": Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
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
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
	
	@Override
	public String getType()
	{
		return _C__31_SENDWAREHOUSEDEPOSITLIST;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.CastleManorManager;
import com.l2jhellas.gameserver.instancemanager.CastleManorManager.SeedProduction;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.instance.L2ManorManagerInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;
import com.l2jhellas.util.Util;

public class RequestBuySeed extends L2GameClientPacket
{
	private static final String _C__C4_REQUESTBUYSEED = "[C] C4 RequestBuySeed";
	
	private int _count;
	private int _manorId;
	private int[] _items; // size _count * 2
	
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		
		if ((_count > 500) || ((_count * 8) < _buf.remaining())) // check values
		{
			_count = 0;
			return;
		}
		
		_items = new int[_count * 2];
		
		for (int i = 0; i < _count; i++)
		{
			int itemId = readD();
			_items[i * 2 + 0] = itemId;
			long cnt = readD();
			if (cnt > Integer.MAX_VALUE || cnt < 1)
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
		if (!FloodProtectors.performAction(getClient(), Action.MANOR))
			return;
		
		long totalPrice = 0;
		int slots = 0;
		int totalWeight = 0;
		
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		if (_count < 1)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Object target = player.getTarget();
		
		if (!(target instanceof L2ManorManagerInstance))
			target = player.getLastFolkNPC();
		
		if (!(target instanceof L2ManorManagerInstance))
			return;
		
		Castle castle = CastleManager.getInstance().getCastleById(_manorId);
		
		for (int i = 0; i < _count; i++)
		{
			int seedId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];
			int price = 0;
			int residual = 0;
			
			SeedProduction seed = castle.getSeed(seedId, CastleManorManager.PERIOD_CURRENT);
			price = seed.getPrice();
			residual = seed.getCanProduce();
			
			if (price <= 0)
				return;
			
			if (residual < count)
				return;
			
			totalPrice += count * price;
			
			L2Item template = ItemTable.getInstance().getTemplate(seedId);
			totalWeight += count * template.getWeight();
			if (!template.isStackable())
				slots += count;
			else if (player.getInventory().getItemByItemId(seedId) == null)
				slots++;
		}
		
		if (totalPrice > Integer.MAX_VALUE)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!player.getInventory().validateWeight(totalWeight))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}
		
		// Charge buyer
		if ((totalPrice < 0) || !player.reduceAdena("Buy", (int) totalPrice, target, false))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		
		// Adding to treasury for Manor Castle
		castle.addToTreasuryNoTax((int) totalPrice);
		
		// Proceed the purchase
		InventoryUpdate playerIU = new InventoryUpdate();
		for (int i = 0; i < _count; i++)
		{
			int seedId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];
			if (count < 0)
				count = 0;
			
			// Update Castle Seeds Amount
			SeedProduction seed = castle.getSeed(seedId, CastleManorManager.PERIOD_CURRENT);
			seed.setCanProduce(seed.getCanProduce() - count);
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				CastleManager.getInstance().getCastleById(_manorId).updateSeed(seed.getId(), seed.getCanProduce(), CastleManorManager.PERIOD_CURRENT);
			
			// Add item to Inventory and adjust update packet
			L2ItemInstance item = player.getInventory().addItem("Buy", seedId, count, player, target);
			
			if (item.getCount() > count)
				playerIU.addModifiedItem(item);
			else
				playerIU.addNewItem(item);
			
			// Send Char Buy Messages
			SystemMessage sm = null;
			sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(seedId);
			sm.addNumber(count);
			player.sendPacket(sm);
		}
		// Send update packets
		player.sendPacket(playerIU);
		
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
	
	@Override
	public String getType()
	{
		return _C__C4_REQUESTBUYSEED;
	}
}
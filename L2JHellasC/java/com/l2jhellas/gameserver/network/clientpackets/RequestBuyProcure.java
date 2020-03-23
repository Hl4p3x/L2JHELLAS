package com.l2jhellas.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.instancemanager.CastleManorManager;
import com.l2jhellas.gameserver.instancemanager.CastleManorManager.CropProcure;
import com.l2jhellas.gameserver.model.L2Manor;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.instance.L2ManorManagerInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Util;

public class RequestBuyProcure extends L2GameClientPacket
{
	private static final String _C__C3_REQUESTBUYPROCURE = "[C] C3 RequestBuyProcure";
	@SuppressWarnings("unused")
	private int _listId;
	private int _count;
	private int[] _items;
	@SuppressWarnings("unused")
	private List<CropProcure> _procureList = new ArrayList<CropProcure>();
	private L2ManorManagerInstance manor;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if (_count > 500) // protect server
		{
			_count = 0;
			return;
		}
		
		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			readD();
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
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
			return;
		
		L2Object target = player.getTarget();
		
		if (_count < 1)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check for buylist validity and calculates summary values
		int slots = 0;
		int weight = 0;
		manor = (target != null && target instanceof L2ManorManagerInstance) ? (L2ManorManagerInstance) target : null;
		
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];
			if (count > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.", Config.DEFAULT_PUNISH);
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				return;
			}
			
			L2Item template = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward()));
			
			if(template==null)
				return;
			
			weight += count * template.getWeight();
			
			if (!template.isStackable())
				slots += count;
			else if (player.getInventory().getItemByItemId(itemId) == null)
				slots++;
		}
		
		if (!player.getInventory().validateWeight(weight))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}
		
		// Proceed the purchase
		InventoryUpdate playerIU = new InventoryUpdate();
		_procureList = manor.getCastle().getCropProcure(CastleManorManager.PERIOD_CURRENT);
		
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];
			if (count < 0)
				count = 0;
			
			int rewradItemId = L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward());
			
			int rewradItemCount = 1; // L2Manor.getInstance().getRewardAmount(itemId, manor.getCastle().getCropReward(itemId));
			
			rewradItemCount = count / rewradItemCount;
			
			// Add item to Inventory and adjust update packet
			L2ItemInstance item = player.getInventory().addItem("Manor", rewradItemId, rewradItemCount, player, manor);
			L2ItemInstance iteme = player.getInventory().destroyItemByItemId("Manor", itemId, count, player, manor);
			
			if (item == null || iteme == null)
				continue;
			
			playerIU.addRemovedItem(iteme);
			if (item.getCount() > rewradItemCount)
				playerIU.addModifiedItem(item);
			else
				playerIU.addNewItem(item);
			
			// Send Char Buy Messages
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(rewradItemId);
			sm.addNumber(rewradItemCount);
			player.sendPacket(sm);
			sm = null;
			
			// manor.getCastle().setCropAmount(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getAmount() - count);
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
		return _C__C3_REQUESTBUYPROCURE;
	}
}
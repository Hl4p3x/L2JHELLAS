package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MercManagerInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;
import com.l2jhellas.util.Util;

public final class RequestSellItem extends L2GameClientPacket
{
	private static final String _C__1E_REQUESTSELLITEM = "[C] 1E RequestSellItem";
	
	private int _listId;
	private int _count;
	private int[] _items; // count*3

	private L2Npc merchant;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		if (_count <= 0 || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
			_items = null;
			return;
		}
		_items = new int[_count * 3];
		for (int i = 0; i < _count; i++)
		{
			int objectId = readD();
			_items[i * 3 + 0] = objectId;
			int itemId = readD();
			_items[i * 3 + 1] = itemId;
			long cnt = readD();
			if (cnt > Integer.MAX_VALUE || cnt <= 0)
			{
				_count = 0;
				_items = null;
				return;
			}
			_items[i * 3 + 2] = (int) cnt;
		}
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		// Flood protect 
		if (!FloodProtectors.performAction(getClient(),FloodAction.MANUFACTURE))
			return;
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
			return;
		
		L2Object target = player.getTarget();
		if (!player.isGM() && ((target == null) // No target (ie GM Shop)
			|| !(target instanceof L2MerchantInstance || target instanceof L2MercManagerInstance) // Target not a merchant and not mercmanager
		|| !player.isInsideRadius(target, L2Npc.INTERACTION_DISTANCE, false, false) // Distance is too far
		))
			return;
		
		boolean ok = true;
		String htmlFolder = "";
		
		if (target != null)
		{
			if (target instanceof L2MerchantInstance)
				htmlFolder = "merchant";
			else if (target instanceof L2FishermanInstance)
				htmlFolder = "fisherman";
			else
				ok = false;
		}
		else
			ok = false;
		
		merchant = null;
		
		if (ok)
			merchant = (L2Npc) target;
		
		if (_listId > 1000000) // lease
		{
			if (merchant.getTemplate().npcId != _listId - 1000000)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		long totalPrice = 0;
		// Proceed the sell
		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 3 + 0];
			int count = _items[i * 3 + 2];
			
			if (count < 0 || count > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.", Config.DEFAULT_PUNISH);
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				sm = null;
				return;
			}
			
			L2ItemInstance item = player.checkItemManipulation(objectId, count, "sell");
			if (item == null || (!item.getItem().isSellable()) || item.getLocation() != ItemLocation.INVENTORY)
				continue;
			
			totalPrice += item.getReferencePrice() * count / 2;
			if (totalPrice > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}
			
			item = player.getInventory().destroyItem("Sell", objectId, count, player, null);
			
		}
		player.addAdena("Sell", (int) totalPrice, merchant, false);
		
		String html = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-sold.htm");
		
		if (html != null)
		{
			NpcHtmlMessage soldMsg = new NpcHtmlMessage(merchant.getObjectId());
			soldMsg.setHtml(html.replaceAll("%objectId%", String.valueOf(merchant.getObjectId())));
			player.sendPacket(soldMsg);
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ItemList(player, true));
	}
	
	@Override
	public String getType()
	{
		return _C__1E_REQUESTSELLITEM;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;
import com.l2jhellas.util.IllegalPlayerAction;
import com.l2jhellas.util.Util;

public final class RequestGiveItemToPet extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestGetItemFromPet.class.getName());
	private static final String REQUESTCIVEITEMTOPET__C__8B = "[C] 8B RequestGiveItemToPet";
	
	private int _objectId;
	private int _amount;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if ((player == null) || (player.getPet() == null) || !(player.getPet() instanceof L2PetInstance))
			return;
		
		if (!FloodProtectors.performAction(player.getClient(), Action.USE_ITEM))
			return;
		
		if (_amount <= 0)
			return;
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && player.getKarma() > 0)
			return;
		
		if (player.getActiveEnchantItem() != null)
		{
			player.setAccessLevel(-100);
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " Tried To Use Enchant Exploit And Got Banned!", IllegalPlayerAction.PUNISH_KICKBAN);
			return;
		}
		
		if (player.getActiveWarehouse() != null || player.getActiveTradeList() != null)
		{
			player.sendMessage("You can't give items when you got active warehouse or active trade.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.getPrivateStoreType() != StoreType.NONE)
		{
			player.sendMessage("Cannot exchange items while trading.");
			return;
		}
		
		// Exploit Fix for Hero weapons Uses pet Inventory to buy New One.
		final L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		
		if (item == null)
			return;
		
		if (item.isAugmented())
		{
			player.sendMessage("You cannot give augmented items to pet.");
			return;
		}
		
		if (!item.isDropable() || !item.isDestroyable() || !item.isTradeable())
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
			return;
		}
		if ((item.getItem().getItemType() == L2WeaponType.ROD) || ((item.getItemId() >= 6611) && (item.getItemId() <= 6621)) || ((item.getItemId() >= 7816) && (item.getItemId() <= 7831)) || item.isShadowItem() || item.isHeroItem() || item.getItem().getItemType() == L2EtcItemType.ARROW || item.getItem().getItemType() == L2EtcItemType.SHOT)
		{
			player.sendMessage("Hero weapons protection,arrows or shot, you can't use pet's inventory.");
			return;
		}

		final L2PetInstance pet = (L2PetInstance) player.getPet();
		if (pet.isDead())
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET));
			return;
		}
		
		if (player.transferItem("Transfer", _objectId, _amount, pet.getInventory(), pet) == null)
		{
			_log.warning(RequestGiveItemToPet.class.getName() + ": Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
	}
	
	@Override
	public String getType()
	{
		return REQUESTCIVEITEMTOPET__C__8B;
	}
}
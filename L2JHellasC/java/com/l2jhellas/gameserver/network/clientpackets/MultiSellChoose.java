package com.l2jhellas.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.datatables.xml.MultisellData;
import com.l2jhellas.gameserver.datatables.xml.MultisellData.MultiSellEntry;
import com.l2jhellas.gameserver.datatables.xml.MultisellData.MultiSellIngredient;
import com.l2jhellas.gameserver.datatables.xml.MultisellData.MultiSellListContainer;
import com.l2jhellas.gameserver.model.L2Augmentation;
import com.l2jhellas.gameserver.model.PcInventory;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.templates.L2Armor;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;

public class MultiSellChoose extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(MultiSellChoose.class.getName());
	private static final String _C__A7_MULTISELLCHOOSE = "[C] A7 MultiSellChoose";
	private int _listId;
	private int _entryId;
	private int _amount;
	private int _enchantment;
	private int _transactionTax; // local handling of taxation
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_entryId = readD();
		_amount = readD();
		// _enchantment = readH(); // <---commented this line because it did NOT work!
		_enchantment = _entryId % 100000;
		_entryId = _entryId / 100000;
		_transactionTax = 0; // initialize tax amount to 0...
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();

		if (player == null)
			return;
		
		if (_amount < 1 || _amount > 9999)
			return;
		
		if (!FloodProtectors.performAction(getClient(), Action.MULTISELL))
			return;
		
		final MultiSellListContainer list = MultisellData.getInstance().getList(_listId);
		
		if (list == null)
			return;
		
		if (_entryId < 1 || _entryId > list.getEntries().size())
			return;
		

		
		for (MultiSellEntry entry : list.getEntries())
		{
			if (entry.getEntryId() == _entryId)
			{
				doExchange(player, entry, list.getApplyTaxes(), list.getMaintainEnchantment(), _enchantment);
				return;
			}
		}
	}
	
	@SuppressWarnings("null")
	private void doExchange(L2PcInstance player, MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantment)
	{
		
		PcInventory inv = player.getInventory();
		
		boolean maintainItemFound = false;
		
		// given the template entry and information about maintaining enchantment and applying taxes
		// re-create the instance of the entry that will be used for this exchange
		// i.e. change the enchantment level of select ingredient/products and adena amount appropriately.
		L2Npc merchant = (player.getTarget() instanceof L2Npc) ? (L2Npc) player.getTarget() : null;
		if (merchant == null)
			return;
		
		MultiSellEntry entry = prepareEntry(merchant, templateEntry, applyTaxes, maintainEnchantment, enchantment);
		
		// Generate a list of distinct ingredients and counts in order to check if the correct item-counts
		// are possessed by the player
		ArrayList<MultiSellIngredient> _ingredientsList = new ArrayList<>();
		boolean newIng = true;
		for (MultiSellIngredient e : entry.getIngredients())
		{
			newIng = true;
			
			// at this point, the template has already been modified so that enchantments are properly included
			// whenever they need to be applied. Uniqueness of items is thus judged by item id AND enchantment level
			for (MultiSellIngredient ex : _ingredientsList)
			{
				// if the item was already added in the list, merely increment the count
				// this happens if 1 list entry has the same ingredient twice (example 2 swords = 1 dual)
				if ((ex.getItemId() == e.getItemId()) && (ex.getEnchantmentLevel() == e.getEnchantmentLevel()))
				{
					if ((double) ex.getItemCount() + e.getItemCount() > Integer.MAX_VALUE)
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
						_ingredientsList.clear();
						_ingredientsList = null;
						return;
					}
					ex.setItemCount(ex.getItemCount() + e.getItemCount());
					newIng = false;
				}
			}
			if (newIng)
			{
				// If there is a maintainIngredient, then we do not need to check the enchantment parameter
				// as the enchant level will be checked elsewhere
				if (maintainEnchantment || e.getMantainIngredient())
					maintainItemFound = true;
				
				// if it's a new ingredient, just store its info directly (item id, count, enchantment)
				_ingredientsList.add(MultisellData.getInstance().new MultiSellIngredient(e));
			}
		}
		
		// If there is no maintainIngredient, then we must make sure that the
		// enchantment is not kept from the client packet, as it may have been forged
		if (!maintainItemFound)
			for (MultiSellIngredient product : entry.getProducts())
				product.setEnchantmentLevel(0);
		
		// now check if the player has sufficient items in the inventory to cover the ingredients' expences
		for (MultiSellIngredient e : _ingredientsList)
		{
			if ((double) e.getItemCount() * _amount > Integer.MAX_VALUE)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				_ingredientsList.clear();
				_ingredientsList = null;
				return;
			}
			if (e.getItemId() != 500000)
			{
				// if this is not a list that maintains enchantment, check the count of all items that have the given id.
				// otherwise, check only the count of items with exactly the needed enchantment level
				if (inv.getInventoryItemCount(e.getItemId(), maintainEnchantment ? e.getEnchantmentLevel() : -1) < ((Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMantainIngredient()) ? (e.getItemCount() * _amount) : e.getItemCount()))
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					_ingredientsList.clear();
					_ingredientsList = null;
					return;
				}
			}
			else
			{
				if (player.getClan() == null)
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
					return;
				}
				if (!player.isClanLeader())
				{
					player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
					return;
				}
				if (player.getClan().getReputationScore() < e.getItemCount())
				{
					player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
					return;
				}
			}
		}
		
		_ingredientsList.clear();
		_ingredientsList = null;
		ArrayList<L2Augmentation> augmentation = new ArrayList<>();
		
		for (MultiSellIngredient e : entry.getIngredients())
		{
			if (e.getItemId() != 500000)
			{
				for (MultiSellIngredient a : entry.getProducts())
				{
					if (player.getInventoryLimit() < inv.getSize() + _amount && !ItemTable.getInstance().createDummyItem(a.getItemId()).isStackable())
					{
						player.sendPacket(SystemMessageId.SLOTS_FULL);
						return;
					}
					if (player.getInventoryLimit() < inv.getSize() && ItemTable.getInstance().createDummyItem(a.getItemId()).isStackable())
					{
						player.sendPacket(SystemMessageId.SLOTS_FULL);
						return;
					}
				}
				L2ItemInstance itemToTake = inv.getItemByItemId(e.getItemId()); // initialize and initial guess for the item to take.
				if (itemToTake == null) // this is a cheat, transaction will be aborted and if any items already tanken will not be returned back to inventory!
				{
					_log.severe("Character: " + player.getName() + " is trying to cheat in multisell, merchatnt id:" + merchant.getNpcId());
					return;
				}
				
				if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMantainIngredient())
				{
					// if it's a stackable item, just reduce the amount from the first (only) instance that is found in the inventory
					if (itemToTake.isStackable())
					{
						if (!player.destroyItem("Multisell", itemToTake.getObjectId(), (e.getItemCount() * _amount), player.getTarget(), true))
							return;
					}
					else
					{
						// for non-stackable items, one of two scenaria are possible:
						// a) list maintains enchantment: get the instances that exactly match the requested enchantment level
						// b) list does not maintain enchantment: get the instances with the LOWEST enchantment level
						
						// a) if enchantment is maintained, then get a list of items that exactly match this enchantment
						if (maintainEnchantment)
						{
							// loop through this list and remove (one by one) each item until the required amount is taken.
							L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), e.getEnchantmentLevel());
							for (int i = 0; i < (e.getItemCount() * _amount); i++)
							{
								if (inventoryContents[i].isAugmented())
									augmentation.add(inventoryContents[i].getAugmentation());
								if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1, player.getTarget(), true))
									return;
							}
						}
						else
						// b) enchantment is not maintained. Get the instances with the LOWEST enchantment level
						{
							
							// choice 1. Small number of items exchanged. No sorting.
							for (int i = 1; i <= (e.getItemCount() * _amount); i++)
							{
								L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId());
								
								itemToTake = inventoryContents[0];
								// get item with the LOWEST enchantment level from the inventory...
								// +0 is lowest by default...
								if (itemToTake.getEnchantLevel() > 0)
								{
									for (L2ItemInstance inventoryContent : inventoryContents)
									{
										if (inventoryContent.getEnchantLevel() < itemToTake.getEnchantLevel())
										{
											itemToTake = inventoryContent;
											// nothing will have enchantment less than 0. If a zero-enchanted
											// item is found, just take it
											if (itemToTake.getEnchantLevel() == 0)
												break;
										}
									}
								}
								if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true))
									return;
							}
						}
					}
				}
			}
			else
			{
				int repCost = player.getClan().getReputationScore() - e.getItemCount();
				player.getClan().setReputationScore(repCost, true);
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
				smsg.addNumber(e.getItemCount());
				player.sendPacket(smsg);
				player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
			}
		}
		// Generate the appropriate items
		for (MultiSellIngredient e : entry.getProducts())
		{
			if (ItemTable.getInstance().createDummyItem(e.getItemId()).isStackable())
			{
				inv.addItem("Multisell", e.getItemId(), (e.getItemCount() * _amount), player, player.getTarget());
			}
			else
			{
				L2ItemInstance product = null;
				for (int i = 0; i < (e.getItemCount() * _amount); i++)
				{
					product = inv.addItem("Multisell", e.getItemId(), 1, player, player.getTarget());
					if (maintainEnchantment && (product != null))
					{
						if (i < augmentation.size())
						{
							product.setAugmentation(new L2Augmentation(product, augmentation.get(i).getAugmentationId(), augmentation.get(i).getSkill(), true));
						}
						product.setEnchantLevel(e.getEnchantmentLevel());
					}
				}
			}
			// msg part
			SystemMessage sm;
			
			if (e.getItemCount() * _amount > 1)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(e.getItemId());
				sm.addNumber(e.getItemCount() * _amount);
				player.sendPacket(sm);
				sm = null;
			}
			else
			{
				if (maintainEnchantment && e.getEnchantmentLevel() > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2);
					sm.addNumber(e.getEnchantmentLevel());
					sm.addItemName(e.getItemId());
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
					sm.addItemName(e.getItemId());
				}
				player.sendPacket(sm);
				sm = null;
			}
		}
		player.sendPacket(new ItemList(player, false));
		
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		su = null;
		
		// finally, give the tax to the castle...
		if (merchant != null && merchant.getIsInTown() && merchant.getCastle().getOwnerId() > 0)
			merchant.getCastle().addToTreasury(_transactionTax * _amount);
	}
	
	// Regarding taxation, the following appears to be the case:
	// a) The count of aa remains unchanged (taxes do not affect aa directly).
	// b) 5/6 of the amount of aa is taxed by the normal tax rate.
	// c) the resulting taxes are added as normal adena value.
	// d) normal adena are taxed fully.
	// e) Items other than adena and ancient adena are not taxed even when the list is taxable.
	// example: If the template has an item worth 120aa, and the tax is 10%,
	// then from 120aa, take 5/6 so that is 100aa, apply the 10% tax in adena (10a)
	// so the final price will be 120aa and 10a!
	private MultiSellEntry prepareEntry(L2Npc merchant, MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel)
	{
		MultiSellEntry newEntry = MultisellData.getInstance().new MultiSellEntry();
		newEntry.setEntryId(templateEntry.getEntryId());
		int totalAdenaCount = 0;
		boolean hasIngredient = false;
		
		for (MultiSellIngredient ing : templateEntry.getIngredients())
		{
			// load the ingredient from the template
			MultiSellIngredient newIngredient = MultisellData.getInstance().new MultiSellIngredient(ing);
			
			if (newIngredient.getItemId() == 57 && newIngredient.isTaxIngredient())
			{
				double taxRate = 0.0;
				if (applyTaxes)
				{
					if (merchant != null && merchant.getIsInTown())
						taxRate = merchant.getCastle().getTaxRate();
				}
				
				_transactionTax = (int) Math.round(newIngredient.getItemCount() * taxRate);
				totalAdenaCount += _transactionTax;
				continue; // do not yet add this adena amount to the list as non-taxIngredient adena might be entered later (order not guaranteed)
			}
			else if (ing.getItemId() == 57) // && !ing.isTaxIngredient()
			{
				totalAdenaCount += newIngredient.getItemCount();
				continue; // do not yet add this adena amount to the list as taxIngredient adena might be entered later (order not guaranteed)
			}
			// if it is an armor/weapon, modify the enchantment level appropriately, if necessary
			else if (maintainEnchantment)
			{
				L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
				if ((tempItem instanceof L2Armor) || (tempItem instanceof L2Weapon))
				{
					newIngredient.setEnchantmentLevel(enchantLevel);
					hasIngredient = true;
				}
			}
			
			// finally, add this ingredient to the entry
			newEntry.addIngredient(newIngredient);
		}
		// Next add the adena amount, if any
		if (totalAdenaCount > 0)
			newEntry.addIngredient(MultisellData.getInstance().new MultiSellIngredient(57, totalAdenaCount, false, false));
		
		// Now modify the enchantment level of products, if necessary
		for (MultiSellIngredient ing : templateEntry.getProducts())
		{
			// load the ingredient from the template
			MultiSellIngredient newIngredient = MultisellData.getInstance().new MultiSellIngredient(ing);
			
			if (maintainEnchantment && hasIngredient)
			{
				// if it is an armor/weapon, modify the enchantment level appropriately
				// (note, if maintain enchantment is "false" this modification will result to a +0)
				L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
				if ((tempItem instanceof L2Armor) || (tempItem instanceof L2Weapon))
					newIngredient.setEnchantmentLevel(enchantLevel);
			}
			newEntry.addProduct(newIngredient);
		}
		return newEntry;
	}
	
	@Override
	public String getType()
	{
		return _C__A7_MULTISELLCHOOSE;
	}
}
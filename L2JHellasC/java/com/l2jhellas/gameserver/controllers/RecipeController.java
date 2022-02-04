package com.l2jhellas.gameserver.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.xml.RecipeData;
import com.l2jhellas.gameserver.model.L2ManufactureItem;
import com.l2jhellas.gameserver.model.L2RecipeInstance;
import com.l2jhellas.gameserver.model.L2RecipeList;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.RecipeBookItemList;
import com.l2jhellas.gameserver.network.serverpackets.RecipeItemMakeInfo;
import com.l2jhellas.gameserver.network.serverpackets.RecipeShopItemInfo;
import com.l2jhellas.gameserver.network.serverpackets.SetupGauge;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public class RecipeController
{
	static final Logger _log = Logger.getLogger(RecipeController.class.getName());
	
	private static RecipeController _instance;
	private Map<Integer, L2RecipeList> _lists;
	protected static final Map<L2PcInstance, RecipeItemMaker> _activeMakers = Collections.synchronizedMap(new WeakHashMap<L2PcInstance, RecipeItemMaker>());
	
	public static RecipeController getInstance()
	{
		return _instance == null ? _instance = new RecipeController() : _instance;
	}
	
	public synchronized void requestBookOpen(L2PcInstance player, boolean isDwarvenCraft)
	{
		RecipeItemMaker maker = null;
		if (Config.ALT_GAME_CREATION)
			maker = _activeMakers.get(player);
		
		if (maker == null)
		{
			player.sendPacket(new RecipeBookItemList(player,isDwarvenCraft));
			return;
		}
		
		player.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
		
		maker = null;
		
		return;
	}
	
	public synchronized void requestMakeItemAbort(L2PcInstance player)
	{
		if(_activeMakers.get(player) != null)
		   _activeMakers.remove(player);
	}
	
	public synchronized void requestManufactureItem(L2PcInstance manufacturer, int recipeListId, L2PcInstance player)
	{
		L2RecipeList recipeList = getValidRecipeList(player, recipeListId);
		
		if (recipeList == null)
			return;

		if(!manufacturer.hasRecipeList(recipeListId,recipeList.isDwarvenRecipe()))
		{		
			Util.handleIllegalPlayerAction(player, "Warning!! Player " + player.getName() + " of account " + player.getAccountName() + " tried to set recipe which he dont have.", Config.DEFAULT_PUNISH);
			return;
		}
		
		RecipeItemMaker maker;
		
		if (Config.ALT_GAME_CREATION && (maker = _activeMakers.get(manufacturer)) != null) // check if busy
		{
			player.sendMessage("Manufacturer is busy, please try later.");
			return;
		}
		
		maker = new RecipeItemMaker(manufacturer, recipeList, player);
		if (maker._isValid)
		{
			if (Config.ALT_GAME_CREATION)
			{
				_activeMakers.put(manufacturer, maker);
				ThreadPoolManager.getInstance().scheduleGeneral(maker, 100);
			}
			else
			{
				maker.run();
			}
		}
		maker = null;
		recipeList = null;
	}
	
	public synchronized void requestMakeItem(L2PcInstance player, int recipeListId)
	{
		if (player.isInDuel() || player.isInCombat())
		{
			player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			return;
		}
		
		L2RecipeList recipeList = getValidRecipeList(player, recipeListId);
		
		if (recipeList == null)
			return;
		
		if(!player.hasRecipeList(recipeListId,recipeList.isDwarvenRecipe()))
		{		
			Util.handleIllegalPlayerAction(player, "Warning!! Player " + player.getName() + " of account " + player.getAccountName() + " tried to set recipe which he dont have.", Config.DEFAULT_PUNISH);
			return;
		}
		
		RecipeItemMaker maker;
		
		if (Config.ALT_GAME_CREATION && (maker = _activeMakers.get(player)) != null)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
			sm.addString("You are busy creating ");
			sm.addItemName(recipeList.getItemId());
			player.sendPacket(sm);
			return;
		}
		
		maker = new RecipeItemMaker(player, recipeList, player);
		if (maker._isValid)
		{
			if (Config.ALT_GAME_CREATION)
			{
				_activeMakers.put(player, maker);
				ThreadPoolManager.getInstance().scheduleGeneral(maker, 100);
			}
			else
			{
				maker.run();
			}
		}
		maker = null;
		recipeList = null;
	}
	
	private class RecipeItemMaker implements Runnable
	{
		protected boolean _isValid;
		protected List<TempItem> _items = null;
		protected final L2RecipeList _recipeList;
		protected final L2PcInstance _player;
		protected final L2PcInstance _target;
		protected final L2Skill _skill;
		protected final int _skillId;
		protected final int _skillLevel;
		protected double _creationPasses;
		protected double _manaRequired;
		protected int _price;
		protected int _totalItems;
		
		@SuppressWarnings("unused")
		protected int _materialsRefPrice;
		protected int _delay;
		
		public RecipeItemMaker(L2PcInstance pPlayer, L2RecipeList pRecipeList, L2PcInstance pTarget)
		{
			_player = pPlayer;
			_target = pTarget;
			_recipeList = pRecipeList;
			
			_isValid = false;
			_skillId = _recipeList.isDwarvenRecipe() ? L2Skill.SKILL_CREATE_DWARVEN : L2Skill.SKILL_CREATE_COMMON;
			_skillLevel = _player.getSkillLevel(_skillId);
			_skill = _player.getKnownSkill(_skillId);
			
			_player.isInCraftMode(true);
			
			if (_player.isAlikeDead())
			{
				_player.sendMessage("Dead people don't craft.");
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_target.isAlikeDead())
			{
				_target.sendMessage("Dead customers can't use manufacture.");
				_target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_target.isProcessingTransaction())
			{
				_target.sendMessage("You are busy.");
				_target.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_player.isProcessingTransaction())
			{
				if (_player != _target)
					_target.sendMessage("Manufacturer " + _player.getName() + " is busy.");

				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_recipeList == null || _recipeList.getRecipes().length == 0)
			{
				_player.sendMessage("No such recipe");
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			_manaRequired = _recipeList.getMpCost();
			
			if (_recipeList.getLevel() > _skillLevel)
			{
				_player.sendMessage("Need skill level " + _recipeList.getLevel());
				_player.sendPacket(ActionFailed.STATIC_PACKET);
				abort();
				return;
			}
			
			if (_player != _target)
			{
				for (L2ManufactureItem temp : _player.getCreateList().getList())
				{
					if (temp.getRecipeId() == _recipeList.getId())
					{
						_price = temp.getCost();
						if (_target.getAdena() < _price)
						{
							_target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
							abort();
							return;
						}
						break;
					}
				}
			}
			
			if ((_items = listItems(false)) == null)
			{
				abort();
				return;
			}
			
			for (TempItem i : _items)
			{
				_materialsRefPrice += i.getReferencePrice() * i.getQuantity();
				_totalItems += i.getQuantity();
			}
			
			if (_player.getCurrentMp() < _manaRequired)
			{
				_target.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
				abort();
				return;
			}
			
			_creationPasses = _totalItems / _skillLevel + (_totalItems % _skillLevel != 0 ? 1 : 0);
			
			if (Config.ALT_GAME_CREATION && _creationPasses != 0)
				_manaRequired /= _creationPasses;
			
			updateMakeInfo(true);
			statusUpdate();
			
			_player.isInCraftMode(false);
			_isValid = true;
		}
		
		@Override
		public void run()
		{
			if (!Config.IS_CRAFTING_ENABLED)
			{
				_target.sendMessage("Item creation is currently disabled.");
				abort();
				return;
			}
			
			if (_player == null || _target == null)
			{
				abort();
				return;
			}
			
			if (!_player.isOnline() || !_target.isOnline())
			{
				abort();
				return;
			}
			
			if (Config.ALT_GAME_CREATION && _activeMakers.get(_player) == null)
			{
				if (_target != _player)
				{
					_target.sendMessage("Manufacture aborted");
					_player.sendMessage("Manufacture aborted");
				}
				else
				{
					_player.sendMessage("Item creation aborted");
				}
				
				abort();
				return;
			}
			
			if (Config.ALT_GAME_CREATION && !_items.isEmpty())
			{
				if (!validateMp())
					return;
				
				_player.reduceCurrentMp(_manaRequired);
				statusUpdate();
				grabSomeItems();
				
				if (!_items.isEmpty())
				{
					_delay = (int) (Config.ALT_GAME_CREATION_SPEED * _player.getMReuseRate(_skill) * GameTimeController.TICKS_PER_SECOND / Config.RATE_CONSUMABLE_COST) * GameTimeController.MILLIS_IN_TICK;
					
					_player.broadcastPacket(new MagicSkillUse(_player, _skillId, _skillLevel, _delay, 0));				
					_player.sendPacket(new SetupGauge(0, _delay));
					
					ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
				}
				else
				{
					_player.sendPacket(new SetupGauge(0, _delay));
					
					try
					{
						Thread.sleep(_delay);
					}
					catch (InterruptedException e)
					{
					}
					finally
					{
						finishCrafting();
					}
				}
			}
			else
				finishCrafting();
		}
		
		private void finishCrafting()
		{
			if (!Config.ALT_GAME_CREATION)
				_player.reduceCurrentMp(_manaRequired);
			
			if (_target != _player && _price > 0)
			{
				L2ItemInstance adenatransfer = _target.transferItem("PayManufacture", _target.getInventory().getAdenaInstance().getObjectId(), _price, _player.getInventory(), _player);
				
				if (adenatransfer == null)
				{
					_target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
					abort();
					return;
				}
				adenatransfer = null;
			}
			
			if ((_items = listItems(true)) == null)
			{
			}
			else if (Rnd.get(100) < _recipeList.getSuccessRate())
			{
				rewardPlayer();
				updateMakeInfo(true);
			}
			else
			{
				if (_target != _player)
				{
					_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CREATION_OF_S2_FOR_S1_AT_S3_ADENA_FAILED).addCharName(_target).addItemName(_recipeList.getItemId()).addItemNumber(_price));
					_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_FAILED_TO_CREATE_S2_FOR_S3_ADENA).addCharName(_player).addItemName(_recipeList.getItemId()).addItemNumber(_price));
				}
				else
					_target.sendPacket(SystemMessageId.ITEM_MIXING_FAILED);
				
				updateMakeInfo(false);
			}
			statusUpdate();
			_activeMakers.remove(_player);
			_player.isInCraftMode(false);
			_target.sendPacket(new ItemList(_target, false));
		}
		
		private void updateMakeInfo(boolean success)
		{
			if (_target == _player)
				_target.sendPacket(new RecipeItemMakeInfo(_recipeList.getId(), _target, success));
			else
				_target.sendPacket(new RecipeShopItemInfo(_player.getObjectId(), _recipeList.getId()));
		}

		private void statusUpdate()
		{
			final StatusUpdate su = new StatusUpdate(_target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_MP, (int) _target.getCurrentMp());
			su.addAttribute(StatusUpdate.CUR_LOAD, _target.getCurrentLoad());
			_target.sendPacket(su);
		}
		
		private void grabSomeItems()
		{
			int numItems = _skillLevel;
			
			while (numItems > 0 && !_items.isEmpty())
			{
				TempItem item = _items.get(0);
				
				int count = item.getQuantity();
				
				if (count >= numItems)
					count = numItems;
				
				item.setQuantity(item.getQuantity() - count);
				
				if (item.getQuantity() <= 0)
					_items.remove(0);
				else
					_items.set(0, item);
				
				numItems -= count;
				
				if (_target == _player)
					_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED).addNumber(count).addItemName(item.getItemId()));
				else
					_target.sendMessage("Manufacturer " + _player.getName() + " used " + count + " " + item.getItemName());
				item = null;
			}
		}
		
		private boolean validateMp()
		{
			if (_player.getCurrentMp() < _manaRequired)
			{
				if (Config.ALT_GAME_CREATION)
				{
					_player.sendPacket(new SetupGauge(0, _delay));
					ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + _delay);
				}
				else
				{
					_target.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
					abort();
				}
				return false;
			}
			return true;
		}
		
		private List<TempItem> listItems(boolean remove)
		{
			L2RecipeInstance[] recipes = _recipeList.getRecipes();
			final Inventory inv = _target.getInventory();
			List<TempItem> materials = new ArrayList<>();
			
			for (L2RecipeInstance recipe : recipes)
			{
				int quantity = _recipeList.isConsumable() ? (int) (recipe.getQuantity() * Config.RATE_CONSUMABLE_COST) : (int) recipe.getQuantity();
				
				if (quantity > 0)
				{
					L2ItemInstance item = inv.getItemByItemId(recipe.getItemId());
					
					if (item == null || item.getCount() < quantity)
					{
						_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MISSING_S2_S1_TO_CREATE).addItemName(item.getItemId()).addItemNumber((item == null) ? quantity : quantity - item.getCount()));
						abort();
						return null;
					}
					
					materials.add(new TempItem(item, quantity));
				}
			}
						
			if (remove)
			{
				for (TempItem tmp : materials)
				{
					inv.destroyItemByItemId("Manufacture", tmp.getItemId(), tmp.getQuantity(), _target, _player);
					
					if (tmp.getQuantity() > 1)
						_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(tmp.getItemId()).addItemNumber(tmp.getQuantity()));
					else
						_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(tmp.getItemId()));
				}
			}
			
			return materials;
		}
		
		private void abort()
		{
			updateMakeInfo(false);
			_player.isInCraftMode(false);
			_activeMakers.remove(_player);
		}
		
		private class TempItem
		{
			private final int _itemId;
			private int _quantity;
			private final int _referencePrice;
			private final String _itemName;
			
			public TempItem(L2ItemInstance item, int quantity)
			{
				super();
				_itemId = item.getItemId();
				_quantity = quantity;
				_itemName = item.getItem().getItemName();
				_referencePrice = item.getReferencePrice();
			}
			
			public int getQuantity()
			{
				return _quantity;
			}
			
			public void setQuantity(int quantity)
			{
				_quantity = quantity;
			}
			
			public int getReferencePrice()
			{
				return _referencePrice;
			}
			
			public int getItemId()
			{
				return _itemId;
			}
			
			public String getItemName()
			{
				return _itemName;
			}
		}
		
		private void rewardPlayer()
		{
			int itemId = _recipeList.getItemId();
			int itemCount = _recipeList.getCount();
			
			L2ItemInstance createdItem = _target.getInventory().addItem("Manufacture", itemId, itemCount, _target, _player);
			
			if (itemCount > 1)
				_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addNumber(itemCount));
			else
				_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(itemId));

			if (_target != _player)
			{
				if (itemCount == 1)
				{
					_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_CREATED_FOR_S1_FOR_S3_ADENA).addString(_target.getName()).addItemName(itemId).addItemNumber(_price));
					_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CREATED_S2_FOR_S3_ADENA).addString(_player.getName()).addItemName(itemId).addItemNumber(_price));
				}
				else
				{
					_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S3_S_CREATED_FOR_S1_FOR_S4_ADENA).addString(_target.getName()).addNumber(itemCount).addItemName(itemId).addItemNumber(_price));
					_target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CREATED_S2_S3_S_FOR_S4_ADENA).addString(_player.getName()).addNumber(itemCount).addItemName(itemId).addItemNumber(_price));
				}
			}
			
			if (Config.ALT_GAME_CREATION)
			{
				int recipeLevel = _recipeList.getLevel();
				int exp = createdItem.getReferencePrice() * itemCount;
				
				if (exp < 0)
					exp = 0;
				
				exp /= recipeLevel;
				for (int i = _skillLevel; i > recipeLevel; i--)
				{
					exp /= 4;
				}
				
				int sp = exp / 10;
				
				_player.addExpAndSp((int) _player.calcStat(Stats.EXPSP_RATE, exp * Config.ALT_GAME_CREATION_XP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null), (int) _player.calcStat(Stats.EXPSP_RATE, sp * Config.ALT_GAME_CREATION_SP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null));
			}
			updateMakeInfo(true);
		}
	}
	
	private static L2RecipeList getValidRecipeList(L2PcInstance player, int id)
	{
		L2RecipeList recipeList = RecipeData.getInstance().getRecipeList(id - 1);
		
		if (recipeList == null || recipeList.getRecipes().length == 0)
		{
			player.sendMessage("No recipe for: " + id);
			player.isInCraftMode(false);
			return null;
		}
		return recipeList;
	}
	
	public L2RecipeList getRecipeByItemId(int itemId)
	{
		for (int i = 0; i < _lists.size(); i++)
		{
			L2RecipeList find = _lists.get(new Integer(i));
			if (find.getRecipeId() == itemId)
				return find;
		}
		return null;
	}
}
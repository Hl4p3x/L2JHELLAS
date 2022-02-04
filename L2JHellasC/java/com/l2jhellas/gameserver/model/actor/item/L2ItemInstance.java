package com.l2jhellas.gameserver.model.actor.item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.enums.items.ShotType;
import com.l2jhellas.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jhellas.gameserver.model.L2Augmentation;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.DropItem;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SpawnItem;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.gameserver.templates.L2Armor;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.util.IllegalPlayerAction;
import com.l2jhellas.util.Util;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class L2ItemInstance extends L2Object
{
	private static final Logger _log = Logger.getLogger(L2ItemInstance.class.getName());
	
	private int _ownerId;
	private int _dropperObjectId = 0;
	
	private int _count;
	
	private int _initCount;
	
	private int _time;
	
	private boolean _decrease = false;
	
	private final int _itemId;
	
	private final L2Item _item;
	
	private ItemLocation _loc;
	
	private int _locData;
	
	private int _enchantLevel;
	
	private int _priceSell;
	
	private int _priceBuy;
	
	private boolean _wear;
	
	private L2Augmentation _augmentation = null;
	
	private int _mana = -1;
	private boolean _consumingMana = false;
	private static final int MANA_CONSUMPTION_RATE = 60000;
	
	private int _type1;
	private int _type2;
	
	private long _dropTime;
	
	public void unChargeAllShots()
	{
		_shotsMask = 0;
	}
	
	private int _shotsMask = 0;
	
	private boolean _chargedFishtshot = false;
	
	private boolean _protected;
	
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	private int _lastChange = 2; // 1 ??, 2 modified, 3 removed
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.
	
	private ScheduledFuture<?> itemLootShedule = null;
	
	private final ReentrantLock _dbLock = new ReentrantLock();
	
	public L2ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		_itemId = itemId;
		_item = ItemTable.getInstance().getTemplate(itemId);
		if (_itemId == 0 || _item == null)
			throw new IllegalArgumentException();
		_count = 1;
		_loc = ItemLocation.VOID;
		_type1 = 0;
		_type2 = 0;
		_dropTime = 0;
		_mana = _item.getDuration();
	}
	
	public L2ItemInstance(int objectId, L2Item item)
	{
		super(objectId);
		_itemId = item.getItemId();
		_item = item;
		if (_itemId == 0 || _item == null)
			throw new IllegalArgumentException();
		_count = 1;
		_loc = ItemLocation.VOID;
		_mana = _item.getDuration();
	}
	
	public void setOwnerId(String process, int owner_id, L2PcInstance creator, L2Object reference)
	{
		setOwnerId(owner_id);
	}
	
	public void setOwnerId(int owner_id)
	{
		if (owner_id == _ownerId)
			return;
		
		_ownerId = owner_id;
		_storedInDb = false;
	}
	
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	public void setLocation(ItemLocation loc)
	{
		setLocation(loc, 0);
	}
	
	public void setLocation(ItemLocation loc, int loc_data)
	{
		if (loc == _loc && loc_data == _locData)
			return;
		_loc = loc;
		_locData = loc_data;
		_storedInDb = false;
	}
	
	public ItemLocation getLocation()
	{
		return _loc;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public void changeCount(String process, int count, L2PcInstance creator, L2Object reference)
	{
		if (count == 0)
			return;
		if (count > 0 && _count > Integer.MAX_VALUE - count)
			_count = Integer.MAX_VALUE;
		else
			_count += count;
		if (_count < 0)
			_count = 0;
		_storedInDb = false;
	}
	
	// No logging (function designed for shots only)
	public void changeCountWithoutTrace(String process, int count, L2PcInstance creator, L2Object reference)
	{
		if (count == 0)
			return;
		if (count > 0 && _count > Integer.MAX_VALUE - count)
			_count = Integer.MAX_VALUE;
		else
			_count += count;
		if (_count < 0)
			_count = 0;
		_storedInDb = false;
	}
	
	public void setCount(int count)
	{
		if (_count == count)
			return;
		
		_count = count >= -1 ? count : 0;
		_storedInDb = false;
	}
	
	public boolean isEquipable()
	{
		return !(_item.getBodyPart() == 0 || _item instanceof L2EtcItem);
	}
	
	public boolean isEquipped()
	{
		return _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP;
	}
	
	public int getLocationSlot()
	{
		assert (_loc == ItemLocation.PAPERDOLL) || (_loc == ItemLocation.PET_EQUIP) || (_loc == ItemLocation.INVENTORY) || (_loc == ItemLocation.FREIGHT);
		return _locData;
	}
	
	public int getEquipSlot()
	{
		return _locData;
	}
	
	public L2Item getItem()
	{
		return _item;
	}
	
	public int getCustomType1()
	{
		return _type1;
	}
	
	public int getCustomType2()
	{
		return _type2;
	}
	
	public void setCustomType1(int newtype)
	{
		_type1 = newtype;
	}
	
	public void setCustomType2(int newtype)
	{
		_type2 = newtype;
	}
	
	public void setDropTime(long time)
	{
		_dropTime = time;
	}
	
	public long getDropTime()
	{
		return _dropTime;
	}
	
	public boolean isOlyRestrictedItem()
	{
		return (Config.OLY_RESTRICTED_ITEMS_LIST.contains(_itemId));
	}
	
	public boolean isWear()
	{
		return _wear;
	}
	
	public void setWear(boolean newwear)
	{
		_wear = newwear;
	}
	
	public Enum<?> getItemType()
	{
		return _item.getItemType();
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public final int getCrystalCount()
	{
		return _item.getCrystalCount(_enchantLevel);
	}
	
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}
	
	public String getItemName()
	{
		return _item.getItemName();
	}
	
	public int getPriceToSell()
	{
		return (isConsumable() ? (int) (_priceSell * Config.RATE_CONSUMABLE_COST) : _priceSell);
	}
	
	public void setPriceToSell(int price)
	{
		_priceSell = price;
		_storedInDb = false;
	}
	
	public int getPriceToBuy()
	{
		return (isConsumable() ? (int) (_priceBuy * Config.RATE_CONSUMABLE_COST) : _priceBuy);
	}
	
	public void setPriceToBuy(int price)
	{
		_priceBuy = price;
		_storedInDb = false;
	}
	
	public int getLastChange()
	{
		return _lastChange;
	}
	
	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}
	
	public boolean isStackable()
	{
		return _item.isStackable();
	}
	
	public boolean isDropable()
	{
		return isAugmented() ? false : _item.isDropable();
	}
	
	public boolean isDestroyable()
	{
		return _item.isDestroyable();
	}
	
	public boolean isTradeable()
	{
		return isAugmented() ? false : _item.isTradeable();
	}
	
	public boolean isConsumable()
	{
		return _item.isConsumable();
	}
	
	public boolean isAvailable(L2PcInstance player, boolean allowAdena)
	{
		return ((!isEquipped()) // Not equipped
			&& (getItem().getType2() != 3) // Not Quest Item
			&& (getItem().getType2() != 4 || getItem().getType1() != 1) // what does this mean?
			&& (player.getPet() == null || getObjectId() != player.getPet().getControlItemId()) // Not Control item of currently summoned pet
			&& (player.getActiveEnchantItem() != this) // Not momentarily used enchant scroll
			&& (allowAdena || getItemId() != 57) && (player.getCurrentSkill() == null || player.getCurrentSkill().getSkill().getItemConsumeId() != getItemId()) && (isTradeable()));
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		// this causes the validate position handler to do the pickup if the location is reached.
		// mercenary tickets can only be picked up by the castle owner.
		if ((_itemId >= 3960 && _itemId <= 4021 && player.isInParty()) || (_itemId >= 3960 && _itemId <= 3969 && !player.isCastleLord(1)) || (_itemId >= 3973 && _itemId <= 3982 && !player.isCastleLord(2)) || (_itemId >= 3986 && _itemId <= 3995 && !player.isCastleLord(3)) || (_itemId >= 3999 && _itemId <= 4008 && !player.isCastleLord(4)) || (_itemId >= 4012 && _itemId <= 4021 && !player.isCastleLord(5)) || (_itemId >= 5205 && _itemId <= 5214 && !player.isCastleLord(6)) || (_itemId >= 6779 && _itemId <= 6788 && !player.isCastleLord(7)) || (_itemId >= 7973 && _itemId <= 7982 && !player.isCastleLord(8)) || (_itemId >= 7918 && _itemId <= 7927 && !player.isCastleLord(9)))
		{
			if (player.isInParty()) // do not allow owner who is in party to pick tickets up
				player.sendMessage("You cannot pickup mercenaries while in a party.");
			else
				player.sendMessage("Only the castle lord can pickup mercenaries.");
			
			player.setTarget(this);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			if (player.getFreight().getItemByObjectId(getObjectId()) != null)
			{
				player.setTarget(this);
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to pickup Freight Items", IllegalPlayerAction.PUNISH_KICK);
			}
			else
				if (!player.isFlying())
			         player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this);
		}
	}
	
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	public void setEnchantLevel(int enchantLevel)
	{
		if (_enchantLevel == enchantLevel)
			return;
		_enchantLevel = enchantLevel;
		_storedInDb = false;
	}
	
	public int getPDef()
	{
		if (_item instanceof L2Armor)
			return ((L2Armor) _item).getPDef();
		return 0;
	}
	
	public boolean isAugmented()
	{
		return _augmentation == null ? false : true;
	}
	
	public L2Augmentation getAugmentation()
	{
		return _augmentation;
	}
	
	public boolean setAugmentation(L2Augmentation augmentation)
	{
		// there shall be no previous augmentation..
		if (_augmentation != null)
			return false;
		_augmentation = augmentation;
		return true;
	}
	
	public void removeAugmentation()
	{
		if (_augmentation == null)
			return;
		_augmentation.deleteAugmentationData();
		_augmentation = null;
	}
	
	public class ScheduleConsumeManaTask implements Runnable
	{
		private final L2ItemInstance _shadowItem;
		
		public ScheduleConsumeManaTask(L2ItemInstance item)
		{
			_shadowItem = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				// decrease mana
				if (_shadowItem != null)
					_shadowItem.decreaseMana(true);
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	public boolean isShadowItem()
	{
		return (_mana >= 0);
	}
	
	public void setMana(int mana)
	{
		_mana = mana;
	}
	
	public int getMana()
	{
		return _mana;
	}
	
	public void decreaseMana(boolean resetConsumingMana)
	{
		if (!isShadowItem())
			return;
		
		if (_mana > 0)
			_mana--;
		
		if (_storedInDb)
			_storedInDb = false;
		if (resetConsumingMana)
			_consumingMana = false;
		
		L2PcInstance player = (L2World.getInstance().getPlayer(getOwnerId()));
		if (player != null)
		{
			switch (_mana)
			{
				case 10:
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10).addString(getItemName()));
					break;
				case 5:
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5).addString(getItemName()));
					break;
				case 1:
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1).addString(getItemName()));
					break;
			}
			
			if (_mana == 0) // The life time has expired
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0).addString(getItemName()));
				
				// unequip
				if (isEquipped())
				{
					L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getEquipSlot());
					InventoryUpdate iu = new InventoryUpdate();
					for (L2ItemInstance element : unequiped)
					{
						player.checkSSMatch(null, element);
						iu.addModifiedItem(element);
					}
					player.sendPacket(iu);
				}
				
				if (getLocation() != ItemLocation.WAREHOUSE)
				{
					// destroy
					player.getInventory().destroyItem("L2ItemInstance", this, player, null);
					
					// send update
					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(this);
					player.sendPacket(iu);
					
					StatusUpdate su = new StatusUpdate(player.getObjectId());
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
					
				}
				else
				{
					player.getWarehouse().destroyItem("L2ItemInstance", this, player, null);
				}
				
				// delete from world
				L2World.getInstance().removeObject(this);
			}
			else
			{
				// Reschedule if still equipped
				if (!_consumingMana && isEquipped())
				{
					scheduleConsumeManaTask();
				}
				if (getLocation() != ItemLocation.WAREHOUSE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(this);
					player.sendPacket(iu);
				}
			}
		}
	}
	
	private void scheduleConsumeManaTask()
	{
		_consumingMana = true;
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleConsumeManaTask(this), MANA_CONSUMPTION_RATE);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}
	
	public void setChargedFishshot(boolean type)
	{
		_chargedFishtshot = type;
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		if (charged)
			_shotsMask |= type.getMask();
		else
			_shotsMask &= ~type.getMask();
	}

	@Override
	public boolean isChargedShot(ShotType type)
	{
		return (_shotsMask & type.getMask()) == type.getMask();
	}
	
	public Func[] getStatFuncs(L2Character player)
	{
		return getItem().getStatFuncs(this, player);
	}
	
	public void updateDatabase()
	{
		if (isWear()) // avoid saving weared items
			return;
		
		_dbLock.lock();
		try
		{
			if (_existsInDb)
			{
				if (_ownerId == 0 || _loc == ItemLocation.VOID || (getCount() == 0 && _loc != ItemLocation.LEASE))
					removeFromDb();
				else
					updateInDb();
			}
			else
			{
				if (_ownerId == 0 || _loc == ItemLocation.VOID || (getCount() == 0 && _loc != ItemLocation.LEASE))
					return;
				
				insertIntoDb();
			}
		}
		finally
		{
			_dbLock.unlock();
		}
	}

	public static L2ItemInstance restoreFromDb(int ownerId, ResultSet rs)
	{
		L2ItemInstance inst = null;
		int objectId = 0;
		try
		{
			objectId = rs.getInt(1);
			int item_id = rs.getInt("item_id");
			int count = rs.getInt("count");
			ItemLocation loc = ItemLocation.valueOf(rs.getString("loc"));
			int loc_data = rs.getInt("loc_data");
			int enchant_level = rs.getInt("enchant_level");
			int custom_type1 = rs.getInt("custom_type1");
			int custom_type2 = rs.getInt("custom_type2");
			int price_sell = rs.getInt("price_sell");
			int price_buy = rs.getInt("price_buy");
			int manaLeft = rs.getInt("mana_left");

			L2Item item = ItemTable.getInstance().getTemplate(item_id);

			if (item == null)
				return null;

			inst = new L2ItemInstance(objectId, item);
			inst._existsInDb = true;
			inst._storedInDb = true;
			inst._ownerId = ownerId;
			inst._count = count;
			inst._enchantLevel = enchant_level;
			inst._type1 = custom_type1;
			inst._type2 = custom_type2;
			inst._loc = loc;
			inst._locData = loc_data;
			inst._priceSell = price_sell;
			inst._priceBuy = price_buy;

			// Setup life time for shadow weapons
			inst._mana = manaLeft;

			// consume 1 mana
			if (inst._mana > 0 && inst.getLocation() == ItemLocation.PAPERDOLL)
				inst.decreaseMana(false);

			// if mana left is 0 delete this item
			if (inst._mana == 0)
			{
				inst.removeFromDb();
				return null;
			}
			else if (inst._mana > 0 && inst.getLocation() == ItemLocation.PAPERDOLL)
				inst.scheduleConsumeManaTask();

			// load weapon augmentation
			if (inst.isWeapon() && inst.isEquipable())
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement ps = con.prepareStatement("SELECT attributes,skill,level FROM augmentations WHERE item_id=?"))
				{
					ps.setInt(1, inst.getObjectId());

					try (ResultSet rsa = ps.executeQuery())
					{
						if (rsa.next())
							inst._augmentation = new L2Augmentation(inst, rsa.getInt("attributes"), rsa.getInt("skill"), rsa.getInt("level"), false);
					}
				}
				catch (Exception e)
				{
					_log.severe(L2ItemInstance.class.getName() + ": Couldn't restore augmentation for " + objectId + " from DB:" + e);
				}
			}

		}
		catch (Exception e)
		{	
			_log.severe(L2ItemInstance.class.getName() + ": Could not restore item " + objectId + " from DB:" +e);
		}
		return inst;
	}
	
	public final void dropMe(L2Character dropper, int x, int y, int z)
	{
		setDropperObjectId(dropper != null ? dropper.getObjectId() : 0);
		
		spawnMe(x, y, z);
		setDropTime(System.currentTimeMillis());
		
		if (Config.SAVE_DROPPED_ITEM)
			ItemsOnGroundManager.getInstance().save(this);
		
		setDropperObjectId(0);
	}
	
	private void updateInDb()
	{
		if (_wear)
			return;
		if (_storedInDb)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,price_sell=?,price_buy=?,custom_type1=?,custom_type2=?,mana_left=? " + "WHERE object_id = ?"))
		{
			statement.setInt(1, _ownerId);
			statement.setInt(2, getCount());
			statement.setString(3, _loc.name());
			statement.setInt(4, _locData);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, _priceSell);
			statement.setInt(7, _priceBuy);
			statement.setInt(8, getCustomType1());
			statement.setInt(9, getCustomType2());
			statement.setInt(10, getMana());
			statement.setInt(11, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
		}		
		catch (Exception e)
		{
			_log.severe(L2ItemInstance.class.getName() + ": Could not update item " + getObjectId() + " in DB: Reason: " + "Duplicate itemId");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void insertIntoDb()
	{
		assert !_existsInDb && getObjectId() != 0;

		if (_wear)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,price_sell,price_buy,object_id,custom_type1,custom_type2,mana_left) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"))
		{
			statement.setInt(1, _ownerId);
			statement.setInt(2, _itemId);
			statement.setInt(3, getCount());
			statement.setString(4, _loc.name());
			statement.setInt(5, _locData);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, _priceSell);
			statement.setInt(8, _priceBuy);
			statement.setInt(9, getObjectId());
			statement.setInt(10, _type1);
			statement.setInt(11, _type2);
			statement.setInt(12, getMana());
			
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
		}
		catch (Exception e)
		{
			_log.severe(L2ItemInstance.class.getName() + ": Could not insert item " + getObjectId() + " into DB: Reason: " + "Duplicate itemId");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void removeFromDb()
	{
		if (_wear)
			return;
		
		// delete augmentation data
		if (isAugmented())
			_augmentation.deleteAugmentationData();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE object_id=?"))
		{
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			_existsInDb = false;
			_storedInDb = false;
		}
		catch (Exception e)
		{
			_log.severe(L2ItemInstance.class.getName() + ": Could not delete item " + getObjectId() + " in DB:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
		
	public boolean canBeRemoved()
	{
		final long autoDestroyTime = isHerb() ? 14000 : Config.AUTODESTROY_ITEM_AFTER <= 0 ? 3600000 : Config.AUTODESTROY_ITEM_AFTER;	
		long curtime = System.currentTimeMillis();
		
	    if ((getDropTime() == 0) || (getLocation() != ItemLocation.VOID))
		     return true;
	    else if(curtime - getDropTime()  > autoDestroyTime)
		{		
			decayMe();
			
			if (Config.SAVE_DROPPED_ITEM)
				ItemsOnGroundManager.getInstance().removeObject(this);
			
		    return true;
		}
		
        return false;
    }
	
	@Override
	public String toString()
	{
		return "" + _item;
	}
	
	public void resetOwnerTimer()
	{
		if (itemLootShedule != null)
			itemLootShedule.cancel(true);
		itemLootShedule = null;
	}
	
	public void setItemLootShedule(ScheduledFuture<?> sf)
	{
		itemLootShedule = sf;
	}
	
	public ScheduledFuture<?> getItemLootShedule()
	{
		return itemLootShedule;
	}
	
	public void setProtected(boolean is_protected)
	{
		_protected = is_protected;
	}
	
	public boolean isProtected()
	{
		return _protected;
	}
	
	public boolean isNightLure()
	{
		return ((_itemId >= 8505 && _itemId <= 8513) || _itemId == 8485);
	}
	
	public void setCountDecrease(boolean decrease)
	{
		_decrease = decrease;
	}
	
	public boolean getCountDecrease()
	{
		return _decrease;
	}
	
	public void setInitCount(int InitCount)
	{
		_initCount = InitCount;
	}
	
	public int getInitCount()
	{
		return _initCount;
	}
	
	public void restoreInitCount()
	{
		if (_decrease)
			_count = _initCount;
	}
	
	public void setTime(int time)
	{
		if (time > 0)
			_time = time;
		else
			_time = 0;
	}
	
	public int getTime()
	{
		return _time;
	}
	
	public boolean isPetItem()
	{
		return getItem().isPetItem();
	}
	
	public boolean isPotion()
	{
		return getItem().isPotion();
	}
	
	public boolean isHerb()
	{
		return getItem().getItemType() == L2EtcItemType.HERB;
	}
	
	public boolean isHeroItem()
	{
		return getItem().isHeroItem();
	}
	
	public boolean isQuestItem()
	{
		return getItem().isQuestItem();
	}
	
	public boolean isWeapon()
	{
		return getItem() instanceof L2Weapon;
	}
	
	public boolean isArmor()
	{
		return getItem() instanceof L2Armor;
	}
	
	public void setDropperObjectId(int id)
	{
		_dropperObjectId = id;
	}
	
	public long getTotalWeight()
	{
		try
		{
			return Math.multiplyExact(getItem().getWeight(), getCount());
		}
		catch (ArithmeticException ae)
		{
			return Long.MAX_VALUE;
		}
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (_dropperObjectId != 0)
			activeChar.sendPacket(new DropItem(this, _dropperObjectId));
		else if (isVisible())
			activeChar.sendPacket(new SpawnItem(this));
	}
	
	public boolean isEtcItem()
	{
		return (_item instanceof L2EtcItem);
	}
	
	public L2EtcItem getEtcItem()
	{
		if (_item instanceof L2EtcItem)
			return (L2EtcItem) _item;
		
		return null;
	}
	
	public List<Quest> getQuestEvents()
	{
		return _item.getQuestEvents();
	}
}
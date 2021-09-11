package com.l2jhellas.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.datatables.xml.PetData;
import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.handler.ItemHandler;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jhellas.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2PetData;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.PcInventory;
import com.l2jhellas.gameserver.model.PetInventory;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.stat.PetStat;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AbstractNpcInfo.SummonInfo;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.PetInfo;
import com.l2jhellas.gameserver.network.serverpackets.PetInventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PetItemList;
import com.l2jhellas.gameserver.network.serverpackets.PetStatusShow;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.StopMove;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.taskmanager.DecayTaskManager;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class L2PetInstance extends L2Summon
{
	protected static final Logger _logPet = Logger.getLogger(L2PetInstance.class.getName());
	
	// private byte _pvpFlag;
	private int _curFed;
	private final PetInventory _inventory;
	private final int _controlItemId;
	private boolean _respawned;
	private final boolean _mountable;
	
	private Future<?> _feedTask;
	private int _weapon;
	private int _armor;
	private int _jewel;
	private int _feedTime;
	protected boolean _feedMode;
	
	private L2PetData _data;
	
	private long _expBeforeDeath = 0;
	private static final int FOOD_ITEM_CONSUME_COUNT = 5;
	
	public final L2PetData getPetData()
	{
		if (_data == null)
			_data = PetData.getInstance().getPetData(getTemplate().npcId, getStat().getLevel());
		
		return _data;
	}
	
	public final void setPetData(L2PetData value)
	{
		_data = value;
	}
	
	class FeedTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				// if pet is attacking
				if (isAttackingNow())
					// if its not already on battleFeed mode
					if (!_feedMode)
						startFeed(true); // switching to battle feed
					else
					// if its on battleFeed mode
					if (_feedMode)
						startFeed(false); // normal feed
						
				if (getCurrentFed() > FOOD_ITEM_CONSUME_COUNT)
				{
					// eat
					setCurrentFed(getCurrentFed() - FOOD_ITEM_CONSUME_COUNT);
				}
				else
				{
					// go back to pet control item, or simply said, unsummon it
					setCurrentFed(0);
					stopFeed();
					unSummon(getOwner());
					getOwner().sendMessage("Your pet is too hungry to stay summoned.");
				}
				
				int foodId = PetData.getFoodItemId(getTemplate().npcId);
				if (foodId == 0)
					return;
				
				L2ItemInstance food = null;
				food = getInventory().getItemByItemId(foodId);
				
				if ((food != null) && (getCurrentFed() < (0.55 * getMaxFed())))
				{
					if (destroyItem("Feed", food.getObjectId(), 1, null, false))
					{
						setCurrentFed(getCurrentFed() + (100));
						if (getOwner() != null)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY);
							sm.addItemName(foodId);
							getOwner().sendPacket(sm);
						}
					}
				}
				
				broadcastStatusUpdate();
			}
			catch (Throwable e)
			{
				_logPet.log(Level.WARNING, getClass().getName() + ": Pet [#" + getObjectId() + "] a feed task error has occurred: ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
	}
	
	public synchronized static L2PetInstance spawnPet(L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
	{
		if (L2World.getInstance().getPet(owner.getObjectId()) != null)
			return null; // owner has a pet listed in world
			
		L2PetInstance pet = restore(control, template, owner);
		// add the pet instance to world
		if (pet != null)
		{
			pet.setTitle(owner.getName());
			L2World.getInstance().addPet(owner.getObjectId(), pet);
		}
		
		return pet;
	}
	
	public L2PetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
	{
		super(objectId, template, owner);
		super.setStat(new PetStat(this));
		
		_controlItemId = control.getObjectId();
		
		// Pet's initial level is supposed to be read from DB
		// Pets start at :
		// Wolf : Level 15
		// Hatcling : Level 35
		// Tested and confirmed on official servers
		// Sin-eaters are defaulted at the owner's level
		if (template.npcId == 12564)
			getStat().setLevel((byte) getOwner().getLevel());
		else
			getStat().setLevel(template.level);
		
		_inventory = new PetInventory(this);
		
		int npcId = template.npcId;
		_mountable = PetData.isMountable(npcId);
	}
	
	@Override
	public PetStat getStat()
	{
		if (super.getStat() == null || !(super.getStat() instanceof PetStat))
			setStat(new PetStat(this));
		return (PetStat) super.getStat();
	}
	
	@Override
	public double getLevelMod()
	{
		return (100.0 - 11 + getLevel()) / 100.0;
	}
	
	public boolean isRespawned()
	{
		return _respawned;
	}
	
	@Override
	public int getSummonType()
	{
		return 2;
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		boolean isOwner = player.getObjectId() == getOwner().getObjectId();
		boolean thisIsTarget = player.getTarget() != null && player.getTarget().getObjectId() == getObjectId();
		
		if (isOwner && thisIsTarget)
		{
			if (isOwner && player != getOwner())
			{
				// update owner
				updateRefOwner(player);
			}
			player.sendPacket(new PetStatusShow(this));
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			if (Config.DEBUG)
				_logPet.log(Level.FINE, getClass().getName() + ": new target selected:" + getObjectId());
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public int getControlItemId()
	{
		return _controlItemId;
	}
	
	public L2ItemInstance getControlItem()
	{
		return getOwner().getInventory().getItemByObjectId(_controlItemId);
	}
	
	public int getCurrentFed()
	{
		return _curFed;
	}
	
	public void setCurrentFed(int num)
	{
		_curFed = num > getMaxFed() ? getMaxFed() : num;
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		for (L2ItemInstance item : getInventory().getItems())
			if (item.getLocation() == ItemLocation.PET_EQUIP && item.getItem().getType1() == L2Item.TYPE2_WEAPON)
				return item;
		
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		
		if (weapon == null)
			return null;
		
		return (L2Weapon) weapon.getItem();
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// temporary? unavailable
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		// temporary? unavailable
		return null;
	}
	
	@Override
	public PetInventory getInventory()
	{
		return _inventory;
	}
	
	@Override
	public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.destroyItem(process, objectId, count, getOwner(), reference);
		
		if (item == null)
		{
			if (sendMessage)
				getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			
			return false;
		}
		
		// Send Pet inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		getOwner().sendPacket(petIU);
		
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addNumber(count);
			sm.addItemName(item.getItemId());
			getOwner().sendPacket(sm);
		}
		return true;
	}
	
	@Override
	public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.destroyItemByItemId(process, itemId, count, getOwner(), reference);
		
		if (item == null)
		{
			if (sendMessage)
				getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}
		
		// Send Pet inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		getOwner().sendPacket(petIU);
		
		if (sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addNumber(count);
			sm.addItemName(itemId);
			getOwner().sendPacket(sm);
		}
		
		return true;
	}
	
	public final void setWeapon(int id)
	{
		_weapon = id;
	}
	
	public final void setArmor(int id)
	{
		_armor = id;
	}
	
	public final void setJewel(int id)
	{
		_jewel = id;
	}
	
	@Override
	public final int getWeapon()
	{
		return _weapon;
	}
	
	@Override
	public final int getArmor()
	{
		return _armor;
	}
	
	public final int getJewel()
	{
		return _jewel;
	}
	
	@Override
	public void doPickupItem(L2Object object)
	{
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());
		
		if (Config.DEBUG)
			_logPet.log(Level.FINE, getClass().getName() + ": Pet pickup pos: " + object.getX() + " " + object.getY() + " " + object.getZ());
		
		broadcastPacket(sm);
		
		if (!(object instanceof L2ItemInstance))
		{
			// Don't try to pickup anything that is not an item :)
			_logPet.log(Level.WARNING, getClass().getName() + ": trying to pickup wrong target." + object);
			getOwner().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2ItemInstance target = (L2ItemInstance) object;
		
		// Herbs
		if (target.getItemId() > 8599 && target.getItemId() < 8615)
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
			smsg.addItemName(target.getItemId());
			getOwner().sendPacket(smsg);
			return;
		}
		// Cursed weapons
		if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
			smsg.addItemName(target.getItemId());
			getOwner().sendPacket(smsg);
			return;
		}
		
		synchronized (target)
		{
			if (!target.isVisible())
			{
				getOwner().sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (target.getOwnerId() != 0 && target.getOwnerId() != getOwner().getObjectId() && !getOwner().isInLooterParty(target.getOwnerId()))
			{
				getOwner().sendPacket(ActionFailed.STATIC_PACKET);
				
				if (target.getItemId() == 57)
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
					smsg.addNumber(target.getCount());
					getOwner().sendPacket(smsg);
				}
				else if (target.getCount() > 1)
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
					smsg.addItemName(target.getItemId());
					smsg.addNumber(target.getCount());
					getOwner().sendPacket(smsg);
				}
				else
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
					smsg.addItemName(target.getItemId());
					getOwner().sendPacket(smsg);
				}
				
				return;
			}
			if (target.getItemLootShedule() != null && (target.getOwnerId() == getOwner().getObjectId() || getOwner().isInLooterParty(target.getOwnerId())))
				target.resetOwnerTimer();
			
			target.pickupMe(this);
			
			if (Config.SAVE_DROPPED_ITEM) // item must be removed from ItemsOnGroundManager if is active
				ItemsOnGroundManager.getInstance().removeObject(target);
		}
		
		getInventory().addItem("Pickup", target, getOwner(), this);
		getOwner().sendPacket(new PetItemList(this));
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		if (getFollowStatus())
			followOwner();
	}
	
	@Override
	public void deleteMe(L2PcInstance owner)
	{
		super.deleteMe(owner);
		destroyControlItem(owner); // this should also delete the pet from the db
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer, true))
			return false;
		DecayTaskManager.getInstance().addDecayTask(this, 1200000);
		deathPenalty();
		return true;
	}
	
	@Override
	public void doRevive()
	{
		if (_curFed > (getMaxFed() / 10))
			_curFed = getMaxFed() / 10;
		
		getOwner().removeReviving();
		
		super.doRevive();
		
		// stopDecay
		DecayTaskManager.getInstance().cancelDecayTask(this);
		startFeed(false);
	}
	
	@Override
	public void doRevive(double revivePower)
	{
		// Restore the pet's lost experience,
		// depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}
	
	public L2ItemInstance transferItem(String process, int objectId, int count, Inventory target, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance oldItem = getInventory().getItemByObjectId(objectId);
		L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, actor, reference);
		
		if (newItem == null)
			return null;
		
		// Send inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		if (oldItem.getCount() > 0 && oldItem != newItem)
			petIU.addModifiedItem(oldItem);
		else
			petIU.addRemovedItem(oldItem);
		getOwner().sendPacket(petIU);
		
		// Send target update packet
		if (target instanceof PcInventory)
		{
			L2PcInstance targetPlayer = ((PcInventory) target).getOwner();
			InventoryUpdate playerUI = new InventoryUpdate();
			if (newItem.getCount() > count)
				playerUI.addModifiedItem(newItem);
			else
				playerUI.addNewItem(newItem);
			targetPlayer.sendPacket(playerUI);
			
			// Update current load as well
			StatusUpdate playerSU = new StatusUpdate(targetPlayer.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		}
		else if (target instanceof PetInventory)
		{
			petIU = new PetInventoryUpdate();
			if (newItem.getCount() > count)
				petIU.addRemovedItem(newItem);
			else
				petIU.addNewItem(newItem);
			((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
		}
		return newItem;
	}
	
	@Override
	public void giveAllToOwner()
	{
		try
		{
			Inventory petInventory = getInventory();
			Collection<L2ItemInstance> items = petInventory.getItems();
			for (L2ItemInstance giveit : items)
			{
				if (((giveit.getItem().getWeight() * giveit.getCount()) + getOwner().getInventory().getTotalWeight()) < getOwner().getMaxLoad())
				{
					// If the owner can carry it give it to them
					giveItemToOwner(giveit);
				}
				else
				{
					// If they can't carry it, chuck it on the floor :)
					dropItemHere(giveit);
				}
			}
		}
		catch (Exception e)
		{
			_logPet.log(Level.WARNING, getClass().getName() + ": Give all items error ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void giveItemToOwner(L2ItemInstance item)
	{
		try
		{
			getInventory().transferItem("PetTransfer", item.getObjectId(), item.getCount(), getOwner().getInventory(), getOwner(), this);
			PetInventoryUpdate petiu = new PetInventoryUpdate();
			ItemList PlayerUI = new ItemList(getOwner(), false);
			petiu.addRemovedItem(item);
			getOwner().sendPacket(petiu);
			getOwner().sendPacket(PlayerUI);
		}
		catch (Exception e)
		{
			_logPet.log(Level.WARNING, getClass().getName() + ": Error while giving item to owner: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void destroyControlItem(L2PcInstance owner)
	{
		// remove the pet instance from world
		L2World.getInstance().removePet(owner.getObjectId());
		
		// delete from inventory
		try
		{
			L2ItemInstance removedItem = owner.getInventory().destroyItem("PetDestroy", getControlItemId(), 1, getOwner(), this);
			
			InventoryUpdate iu = new InventoryUpdate();
			iu.addRemovedItem(removedItem);
			
			owner.sendPacket(iu);
			
			StatusUpdate su = new StatusUpdate(owner.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, owner.getCurrentLoad());
			owner.sendPacket(su);
			
			owner.broadcastUserInfo();
			
			L2World.getInstance().removeObject(removedItem);
		}
		catch (Exception e)
		{
			_logPet.log(Level.WARNING, getClass().getName() + ": Error while destroying control item: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		// pet control item no longer exists, delete the pet from the db
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			statement.setInt(1, getControlItemId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_logPet.log(Level.WARNING, getClass().getName() + ": could not delete pet:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void dropAllItems()
	{
		try
		{
			for (L2ItemInstance items : getInventory().getItems())
			{
				dropItemHere(items);
			}
		}
		catch (Exception e)
		{
			_logPet.log(Level.WARNING, getClass().getName() + ": Pet Drop Error: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void dropItemHere(L2ItemInstance dropit)
	{
		dropit = getInventory().dropItem("Drop", dropit.getObjectId(), dropit.getCount(), getOwner(), this);
		
		if (dropit != null)
		{
			_logPet.log(Level.FINER, getClass().getName() + ": Item id to drop: " + dropit.getItemId() + " amount: " + dropit.getCount());
			dropit.dropMe(this, getX(), getY(), getZ() + 100);
		}
	}
	
	// public void startAttack(L2Character target)
	// {
	// if (!knownsObject(target))
	// {
	// target.addKnownObject(this);
	// this.addKnownObject(target);
	// }
	// if (!target.knownsObject(this))
	// {
	// target.addKnownObject(this);
	// this.addKnownObject(target);
	// }
	//
	// if (!isRunning())
	// {
	// setRunning(true);
	// ChangeMoveType move = new ChangeMoveType(this, ChangeMoveType.RUN);
	// broadcastPacket(move);
	// }
	//
	// super.startAttack(target);
	// }
	//
	
	@Override
	public boolean isMountable()
	{
		return _mountable;
	}
	
	private static L2PetInstance restore(L2ItemInstance control, L2NpcTemplate template, L2PcInstance owner)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			L2PetInstance pet;
			if (template.type.compareToIgnoreCase("L2BabyPet") == 0)
				pet = new L2BabyPetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
			else
				pet = new L2PetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
			
			PreparedStatement statement = con.prepareStatement("SELECT item_obj_id, name, level, curHp, curMp, exp, sp, karma, pkkills, fed FROM pets WHERE item_obj_id=?");
			statement.setInt(1, control.getObjectId());
			ResultSet rset = statement.executeQuery();
			if (!rset.next())
			{
				rset.close();
				statement.close();
				return pet;
			}
			
			pet._respawned = true;
			pet.setName(rset.getString("name"));
			
			pet.getStat().setLevel(rset.getByte("level"));
			pet.getStat().setExp(rset.getLong("exp"));
			pet.getStat().setSp(rset.getInt("sp"));
			
			pet.getStatus().setCurrentHp(rset.getDouble("curHp"));
			pet.getStatus().setCurrentMp(rset.getDouble("curMp"));
			pet.getStatus().setCurrentCp(pet.getMaxCp());

			if (rset.getDouble("curHp") < 0.5)
			{
				pet.doDie(pet);
				pet.getStatus().stopHpMpRegeneration();
			}
			
			pet.setCurrentFed(rset.getInt("fed"));
			
			rset.close();
			statement.close();
			return pet;
		}
		catch (Exception e)
		{
			_logPet.log(Level.WARNING, ": could not restore pet data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public void store()
	{
		if (getControlItemId() == 0)
		{
			// this is a summon, not a pet, don't store anything
			return;
		}
		
		String req;
		if (!isRespawned())
			req = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,karma,fed,item_obj_id) VALUES (?,?,?,?,?,?,?,?,?)";
		else
			req = "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,karma=?,fed=? WHERE item_obj_id=?";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(req);
			statement.setString(1, getName());
			statement.setInt(2, getStat().getLevel());
			statement.setDouble(3, getStatus().getCurrentHp());
			statement.setDouble(4, getStatus().getCurrentMp());
			statement.setLong(5, getStat().getExp());
			statement.setInt(6, getStat().getSp());
			statement.setInt(7, getKarma());
			statement.setInt(8, getCurrentFed());
			statement.setInt(9, getControlItemId());
			statement.executeUpdate();
			statement.close();
			_respawned = true;
		}
		catch (Exception e)
		{
			_logPet.log(Level.WARNING, getClass().getName() + ": could not store pet data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		L2ItemInstance itemInst = getControlItem();
		if (itemInst != null && itemInst.getEnchantLevel() != getStat().getLevel())
		{
			itemInst.setEnchantLevel(getStat().getLevel());
			itemInst.updateDatabase();
		}
	}
	
	public synchronized void stopFeed()
	{
		if (_feedTask != null)
		{
			_feedTask.cancel(false);
			_feedTask = null;
			if (Config.DEBUG)
				_logPet.log(Level.FINE, getClass().getName() + ": Pet [#" + getObjectId() + "] feed task stop");
		}
	}
	
	public synchronized void startFeed(boolean battleFeed)
	{
		// stop feeding task if its active
		
		stopFeed();
		if (!isDead())
		{
			if (battleFeed)
			{
				_feedMode = true;
				_feedTime = _data.getPetFeedBattle();
			}
			else
			{
				_feedMode = false;
				_feedTime = _data.getPetFeedNormal();
			}
			// pet feed time must be different than 0. Changing time to bypass divide by 0
			if (_feedTime <= 0)
			{
				_feedTime = 1;
			}
			
			_feedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 60000 / _feedTime, 60000 / _feedTime);
		}
	}
	
	@Override
	public synchronized void unSummon(L2PcInstance owner)
	{
		stopFeed();
		stopHpMpRegeneration();
		super.unSummon(owner);
		
		if (!isDead())
			L2World.getInstance().removePet(owner.getObjectId());
	}
	
	public void restoreExp(double restorePercent)
	{
		if (_expBeforeDeath > 0)
		{
			// Restore the specified % of lost experience.
			getStat().addExp(Math.round((_expBeforeDeath - getStat().getExp()) * restorePercent / 100));
			_expBeforeDeath = 0;
		}
	}
	
	private void deathPenalty()
	{
		int lvl = getStat().getLevel();
		double percentLost = -0.07 * lvl + 6.5;
		
		// Calculate the Experience loss
		long lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);
		
		// Get the Experience before applying penalty
		_expBeforeDeath = getStat().getExp();
		
		// Set the new Experience value of the L2PetInstance
		getStat().addExp(-lostExp);
	}
	
	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		if (getNpcId() == 12564) // SinEater
			getStat().addExpAndSp(Math.round(addToExp * Config.SINEATER_XP_RATE), addToSp);
		else
			getStat().addExpAndSp(Math.round(addToExp * Config.PET_XP_RATE), addToSp);
	}
	
	@Override
	public long getExpForThisLevel()
	{
		return getStat().getExpForLevel(getLevel());
	}
	
	@Override
	public long getExpForNextLevel()
	{
		return getStat().getExpForLevel(getLevel() + 1);
	}
	
	@Override
	public final int getLevel()
	{
		return getStat().getLevel();
	}
	
	public int getMaxFed()
	{
		return getStat().getMaxFeed();
	}
	
	@Override
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}
	
	@Override
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}
	
	@Override
	public int getEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}
	
	@Override
	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}
	
	@Override
	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}
	
	@Override
	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}
	
	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}
	
	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}
	
	@Override
	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}
	
	@Override
	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}
	
	@Override
	public final int getSkillLevel(int skillId)
	{
		if ((_skills == null) || (_skills.get(skillId) == null))
			return -1;
		int lvl = getLevel();
		return lvl > 70 ? 7 + (lvl - 70) / 5 : lvl / 10;
	}
	
	public void updateRefOwner(L2PcInstance owner)
	{
		int oldOwnerId = getOwner().getObjectId();
		
		setOwner(owner);
		L2World.getInstance().removePet(oldOwnerId);
		L2World.getInstance().addPet(oldOwnerId, this);
	}
	
	public boolean canWear(L2Item item)
	{
		if (PetData.isHatchling(getNpcId()) && item.getBodyPart() == L2Item.SLOT_HATCHLING)
			return true;
		
		if (PetData.isWolf(getNpcId()) && item.getBodyPart() == L2Item.SLOT_WOLF)
			return true;
		
		if (PetData.isStrider(getNpcId()) && item.getBodyPart() == L2Item.SLOT_STRIDER)
			return true;
		
		if (PetData.isBaby(getNpcId()) && item.getBodyPart() == L2Item.SLOT_BABYPET)
			return true;
		
		return false;
	}
	
	@Override
	public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss)
			return;
		
		// Prevents the double spam of system messages, if the target is the owning player.
		if (target.getObjectId() != getOwner().getObjectId())
		{
			if (pcrit || mcrit)
				getOwner().sendPacket(SystemMessageId.CRITICAL_HIT_BY_PET);
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_HIT_FOR_S1_DAMAGE);
			sm.addNumber(damage);
			getOwner().sendPacket(sm);
		}
	}
	
	public int getInventoryLimit()
	{
		return 12;
	}
	
	@Override
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		super.onHitTimer(target, damage, crit, miss, soulshot, shld);
		rechargeShots(true,false);
	}
	
	@Override
	public void rechargeShots(boolean physical, boolean magic)
	{
		if (getOwner().getAutoSoulShot() == null || getOwner().getAutoSoulShot().isEmpty())
			return;
		
		for (int itemId : getOwner().getAutoSoulShot())
		{
			L2ItemInstance item = getOwner().getInventory().getItemByItemId(itemId);
			if (item != null)
			{
				if (magic && itemId == 6646 || itemId == 6647)
				{
					final IItemHandler handler = ItemHandler.getInstance().getHandler(itemId);
					
					if (handler != null)
						handler.useItem(getOwner(), item);
				}
				
				if (physical && itemId == 6645)
				{
					final IItemHandler handler = ItemHandler.getInstance().getHandler(itemId);
					
					if (handler != null)
						handler.useItem(this, item);
				}
			}
			else
				getOwner().removeAutoSoulShot(itemId);
		}
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		L2Summon summon = this;
		
		// Check if the L2PcInstance is the owner of the Pet
		if (activeChar.equals(summon.getOwner()))
		{
			activeChar.sendPacket(new PetInfo(summon, 0));
			// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
			summon.updateEffectIcons(true);
			
			activeChar.sendPacket(new PetItemList((L2PetInstance) summon));
		}
		else
		    activeChar.sendPacket(new SummonInfo(this, activeChar, 0));
		
	}
	
}
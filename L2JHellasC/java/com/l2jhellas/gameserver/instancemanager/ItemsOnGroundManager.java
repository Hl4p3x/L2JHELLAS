package com.l2jhellas.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ItemsAutoDestroy;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class ItemsOnGroundManager
{
	static final Logger _log = Logger.getLogger(ItemsOnGroundManager.class.getName());
	private static ItemsOnGroundManager _instance;
	protected List<L2ItemInstance> _items = null;
	
	private ItemsOnGroundManager()
	{
		if (!Config.SAVE_DROPPED_ITEM)
			return;
		_items = new ArrayList<>();
		if (Config.SAVE_DROPPED_ITEM_INTERVAL > 0)
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new storeInDb(), Config.SAVE_DROPPED_ITEM_INTERVAL, Config.SAVE_DROPPED_ITEM_INTERVAL);
	}
	
	public static final ItemsOnGroundManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ItemsOnGroundManager();			
			_instance.load();
		}
		return _instance;
	}
	
	public void reload()
	{
		_instance.load();
	}
	
	private void load()
	{
		// If SaveDroppedItem is false, may want to delete all items previously stored to avoid add old items on reactivate
		if (!Config.SAVE_DROPPED_ITEM && Config.CLEAR_DROPPED_ITEM_TABLE)
			emptyTable();
		
		if (!Config.SAVE_DROPPED_ITEM)
			return;
		
		// if DestroyPlayerDroppedItem was previously false, items curently protected will be added to ItemsAutoDestroy
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				String str = null;
				if (!Config.DESTROY_EQUIPABLE_PLAYER_ITEM) // Recycle Misc
					str = "UPDATE itemsonground SET drop_time=? WHERE drop_time=-1 AND equipable=0";
				else if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM) // Recycle All
					str = "UPDATE itemsonground SET drop_time=? WHERE drop_time=-1";
				
				PreparedStatement statement = con.prepareStatement(str);
				statement.setLong(1, System.currentTimeMillis());
				statement.execute();
				statement.close();
			}
			catch (SQLException e)
			{
				_log.warning(ItemsOnGroundManager.class.getName() + ": error while updating table ItemsOnGround ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
		
		// Add items to world
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Statement s = con.createStatement();
			ResultSet result;
			int count = 0;
			result = s.executeQuery("SELECT object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable FROM itemsonground");
			while (result.next())
			{
				L2ItemInstance item = new L2ItemInstance(result.getInt(1), result.getInt(2));
				L2World.getInstance().storeObject(item);
				if (item.isStackable() && result.getInt(3) > 1) // this check and..
					item.setCount(result.getInt(3));
				if (result.getInt(4) > 0) // this, are really necessary?
					item.setEnchantLevel(result.getInt(4));
				item.getPosition().setWorldPosition(result.getInt(5), result.getInt(6), result.getInt(7));
				item.getPosition().setWorldRegion(L2World.getInstance().getRegion(item.getPosition().getWorldPosition()));
				item.getWorldRegion().addVisibleObject(item);
				item.setDropTime(result.getLong(8));
				if (result.getLong(8) == -1)
					item.setProtected(true);
				else
					item.setProtected(false);
				item.setIsVisible(true);
				L2World.getInstance().addVisibleObject(item, item.getWorldRegion());
				_items.add(item);
				count++;
				// add to ItemsAutoDestroy only items not protected
				if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
				{
					if (result.getLong(8) > -1)
					{
						if (Config.AUTODESTROY_ITEM_AFTER > 0)
							ItemsAutoDestroy.getInstance().addItem(item);
					}
				}
			}
			result.close();
			s.close();
			if (count > 0)
				_log.info(ItemsOnGroundManager.class.getSimpleName() + ": restored " + count + " items.");
			else
				_log.info(ItemsOnGroundManager.class.getSimpleName() + ": Initializing ItemsOnGroundManager.");
		}
		catch (SQLException e)
		{
			_log.warning(ItemsOnGroundManager.class.getName() + ": error while loading ItemsOnGround ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		if (Config.EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD)
			emptyTable();
	}
	
	public void save(L2ItemInstance item)
	{
		if (Config.SAVE_DROPPED_ITEM)
		    _items.add(item);
	}
	
	public void removeObject(L2Object item)
	{
		if (Config.SAVE_DROPPED_ITEM)
		   _items.remove(item);
	}
	
	public void saveInDb()
	{
		new storeInDb().run();
	}
	
	public void cleanUp()
	{
		_items.clear();
	}
	
	public void emptyTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement del = con.prepareStatement("DELETE FROM itemsonground");
			del.execute();
			del.close();
		}
		catch (SQLException e)
		{
			_log.warning(ItemsOnGroundManager.class.getName() + ": error while cleaning table ItemsOnGround ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	protected class storeInDb extends Thread
	{
		@Override
		public void run()
		{
			if (!Config.SAVE_DROPPED_ITEM)
				return;
			
			emptyTable();
			
			if (_items.isEmpty())
				return;
			
			for (L2ItemInstance item : _items)
			{
				if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
					continue; // Cursed Items not saved to ground, prevent double save
					
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("INSERT INTO itemsonground(object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable) VALUES (?,?,?,?,?,?,?,?,?)");
					statement.setInt(1, item.getObjectId());
					statement.setInt(2, item.getItemId());
					statement.setInt(3, item.getCount());
					statement.setInt(4, item.getEnchantLevel());
					statement.setInt(5, item.getX());
					statement.setInt(6, item.getY());
					statement.setInt(7, item.getZ());
					
					if (item.isProtected())
						statement.setLong(8, -1); // item will be protected
					else
						statement.setLong(8, item.getDropTime()); // item will be added to ItemsAutoDestroy
					if (item.isEquipable())
						statement.setLong(9, 1); // set equipable
					else
						statement.setLong(9, 0);
					statement.execute();
					statement.close();
				}
				catch (SQLException e)
				{
					_log.warning(ItemsOnGroundManager.class.getName() + ": error while inserting into table ItemsOnGround ");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
			}
			if (Config.DEBUG)
				_log.config(ItemsOnGroundManager.class.getName() + ": " + _items.size() + " items on ground saved.");
		}
	}
}
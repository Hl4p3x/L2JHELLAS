package com.l2jhellas.gameserver.instancemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.CursedWeapon;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2RiftInvaderInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Broadcast;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class CursedWeaponsManager
{
	private static final Logger _log = Logger.getLogger(CursedWeaponsManager.class.getName());

	private Map<Integer, CursedWeapon> _cursedWeapons;
	
	public CursedWeaponsManager()
	{
		_cursedWeapons = new HashMap<>();
		
		if (!Config.ALLOW_CURSED_WEAPONS)
			return;
		
		load();
		restore();
		controlPlayers();
		_log.info("CursedWeaponsManager: Loaded " + _cursedWeapons.size() + " cursed weapons.");
	}
	
	public final static void reload()
	{
		getInstance();
	}
	
	private final void load()
	{
		_cursedWeapons.clear();
		
		if (Config.DEBUG)
			_log.config(CursedWeaponsManager.class.getName() + ": Parsing..");
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			
			File file = new File(PackRoot.DATAPACK_ROOT, "data/xml/cursedWeapons.xml");
			if (!file.exists())
			{
				_log.config(CursedWeaponsManager.class.getName() + ": NO FILE cursedWeapons.xml");
				return;
			}
			
			Document doc = factory.newDocumentBuilder().parse(file);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
							String name = attrs.getNamedItem("name").getNodeValue();
							
							CursedWeapon cw = new CursedWeapon(id, skillId, name);
							
							int val;
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("dropRate".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDropRate(val);
								}
								else if ("duration".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDuration(val);
								}
								else if ("durationLost".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDurationLost(val);
								}
								else if ("disapearChance".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setDisapearChance(val);
								}
								else if ("stageKills".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
									cw.setStageKills(val);
								}
							}
							
							// Store cursed weapon
							_cursedWeapons.put(id, cw);
						}
					}
				}
			}
			
			if (Config.DEBUG)
				_log.config(CursedWeaponsManager.class.getName() + ": OK");
		}
		catch (Exception e)
		{
			_log.warning(CursedWeaponsManager.class.getName() + ": Error parsing cursed weapons file.");
			if (Config.DEVELOPER)
				e.printStackTrace();
			return;
		}
	}
	
	private final void restore()
	{
		if (Config.DEBUG)
			_log.config(CursedWeaponsManager.class.getName() + ": Restoring ... ");
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Retrieve the L2PcInstance from the characters table of the database
			PreparedStatement statement = con.prepareStatement("SELECT itemId, playerId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons");
			ResultSet rset = statement.executeQuery();
			
			if (rset.next())
			{
				int itemId = rset.getInt("itemId");
				int playerId = rset.getInt("playerId");
				int playerKarma = rset.getInt("playerKarma");
				int playerPkKills = rset.getInt("playerPkKills");
				int nbKills = rset.getInt("nbKills");
				long endTime = rset.getLong("endTime");
				
				CursedWeapon cw = _cursedWeapons.get(itemId);
				cw.setPlayerId(playerId);
				cw.setPlayerKarma(playerKarma);
				cw.setPlayerPkKills(playerPkKills);
				cw.setNbKills(nbKills);
				cw.setEndTime(endTime);
				cw.reActivate();
			}
			
			rset.close();
			statement.close();
			
			if (Config.DEBUG)
				_log.config(CursedWeaponsManager.class.getName() + ": OK");
		}
		catch (Exception e)
		{
			_log.warning(CursedWeaponsManager.class.getName() + ": Could not restore CursedWeapons data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
			return;
		}
	}
	
	private final void controlPlayers()
	{
		if (Config.DEBUG)
			_log.config(CursedWeaponsManager.class.getName() + ": Checking players ... ");
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			for (CursedWeapon cw : _cursedWeapons.values())
			{
				if (cw.isActivated())
					continue;
				
				// Do an item check to be sure that the cursed weapon isn't hold by someone
				int itemId = cw.getItemId();
				try
				{
					PreparedStatement statement = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
					statement.setInt(1, itemId);
					ResultSet rset = statement.executeQuery();
					
					if (rset.next())
					{
						// A player has the cursed weapon in his inventory ...
						int playerId = rset.getInt("owner_id");
						_log.warning(CursedWeaponsManager.class.getSimpleName() + ": Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");
						
						// Delete the item
						PreparedStatement statement1 = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
						statement1.setInt(1, playerId);
						statement1.setInt(2, itemId);
						if (statement1.executeUpdate() != 1)
						{
							_log.warning(CursedWeaponsManager.class.getSimpleName() + ": Error while deleting cursed weapon " + itemId + " from userId " + playerId);
						}
						statement1.close();
						// Restore the player's old karma and pk count
						PreparedStatement statement2 = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_Id=?");
						statement2.setInt(1, cw.getPlayerKarma());
						statement2.setInt(2, cw.getPlayerPkKills());
						statement2.setInt(3, playerId);
						if (statement2.executeUpdate() != 1)
						{
							_log.warning(CursedWeaponsManager.class.getSimpleName() + ": Error while updating karma & pkkills for userId " + cw.getPlayerId());
						}
						statement2.close();
						// clean up the cursedweapons table.
						removeFromDb(itemId);
					}
					rset.close();
					statement.close();
				}
				catch (SQLException e)
				{
					_log.warning(CursedWeaponsManager.class.getName() + ": ");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			_log.warning(CursedWeaponsManager.class.getName() + ": Could not check CursedWeapons data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
			return;
		}
		
		if (Config.DEBUG)
			_log.config(CursedWeaponsManager.class.getName() + ": DONE");
	}
	
	public synchronized void checkDrop(L2Attackable attackable, L2PcInstance player)
	{
		if (attackable instanceof L2SiegeGuardInstance || attackable instanceof L2RiftInvaderInstance || attackable instanceof L2GrandBossInstance || attackable instanceof L2FestivalMonsterInstance)
			return;
		
		if (player.isCursedWeaponEquiped())
			return;
		
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActive())
				continue;
			
			if (cw.checkDrop(attackable, player))
				break;
		}
	}
	
	public void activate(L2PcInstance player, L2ItemInstance item)
	{
		CursedWeapon cw = _cursedWeapons.get(item.getItemId());
		if (player.isCursedWeaponEquiped()) // cannot own 2 cursed swords
		{
			CursedWeapon cw2 = _cursedWeapons.get(player.getCursedWeaponEquipedId());
			
			cw2.setNbKills(cw2.getStageKills() - 1);
			cw2.increaseKills();
			
			// erase the newly obtained cursed weapon
			cw.setPlayer(player); // NECESSARY in order to find which inventory the weapon is in!
			cw.endOfLife(); // expire the weapon and clean up.
		}
		else
			cw.activate(player, item);
	}
	
	public void drop(int itemId, L2Character killer)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);
		
		cw.dropIt(killer);
	}
	
	public void increaseKills(int itemId)
	{
		CursedWeapon cw = _cursedWeapons.get(itemId);
		
		cw.increaseKills();
	}
	
	public int getLevel(int itemId)
	{
		final CursedWeapon cw = _cursedWeapons.get(itemId);
		return (cw == null) ? 0 : cw.getLevel();
	}
	
	public static void announce(SystemMessage sm)
	{
		Broadcast.toAllOnlinePlayers(sm);
	}
	
	public void checkPlayer(L2PcInstance player)
	{
		if (player == null)
			return;
		
		for (CursedWeapon cw : _cursedWeapons.values())
		{
			if (cw.isActivated() && player.getObjectId() == cw.getPlayerId())
			{
				cw.setPlayer(player);
				cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
				cw.giveSkill();
				player.setCursedWeaponEquipedId(cw.getItemId());
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
				sm.addString(cw.getName());
				// sm.addItemName(cw.getItemId());
				sm.addNumber((int) ((cw.getEndTime() - System.currentTimeMillis()) / 60000));
				player.sendPacket(sm);
			}
		}
	}
	
	public static void removeFromDb(int itemId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Delete datas
			PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
			statement.setInt(1, itemId);
			statement.executeUpdate();
			
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(CursedWeaponsManager.class.getSimpleName() + ": CursedWeaponsManager: Failed to remove data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void saveData()
	{
		for (CursedWeapon cw : _cursedWeapons.values())
			cw.saveData();
	}
	
	public boolean isCursed(int itemId)
	{
		return _cursedWeapons.containsKey(itemId);
	}
	
	public Collection<CursedWeapon> getCursedWeapons()
	{
		return _cursedWeapons.values();
	}
	
	public Set<Integer> getCursedWeaponsIds()
	{
		return _cursedWeapons.keySet();
	}
	
	public CursedWeapon getCursedWeapon(int itemId)
	{
		return _cursedWeapons.get(itemId);
	}
	
	public void givePassive(int itemId)
	{
		try
		{
			_cursedWeapons.get(itemId).giveSkill();
		}
		catch (Exception e)
		{
		}
	}
	
	public static CursedWeaponsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CursedWeaponsManager _instance = new CursedWeaponsManager();
	}
}
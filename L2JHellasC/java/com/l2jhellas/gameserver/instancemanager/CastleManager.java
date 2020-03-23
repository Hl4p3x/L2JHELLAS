package com.l2jhellas.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.SevenSigns;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class CastleManager
{
	protected static final Logger _log = Logger.getLogger(CastleManager.class.getName());
	
	private static CastleManager _instance;
	
	public static final CastleManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new CastleManager();
			_instance.load();
		}
		return _instance;
	}
	
	private List<Castle> _castles;
	
	private static final int _castleCirclets[] =
	{
		0,
		6838,
		6835,
		6839,
		6837,
		6840,
		6834,
		6836,
		8182,
		8183
	};
	
	public CastleManager()
	{
	}
	
	public final int findNearestCastleIndex(L2Object obj)
	{
		int index = getCastleIndex(obj);
		if (index < 0)
		{
			double closestDistance = 99999999;
			double distance;
			Castle castle;
			for (int i = 0; i < getCastles().size(); i++)
			{
				castle = getCastles().get(i);
				if (castle == null)
					continue;
				distance = castle.getDistance(obj);
				if (closestDistance > distance)
				{
					closestDistance = distance;
					index = i;
				}
			}
		}
		return index;
	}
	
	public void reload()
	{
		_castles.clear();
		load();
	}
	
	private final void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id FROM castle ORDER BY id");
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				getCastles().add(new Castle(rs.getInt("id")));
			}
			rs.close();
			statement.close();
			
			_log.info(CastleManager.class.getSimpleName() + ": Loaded: " + getCastles().size() + " castles.");
		}
		catch (Exception e)
		{
			_log.warning(" loadCastleData(): "+e);
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public final Castle getCastleById(int castleId)
	{
		return getCastles().stream().filter(c -> c.getCastleId() == castleId).findFirst().orElse(null);
	}
	
	public final Castle getCastleByOwner(L2Clan clan)
	{
		return getCastles().stream().filter(c -> c.getOwnerId() == clan.getClanId()).findFirst().orElse(null);
	}
	
	public final Castle getCastle(String name)
	{
		return getCastles().stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
	
	public final Castle getCastle(int x, int y, int z)
	{
		return getCastles().stream().filter(c -> c.checkIfInZone(x, y, z)).findFirst().orElse(null);
	}
	
	public final Castle getCastle(L2Object activeObject)
	{
		return getCastle(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final int getCastleIndex(int castleId)
	{
		Castle castle;
		for (int i = 0; i < getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.getCastleId() == castleId)
				return i;
		}
		return -1;
	}
	
	public final int getCastleIndex(L2Object activeObject)
	{
		return getCastleIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final int getCastleIndex(int x, int y, int z)
	{
		Castle castle;
		for (int i = 0; i < getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.checkIfInZone(x, y, z))
				return i;
		}
		return -1;
	}
	
	public final List<Castle> getCastles()
	{
		if (_castles == null)
			_castles = new ArrayList<>();
		return _castles;
	}
	
	public final void validateTaxes(int sealStrifeOwner)
	{
		int maxTax;
		switch (sealStrifeOwner)
		{
			case SevenSigns.CABAL_DUSK:
				maxTax = 5;
				break;
			case SevenSigns.CABAL_DAWN:
				maxTax = 25;
				break;
			default: // no owner
				maxTax = 15;
				break;
		}
		getCastles().stream().filter(c -> c.getTaxPercent() > maxTax).forEach(c -> c.setTaxPercent(maxTax));
	}
	
	int _castleId = 1; // from this castle
	
	public int getCirclet()
	{
		return getCircletByCastleId(_castleId);
	}
	
	public int getCircletByCastleId(int castleId)
	{
		if (castleId > 0 && castleId < 10)
			return _castleCirclets[castleId];
		
		return 0;
	}
	
	// remove this castle's circlets from the clan
	public void removeCirclet(L2Clan clan, int castleId)
	{
		for (L2ClanMember member : clan.getMembers())
			removeCirclet(member, castleId);
	}
	
	public void removeCirclet(L2ClanMember member, int castleId)
	{
		if (member == null)
			return;
		L2PcInstance player = member.getPlayerInstance();
		int circletId = getCircletByCastleId(castleId);
		
		if (circletId != 0)
		{
			// online-player circlet removal
			if (player != null)
			{
				try
				{
					L2ItemInstance circlet = player.getInventory().getItemByItemId(circletId);
					if (circlet != null)
					{
						if (circlet.isEquipped())
							player.getInventory().unEquipItemInSlotAndRecord(circlet.getEquipSlot());
						player.destroyItemByItemId("CastleCircletRemoval", circletId, 1, player, true);
					}
					return;
				}
				catch (NullPointerException e)
				{
					// continue removing offline
				}
			}
			// else offline-player circlet removal
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
				statement.setInt(1, member.getObjectId());
				statement.setInt(2, circletId);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warning(CastleManager.class.getName() + ": Failed to remove castle circlets offline for player " + member.getName());
				if (Config.DEVELOPER)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
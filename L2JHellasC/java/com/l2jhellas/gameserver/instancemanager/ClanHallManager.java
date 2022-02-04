package com.l2jhellas.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.entity.ClanHall;
import com.l2jhellas.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jhellas.gameserver.scrips.siegable.SiegableHall;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class ClanHallManager
{
	protected static final Logger _log = Logger.getLogger(ClanHallManager.class.getName());
	
	private static ClanHallManager _instance;
	
	private final Map<Integer, ClanHall> _clanHall;
	private final Map<Integer, ClanHall> _freeClanHall;
	private boolean _loaded = false;
	
	public static ClanHallManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ClanHallManager();
		}
		return _instance;
	}
	
	public boolean loaded()
	{
		return _loaded;
	}
	
	public final void reload()
	{
		_clanHall.clear();
		_freeClanHall.clear();
		load();
	}
	
	private ClanHallManager()
	{
		_clanHall = new HashMap<>();
		_freeClanHall = new HashMap<>();
		load();
	}
	
	private final void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id"))
		{
			try(ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					int id = rs.getInt("id");
					if (rs.getInt("ownerId") == 0)
						_freeClanHall.put(id, new ClanHall(id, rs.getString("name"), rs.getInt("ownerId"), rs.getInt("lease"), rs.getString("desc"), rs.getString("location"), 0, rs.getInt("Grade"), rs.getBoolean("paid")));
					else
					{
						if (ClanTable.getInstance().getClan(rs.getInt("ownerId")) != null)
						{
							_clanHall.put(id, new ClanHall(id, rs.getString("name"), rs.getInt("ownerId"), rs.getInt("lease"), rs.getString("desc"), rs.getString("location"), rs.getLong("paidUntil"), rs.getInt("Grade"), rs.getBoolean("paid")));
							ClanTable.getInstance().getClan(rs.getInt("ownerId")).setHasHideout(id);
						}
						else
						{
							_freeClanHall.put(id, new ClanHall(id, rs.getString("name"), rs.getInt("ownerId"), rs.getInt("lease"), rs.getString("desc"), rs.getString("location"), rs.getLong("paidUntil"), rs.getInt("Grade"), rs.getBoolean("paid")));
							_freeClanHall.get(id).free();
							AuctionManager.getInstance().initNPC(id);
						}
					}
				}
			}

			_log.info(ClanHallManager.class.getSimpleName() + ": Loaded: " + getClanHalls().size() + " taken clan halls.");
			_log.info(ClanHallManager.class.getSimpleName() + ": Loaded: " + getFreeClanHalls().size() + " free clan halls.");
			_loaded = true;
		}
		catch (Exception e)
		{
			_log.warning(ClanHallManager.class.getName() + ": ClanHallManager.load(): ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public final Map<Integer, ClanHall> getFreeClanHalls()
	{
		return _freeClanHall;
	}
	
	public final Map<Integer, ClanHall> getClanHalls()
	{
		return _clanHall;
	}
	
	public final boolean isFree(int chId)
	{
		if (_freeClanHall.containsKey(chId))
			return true;
		return false;
	}
	
	public final synchronized void setFree(int chId)
	{
		_freeClanHall.put(chId, _clanHall.get(chId));
		ClanTable.getInstance().getClan(_freeClanHall.get(chId).getOwnerId()).setHasHideout(0);
		_freeClanHall.get(chId).free();
		_clanHall.remove(chId);
	}
	
	public final synchronized void setOwner(int chId, L2Clan clan)
	{
		if (!_clanHall.containsKey(chId))
		{
			_clanHall.put(chId, _freeClanHall.get(chId));
			_freeClanHall.remove(chId);
		}
		else
			_clanHall.get(chId).free();
		ClanTable.getInstance().getClan(clan.getClanId()).setHasHideout(chId);
		_clanHall.get(chId).setOwner(clan);
	}
	
	public final ClanHall getClanHallById(int clanHallId)
	{
		if (_clanHall.containsKey(clanHallId))
			return _clanHall.get(clanHallId);
		if (_freeClanHall.containsKey(clanHallId))
			return _freeClanHall.get(clanHallId);		
		SiegableHall siegableHall = ClanHallSiegeManager.getInstance().getSiegableHall(clanHallId);
		if(siegableHall != null)
			return siegableHall;
		
		return null;
	}
	
	public final ClanHall getNearbyClanHall(int x, int y, int maxDist)
	{	
		L2ClanHallZone zone = null;
		
		for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if (zone != null && zone.getDistanceToZone(x, y) < maxDist)
				return ch.getValue();
		}
		for (Map.Entry<Integer, ClanHall> ch : _freeClanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if (zone != null && zone.getDistanceToZone(x, y) < maxDist)
				return ch.getValue();
		}
		
		return ClanHallSiegeManager.getInstance().getNearbyClanHall(x, y, maxDist);
	}
	
	public final ClanHall getClanHallByOwner(L2Clan clan)
	{
		for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
			if (clan.getClanId() == ch.getValue().getOwnerId())
				return ch.getValue();
		
		return ClanHallSiegeManager.getInstance().getClanHallByOwner(clan);
	}
}
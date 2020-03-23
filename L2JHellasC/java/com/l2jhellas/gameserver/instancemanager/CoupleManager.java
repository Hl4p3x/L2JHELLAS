package com.l2jhellas.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.Couple;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class CoupleManager
{
	protected static final Logger _log = Logger.getLogger(CoupleManager.class.getName());
	
	private static CoupleManager _instance;
	
	public static final CoupleManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new CoupleManager();
			_instance.load();
		}
		return _instance;
	}
	
	private List<Couple> _couples;
	
	public void reload()
	{
		_couples.clear();
		load();
	}
	
	private final void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			
			statement = con.prepareStatement("SELECT id FROM mods_wedding ORDER BY id");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				getCouples().add(new Couple(rs.getInt("id")));
			}
			rs.close();
			statement.close();
			
			_log.info(CoupleManager.class.getSimpleName() + ": Loaded: " + getCouples().size() + " couples.");
		}
		catch (Exception e)
		{
			_log.warning(CoupleManager.class.getName() + ": CoupleManager.load(): ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public final Couple getCouple(int coupleId)
	{
		int index = getCoupleIndex(coupleId);
		if (index >= 0)
			return getCouples().get(index);
		return null;
	}
	
	public void createCouple(L2PcInstance player1, L2PcInstance player2)
	{
		if (player1 != null && player2 != null)
		{
			if (player1.getPartnerId() == 0 && player2.getPartnerId() == 0)
			{
				int _player1id = player1.getObjectId();
				int _player2id = player2.getObjectId();
				
				Couple _new = new Couple(player1, player2);
				getCouples().add(_new);
				player1.setPartnerId(_player2id);
				player2.setPartnerId(_player1id);
				player1.setCoupleId(_new.getId());
				player2.setCoupleId(_new.getId());
			}
		}
	}
	
	public void deleteCouple(int coupleId)
	{
		int index = getCoupleIndex(coupleId);
		Couple couple = getCouples().get(index);
		if (couple != null)
		{
			L2PcInstance player1 = L2World.getInstance().getPlayer(couple.getPlayer1Id());
			L2PcInstance player2 = L2World.getInstance().getPlayer(couple.getPlayer2Id());
			if (player1 != null)
			{
				player1.setPartnerId(0);
				player1.setMarried(false);
				player1.setCoupleId(0);
				
			}
			if (player2 != null)
			{
				player2.setPartnerId(0);
				player2.setMarried(false);
				player2.setCoupleId(0);
				
			}
			couple.divorce();
			getCouples().remove(index);
		}
	}
	
	public final int getCoupleIndex(int coupleId)
	{
		int i = 0;
		for (Couple temp : getCouples())
		{
			if (temp != null && temp.getId() == coupleId)
				return i;
			i++;
		}
		return -1;
	}
	
	public final List<Couple> getCouples()
	{
		if (_couples == null)
			_couples = new ArrayList<>();
		return _couples;
	}
}
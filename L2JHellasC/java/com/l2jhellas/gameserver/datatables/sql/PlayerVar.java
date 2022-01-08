package com.l2jhellas.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.templates.AbstractVarSet;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class PlayerVar extends AbstractVarSet
{
	private static final long serialVersionUID = 2L;
	
	protected static final Logger _log = Logger.getLogger(PlayerVar.class.getName());
	
	private static final String SELECT_VAR = "SELECT * FROM player_var WHERE charId = ?";
	private static final String DELETE_VAR = "DELETE FROM player_var WHERE charId = ? AND var = ?";
	private static final String INSERT_VAR = "INSERT INTO player_var (charId, var, val) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE val = VALUES(val)";
	
	public static String CUSTOM_COLOR_NAME = "namecolor";
	public static String CUSTOM_COLOR_TITLE = "titlecolor";
		
	private final int _objectId;
	
	public PlayerVar(int objectId)
	{
		_objectId = objectId;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_VAR))
		{
			ps.setInt(1, _objectId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
					put(rs.getString("var"), rs.getString("val"));
			}
		}
		catch (Exception e)
		{
			_log.warning("Couldn't restore variable for player id " + _objectId + " ," + e);
		}
	}
	
	@Override
	protected void onSet(String key, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_VAR))
		{
			ps.setInt(1, _objectId);
			ps.setString(2, key);
			ps.setString(3, value);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warning("Couldn't set " + key +" variable for player id: " + _objectId + " , " +e );
		}
	}
	
	@Override
	protected void onUnset(String key)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_VAR))
		{
			ps.setInt(1, _objectId);
			ps.setString(2, key);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warning("Couldn't unset " + key +" variable for player id: " + _objectId + " , " +e );
		}
	}
}
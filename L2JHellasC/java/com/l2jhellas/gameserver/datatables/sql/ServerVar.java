package com.l2jhellas.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.templates.AbstractVarSet;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class ServerVar extends AbstractVarSet
{
	private static final long serialVersionUID = 2L;
	
	protected static final Logger _log = Logger.getLogger(ServerVar.class.getName());
	
	private static final String SELECT_VAR = "SELECT * FROM server_var";
	private static final String DELETE_VAR = "DELETE FROM server_var WHERE var = ?";
	private static final String INSERT_VAR= "INSERT INTO server_var (var, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value)";
	
	protected ServerVar()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_VAR);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
				put(rs.getString("var"), rs.getString("value"));
		}
		catch (Exception e)
		{
			_log.warning("Couldn't restore server variables : " + e);
		}
		_log.warning("Loaded " + size() +" server variables.");
	}
	
	@Override
	protected void onSet(String key, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_VAR))
		{
			ps.setString(1, key);
			ps.setString(2, value);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warning("Couldn't set " + key +" server variable: " + e );
		}
	}
	
	@Override
	protected void onUnset(String key)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_VAR))
		{
			ps.setString(1, key);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warning("Couldn't unset " + key +" server variable: " + e);
		}
	}
	
	public static final ServerVar getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ServerVar INSTANCE = new ServerVar();
	}
}
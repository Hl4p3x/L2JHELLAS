package com.l2jhellas.util.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class L2DatabaseFactory
{
	private static final Logger _log = Logger.getLogger(L2DatabaseFactory.class.getName());
	
	private ComboPooledDataSource _source;
	
	public L2DatabaseFactory()
	{
		try
		{
			if (Config.DATABASE_MAX_CONNECTIONS < 10)
				Config.DATABASE_MAX_CONNECTIONS = 10;
			
			if (Config.DATABASE_MAX_CONNECTIONS > 200)
				Config.DATABASE_MAX_CONNECTIONS = 200;
			
			_source = new ComboPooledDataSource();
			
			_source.setAutoCommitOnClose(true);
			_source.setInitialPoolSize(10);
			_source.setMinPoolSize(10);
			_source.setMaxPoolSize(Math.max(10, Config.DATABASE_MAX_CONNECTIONS));
			
			_source.setAcquireRetryAttempts(0);
			_source.setAcquireRetryDelay(500);
			_source.setCheckoutTimeout(0);
			_source.setAcquireIncrement(5);
			
			_source.setTestConnectionOnCheckin(false);
			
			_source.setIdleConnectionTestPeriod(3600);
			_source.setMaxIdleTime(0);
			
			_source.setMaxStatementsPerConnection(100);
			
			_source.setBreakAfterAcquireFailure(false);
			_source.setDriverClass("com.mysql.cj.jdbc.Driver");
			_source.setJdbcUrl(Config.DATABASE_URL);
			_source.setUser(Config.DATABASE_LOGIN);
			_source.setPassword(Config.DATABASE_PASSWORD);
			
			_source.getConnection().close();
			
			_log.info(L2DatabaseFactory.class.getSimpleName() + ": Database Connected.");
		}
		catch (Exception e)
		{
			_log.severe(L2DatabaseFactory.class.getSimpleName() + ": Failed to init database connections: " + e);
		}
	}
	
	public void shutdown()
	{
		try
		{
			_source.close();
		}
		catch (Exception e)
		{
			_log.info(L2DatabaseFactory.class.getName() + "");
		}
		try
		{
			_source = null;
		}
		catch (Exception e)
		{
			_log.info(L2DatabaseFactory.class.getName() + "");
		}
	}
	
	public static L2DatabaseFactory getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public Connection getConnection()
	{
		Connection con = null;
		while (con == null)
		{
			try
			{
				con = _source.getConnection();
			}
			catch (SQLException e)
			{
				_log.warning(L2DatabaseFactory.class.getSimpleName() + ": Database connection failed, trying again.");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
		return con;
	}
	
	public final static String prepQuerySelect(String[] fields, String tableName, String whereClause)
	{
		String mySqlTop1 = " Limit 1 ";
		String query = "SELECT " + fields + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;
		return query;
	}
	
	private static class SingletonHolder
	{
		protected static final L2DatabaseFactory _instance;
		
		static
		{
			try
			{
				_instance = new L2DatabaseFactory();
			}
			catch (Exception e)
			{
				throw new ExceptionInInitializerError(e);
			}
		}
	}
}
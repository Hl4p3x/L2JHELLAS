package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class AdminPremium implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger(AdminPremium.class.getName());
	
	private static final String UPDATE_PREMIUMSERVICE = "REPLACE INTO account_premium (premium_service,enddate,account_name) values(?,?,?)";

	private static final String[] ADMIN_COMMANDS =
	{
		"admin_premium_menu",
		"admin_premium_add"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_premium_menu"))
			AdminHelpPage.showHelpPage(activeChar, "premium_menu.htm");
		else if (command.startsWith("admin_premium_add"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			final String accname = st.nextToken();
			final int month = Integer.parseInt(st.nextToken());
			final int DayOfmonth = Integer.parseInt(st.nextToken());
			final int HourOfDay = Integer.parseInt(st.nextToken());

			if (accname.isEmpty() || accname.length() < 2)
				activeChar.sendMessage("Invalid account!");
			else
				addPremiumServices(activeChar, month, DayOfmonth, HourOfDay, accname);
		}
		
		return true;
	}

	private static void addPremiumServices(L2PcInstance activeChar ,int month,int dayofmonth,int HourOfDay, String AccName)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_PREMIUMSERVICE))
		{
	        Calendar finishtime = Calendar.getInstance();
	        finishtime.set(Calendar.DAY_OF_MONTH, dayofmonth);
	        finishtime.set(Calendar.HOUR_OF_DAY, HourOfDay);
	        finishtime.set(Calendar.MINUTE, 0);
	        finishtime.add(Calendar.MONTH, month);  

			statement.setInt(1, 1);
			statement.setLong(2, finishtime.getTimeInMillis());
			statement.setString(3, AccName);
			statement.execute();
			
	        activeChar.sendMessage("The premium has been set until: " + finishtime.getTime() + " for account: " + AccName);
		}
		catch (SQLException e)
		{
			_log.warning(AdminPremium.class.getName() + " Could not add premium services:" + e);
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
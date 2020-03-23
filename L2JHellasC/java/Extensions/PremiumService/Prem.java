package Extensions.PremiumService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class Prem
{
	protected static final Logger _log = Logger.getLogger(Prem.class.getName());
	private long _end_pr_date;
	
	public static final Prem getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public long getPremServiceData(String playerAcc)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT premium_service,enddate FROM account_premium WHERE account_name=?");
			statement.setString(1, playerAcc);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				if (Config.USE_PREMIUMSERVICE)
				{
					_end_pr_date = rset.getLong("enddate");
				}
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(Prem.class.getName() + ": Error connecting on database. ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		return _end_pr_date;
	}
	
	private static class SingletonHolder
	{
		protected static final Prem _instance = new Prem();
	}
}
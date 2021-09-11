package Extensions.Vote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.player.VoteSite;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class VoteApiSystem
{
	private static Logger LOGGER = Logger.getLogger(VoteApiSystem.class.getName());
	
	private static final String INSERT_OR_UPDATE = "INSERT INTO character_vote (ip,site,time) VALUES (?,?,?) ON DUPLICATE KEY UPDATE site=VALUES(site), time=VALUES(time)";
		
	public void updateVoted(L2PcInstance player, VoteSite site)
	{
		player.setIsVoting(true);
		
		if (!hasVoted(player, site))
		{
			player.sendMessage("You have not voted yet.");
			player.setIsVoting(false);
			return;
		}
		
		long delay = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(12);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT_OR_UPDATE))
		{
			ps.setString(1, player.getIP());
			ps.setString(2, String.valueOf(site));
			ps.setLong(3, delay);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.warning("Couldn't update vote status for player " + player.getName() + " with ip " + player.getIP() + " for " +  String.valueOf(site));
		}
		
		L2World.getInstance().getAllPlayers().values().stream().filter(x -> x != null && x.getClient() !=null && !x.getClient().isDetached() && x.getIP().equals(player.getIP())).forEach(x -> x.updateVoteSite(site, delay));

		for (int[] result : Config.VOTE_REWARD)
			player.addItem("VoteReward", result[0], result[1], player, true);
		
		player.sendMessage("Thanks for the vote,you have been rewarded!");

		player.setIsVoting(false);
	}
	
	public void retrieve(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM character_vote WHERE ip=?"))
		{
			ps.setString(1, player.getIP());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
					player.getVoteData().put(VoteSite.valueOf(rs.getString("site")), rs.getLong("time"));
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Couldn't load vote data for player " + player.getName());
		}
	}
	
	public boolean hasVoted(L2PcInstance player, VoteSite site)
	{
		try
		{
			String endpoint = String.format(site.getApiLink(), player.getIP());
			if (endpoint.startsWith("err"))
				return false;
			
			String response = getApiResponse(endpoint, site);
			
			if(site.equals(VoteSite.L2JBRAZIL))
				return (check("\"status\":\"", response, "\",\"server_time\"") != "" && Integer.parseInt(check("\"status\":\"", response, "\",\"server_time\"")) == 1) ? true : false;
			
			if(site.equals(VoteSite.L2VOTES))
			    return(check("\"status\":\"", response, "\",\"date\"") != "" && Integer.parseInt(check("\"status\":\"", response, "\",\"date\"")) == 1) ?  true : false;

			if (response.contains(site.getResult()))
				return true; 

			return Boolean.parseBoolean(response.trim());
		}
		catch (Exception e)
		{
			LOGGER.warning("Couldn't check if player " + player.getName() + " has voted on " + String.valueOf(site));
		}
		return false;
	}
	
	public final String check(String p1, String str, String p2)
	{
		String returnValue = "";
		int i1 = str.indexOf(p1);
		int i2 = str.indexOf(p2);
		if(i1 != -1 && i2 != -1)
		{
			i1 = i1+p1.length();
			returnValue = str.substring(i1,i2);
		}
		return returnValue;
	}
	
	public String getApiResponse(String url, VoteSite site)
	{
		String response = "";
		try
		{
			final URL obj = new URL(url);
			final HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			con.setConnectTimeout(1000);
			con.addRequestProperty("User-Agent", site.getUserAgent());
			
			final int responseCode = con.getResponseCode();
			if (responseCode == 200)
			{
				final StringBuilder sb = new StringBuilder();
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
				{
					String inputLine;
					while ((inputLine = in.readLine()) != null)
						sb.append(inputLine);
				}
				response = sb.toString();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("There was problem with API response for " + String.valueOf(site));
		}
		return response;
	}
	
	public static final VoteApiSystem getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final VoteApiSystem INSTANCE = new VoteApiSystem();
	}
}
package Extensions.Vote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.Gui;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;

public class VoteRewardHopzone
{
	private static Logger _log = Logger.getLogger(VoteRewardHopzone.class.getName());
	public static int HLastVotes = 0, HCurrentVotes = 0, HGoalVotes = 0, HAllowedRewards = Config.HOPZONE_BOXES_ALLOWED;
	public static List<String> HBoxes = new ArrayList<>();
	
	public static void LoadHopZone()
	{
		int delay;
		if (Config.ALLOW_TOPZONE_VOTE_REWARD)
			delay = 60 * 1000 * Config.HOPZONE_REWARD_CHECK_TIME;
		else
			delay = 0;
		HCurrentVotes = getVotes();
		if (HCurrentVotes == -1)
		{
			if (HCurrentVotes == -1)
				_log.warning(VoteRewardHopzone.class.getSimpleName() + ": There was a problem on getting Hopzone server votes.");
			return;
		}
		HLastVotes = HCurrentVotes;
		HGoalVotes = HCurrentVotes + Config.HOPZONE_VOTES_DIFFERENCE;
		_log.info(VoteRewardHopzone.class.getName() + "Hopzone - Vote reward system initialized.");
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				RunEngine();
			}
		}, 60 * 1000 * Config.HOPZONE_REWARD_CHECK_TIME + delay, 60 * 1000 * Config.HOPZONE_REWARD_CHECK_TIME);
	}
	
	static void RunEngine()
	{
		HCurrentVotes = getVotes();
		if (HCurrentVotes == -1)
		{
			if (HCurrentVotes == -1)
				_log.warning(VoteRewardHopzone.class.getSimpleName() + ": There was a problem on getting Hopzone server votes.");
			return;
		}
		if ((HCurrentVotes >= HLastVotes && HCurrentVotes < HGoalVotes) || HCurrentVotes == HLastVotes)
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				player.sendPacket(new ExShowScreenMessage("HopZone Votes: " + HCurrentVotes, 4000, SMPOS.BOTTOM_RIGHT, true));
			Announcements.getInstance().announceToAll("HopZone Votes: " + HCurrentVotes);
			Announcements.getInstance().announceToAll("Next Reward in: " + HGoalVotes + " Votes.");
			waitSecs(5);
			for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				player.sendPacket(new ExShowScreenMessage("Next Reward in: " + HGoalVotes + " Votes.", 4000, SMPOS.BOTTOM_RIGHT, true));
		}
		if (HCurrentVotes >= HGoalVotes)
		{
			RewardPlayers();
			for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				player.sendPacket(new ExShowScreenMessage("HopZone Rewarded!", 4000, SMPOS.BOTTOM_RIGHT, true));
			Announcements.getInstance().announceToAll("All players Rewarded!");
			HGoalVotes = HCurrentVotes + Config.HOPZONE_VOTES_DIFFERENCE;
			Announcements.getInstance().announceToAll("HopZone Votes: " + HCurrentVotes);
			Announcements.getInstance().announceToAll("Next Reward in : " + HGoalVotes + " Votes.");
			waitSecs(5);
			for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				player.sendPacket(new ExShowScreenMessage("Next Reward in: " + HGoalVotes + " Votes.", 4000, SMPOS.BOTTOM_RIGHT, true));
		}
		HLastVotes = HCurrentVotes;
	}
	
	public static int countNumberEqual(String itemToCheck)
	{
		int count = 0;
		for (String i : HBoxes)
			if (i.equals(itemToCheck))
				count++;
		return count;
	}
	
	public static void RewardPlayers()
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			String temp = player.getClient().getConnection().getInetAddress().getHostAddress();
			if (Config.HOPZONE_BOXES_ALLOWED != 0 && HBoxes.contains(temp))
			{
				int count1 = countNumberEqual(temp);
				if (count1 >= Config.HOPZONE_BOXES_ALLOWED)
				{
					player.sendMessage("You have already been rewarded more than the admin wants!");
				}
				else
				{
					for (int[] element : Config.HOPZONE_REWARD)
					{
						player.addItem("Vote reward.", element[0], element[1], player, true);
						player.sendMessage("You have been rewarded check your inventory");
					}
					HBoxes.add(temp);
				}
			}
			else
			{
				HBoxes.add(temp);
				for (int[] element : Config.HOPZONE_REWARD)
				{
					player.addItem("Vote reward.", element[0], element[1], player, true);
					player.sendMessage("You have been rewarded check your inventory");
				}
			}
		}
		HBoxes.clear();
		
	}
	
	public static void waitSecs(int i)
	{
		try
		{
			Thread.sleep(i * 1000);
		}
		catch (InterruptedException ie)
		{
			if (Config.DEVELOPER)
				ie.printStackTrace();
		}
	}
	
	private static int getVotes()
	{
		try
		{
			if (!Config.HOPZONE_SERVER_LINK.endsWith(".html"))
				Config.HOPZONE_SERVER_LINK += ".html";
			
			URLConnection con = new URL(Config.HOPZONE_SERVER_LINK).openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/5.0");
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.contains("Total Votes") || line.contains("no steal make love") || line.contains("no votes here") || line.contains("bang, you don't have votes") || line.contains("la vita e bella"))
				{
					int votes = Integer.valueOf(line.split(">")[2].replace("</span", ""));
					Gui.hopzone.setText("HopZone Votes: " + votes);
					return votes;
				}
			}
			br.close();
		}
		catch (Exception e)
		{
			_log.warning(VoteRewardHopzone.class.getSimpleName() + ": Error while getting server vote count.");
		}
		return -1;
	}
}
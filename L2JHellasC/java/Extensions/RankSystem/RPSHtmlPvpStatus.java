package Extensions.RankSystem;

import java.util.logging.Level;
import java.util.logging.Logger;

import Extensions.RankSystem.Util.RPSUtil;
import Extensions.RankSystem.Util.ServerSideImage;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class RPSHtmlPvpStatus
{
	private static final Logger log = Logger.getLogger(RPSHtmlPvpStatus.class.getSimpleName());
	
	public static void sendPage(L2PcInstance player, L2PcInstance playerTarget)
	{
		NpcHtmlMessage n = new NpcHtmlMessage(0);
		
		n.setHtml(preparePage(player, playerTarget));
		
		player.sendPacket(n);
	}
	
	private static String preparePage(L2PcInstance player, L2PcInstance playerTarget)
	{
		String tb = "";
		
		// get PvP object with target. (for get how many times he killed player):
		Pvp pvp1 = new Pvp();
		Pvp pvp2 = new Pvp();
		
		if (player.getObjectId() != playerTarget.getObjectId())
		{
			pvp1 = PvpTable.getInstance().getPvp(playerTarget.getObjectId(), player.getObjectId(), true, false); // pvp: target -> player
			pvp2 = PvpTable.getInstance().getPvp(player.getObjectId(), playerTarget.getObjectId(), true, false); // pvp: player -> target
		}
		
		// get target PvpSummary:
		PvpSummary targetPvpSummary = PvpTable.getInstance().getKillerPvpSummary(playerTarget.getObjectId(), true, false);
		
		tb += "<html><title>" + playerTarget.getName() + " PvP Status</title><body>";
		
		tb += rankImgTableHtml(player, targetPvpSummary);
		
		if (player.equals(playerTarget))
		{
			tb += expBelt(player, targetPvpSummary);
		}
		else
		{
			// span
			tb += "<br>";
		}
		
		// about player target:
		tb += "<center><table border=0 cellspacing=0 cellpadding=0>";
		
		// name [level]
		if (Config.SHOW_PLAYER_LEVEL_IN_PVPINFO_ENABLED)
		{
			tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Name [lvl]</font></td><td width=135 height=22 align=left><font color=ffa000>" + playerTarget.getName() + " [" + playerTarget.getLevel() + "]</font></td></tr>";
		}
		else
		{
			tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Name</font></td><td width=135 height=22 align=left><font color=ffa000>" + playerTarget.getName() + "</font></td></tr>";
		}
		
		// current class
		tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Current class</font></td><td width=135 height=22 align=left><font color=ffa000>" + RPSUtil.getClassName(playerTarget.getClassId().getId()) + "</font></td></tr>";
		
		// main class
		if (playerTarget.getBaseClass() != playerTarget.getClassId().getId())
		{
			tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Main class</font></td><td width=135 height=22 align=left><font color=ffa000>" + RPSUtil.getClassName(playerTarget.getBaseClass()) + "</font></td></tr>";
		}
		
		// nobles / hero
		tb += "<tr><td width=135 height=22 align=rigth><font color=ae9977>Nobles/Hero</font></td><td width=135 height=22 align=left><font color=808080>";
		
		if (playerTarget.isNoble())
		{
			tb += "Yes / ";
		}
		else
		{
			tb += "No / ";
		}
		
		if (playerTarget.isHero())
		{
			tb += "Yes";
		}
		else
		{
			tb += "No";
		}
		
		tb += "</font></td></tr>";
		
		// clan
		if (player.isDead() && !player.equals(playerTarget))
		{
			tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Clan</font></td><td width=135 height=22 align=left>";
			
			if (playerTarget.getClan() != null)
			{
				tb += "<font color=ffa000>" + playerTarget.getClan().getName() + "</font>";
			}
			else
			{
				tb += "<font color=808080>No clan</font>";
			}
			
			tb += "</td></tr>";
		}
		
		// span
		tb += "<tr><td width=135 HEIGHT=1><img src=\"L2UI.Squaregray\" width=\"135\" height=\"1\"></img></td><td width=135 HEIGHT=1><img src=\"L2UI.Squaregray\" width=\"135\" height=\"1\"></img></td></tr><tr><td width=135 height=12></td><td width=135 height=12></td></tr>";
		
		if (Config.RANKS_ENABLED)
		{
			tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Rank</font></td><td width=135 height=22 align=left><font color=ffff00>" + targetPvpSummary.getRank().getName() + "</font></td></tr>";
			
			if (targetPvpSummary.getPvpExp() != targetPvpSummary.getTotalRankPoints())
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>PvP Exp / RP</font></td><td width=135 height=22 align=left><font color=ffa000>" + RPSUtil.preparePrice(targetPvpSummary.getPvpExp()) + " / " + RPSUtil.preparePrice(targetPvpSummary.getTotalRankPoints()) + "</font></td></tr>";
			else
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>PvP Exp</font></td><td width=135 height=22 align=left><font color=ffa000>" + RPSUtil.preparePrice(targetPvpSummary.getPvpExp()) + "</font></td></tr>";
		}
		
		if (Config.TOTAL_KILLS_IN_PVPINFO_ENABLED)
		{
			// legal/total kills:
			tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Legal/Total Kills</font></td><td width=135 height=22 align=left><font color=ffa000>" + RPSUtil.preparePrice(targetPvpSummary.getTotalKillsLegal()) + " / " + RPSUtil.preparePrice(targetPvpSummary.getTotalKills()) + "</font></td></tr>";
		}
		else
		{
			// legal kills:
			tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Legal Kills</font></td><td width=135 height=22 align=left><font color=ffa000>" + RPSUtil.preparePrice(targetPvpSummary.getTotalKillsLegal()) + "</font></td></tr>";
		}
		
		// war kills
		if (Config.WAR_KILLS_ENABLED)
		{
			
			if (Config.TOTAL_KILLS_IN_PVPINFO_ENABLED)
			{
				// war legal/total kills:
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Legal/Total War Kills</font></td><td width=135 height=22 align=left><font color=2080D0>" + RPSUtil.preparePrice(targetPvpSummary.getTotalWarKillsLegal()) + " / " + RPSUtil.preparePrice(targetPvpSummary.getTotalWarKills()) + "</font></td></tr>";
			}
			else
			{
				// war legal kills:
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Legal War Kills</font></td><td width=135 height=22 align=left><font color=2080D0>" + RPSUtil.preparePrice(targetPvpSummary.getTotalWarKillsLegal()) + "</font></td></tr>";
			}
		}
		
		// span
		tb += "<tr><td width=135 HEIGHT=1><img src=\"L2UI.Squaregray\" width=\"135\" height=\"1\"></img></td><td width=135 HEIGHT=1><img src=\"L2UI.Squaregray\" width=\"135\" height=\"1\"></img></td></tr><tr><td width=135 height=12></td><td width=135 height=12></td></tr>";
		
		// prepare for cut the rank points and the RPC:
		PvpSummary playerPvpSummary = null;
		if (Config.RANK_POINTS_CUT_ENABLED || Config.RANK_RPC_CUT_ENABLED)
			playerPvpSummary = PvpTable.getInstance().getKillerPvpSummary(player.getObjectId(), true, false);
		
		// Rank Points for Rank
		if (Config.RANKS_ENABLED)
		{
			tb += "<tr><td width=135 height=22 align=left><font color=ae9977>RP for kill</font></td><td width=135 height=22 align=left><font color=ffa000>" + targetPvpSummary.getRank().getPointsForKill() + "</font>";
			
			if (Config.RANK_POINTS_CUT_ENABLED && playerPvpSummary != null)
			{
				if (playerPvpSummary.getRank().getPointsForKill() < targetPvpSummary.getRank().getPointsForKill())
				{
					tb += "<font color=ff0000> [" + playerPvpSummary.getRank().getPointsForKill() + "]</font>";
				}
			}
			
			tb += "</td></tr>";
		}
		
		// PvP + Rank RPC Reward
		// (PvP + Rank || PvP || Rank) RPC Reward
		if (Config.RANK_RPC_ENABLED || Config.RPC_REWARD_ENABLED)
		{
			if (Config.RANK_RPC_ENABLED && Config.RPC_REWARD_ENABLED)
			{
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>PvP + Rank RPC Reward</font></td><td width=135 height=22 align=left><font color=ffa000>(" + Config.RPC_REWARD_AMOUNT + " + " + targetPvpSummary.getRank().getRpc();
				if (Config.RANK_RPC_CUT_ENABLED && playerPvpSummary != null)
				{
					if (playerPvpSummary.getRank().getRpc() < targetPvpSummary.getRank().getRpc())
					{
						tb += "<font color=ff0000> [" + playerPvpSummary.getRank().getRpc() + "]</font>";
					}
				}
				tb += ")</font><font color=FFFF00> RPC</font></td></tr>";
			}
			else if (!Config.RANK_RPC_ENABLED && Config.RPC_REWARD_ENABLED)
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>PvP RPC Reward</font></td><td width=135 height=22 align=left><font color=ffa000>" + Config.RPC_REWARD_AMOUNT + " </font><font color=FFFF00> RPC</font></td></tr>";
			else if (Config.RANK_RPC_ENABLED)
			{
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Rank RPC Reward</font></td><td width=135 height=22 align=left><font color=ffa000>" + targetPvpSummary.getRank().getRpc();
				if (Config.RANK_RPC_CUT_ENABLED && playerPvpSummary != null)
				{
					if (playerPvpSummary.getRank().getRpc() < targetPvpSummary.getRank().getRpc())
					{
						tb += "<font color=ff0000> [" + playerPvpSummary.getRank().getRpc() + "]</font>";
					}
				}
				tb += " </font><font color=FFFF00> RPC</font></td></tr>";
			}
		}
		
		// PvP + Rank Reward (no RPC)
		// (PvP + Rank || PvP || Rank) Reward
		if (Config.RANK_PVP_REWARD_ENABLED || Config.PVP_REWARD_ENABLED)
		{
			String list_button = "<a action=\"bypass RPS.RewardList:" + targetPvpSummary.getRankId() + ",1\">Show</a>";
			
			if (Config.RANK_PVP_REWARD_ENABLED && Config.PVP_REWARD_ENABLED)
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>PvP + Rank Reward List</font></td><td width=135 height=22 align=left>" + list_button + "</td></tr>";
			
			else if (!Config.RANK_PVP_REWARD_ENABLED && Config.PVP_REWARD_ENABLED)
			{
				String item_name = ItemTable.getInstance().getTemplate(Config.PVP_REWARD_ID).getItemName();
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>PvP Reward</font></td><td width=135 height=22 align=left><font color=ffa000>" + Config.PVP_REWARD_AMOUNT + " </font>x<font color=FFFF00> " + item_name + "</font></td></tr>";
			}
			else if (Config.RANK_PVP_REWARD_ENABLED)
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Rank Reward List</font></td><td width=135 height=22 align=left>" + list_button + "</td></tr>";
		}
		
		// protection
		if (!player.equals(playerTarget) && Config.PROTECTION_TIME_RESET > 0)
		{
			if ((Config.PVP_REWARD_ENABLED || Config.RPC_REWARD_ENABLED || Config.RANK_RPC_ENABLED) && Config.RANKS_ENABLED)
			{
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>RP/Reward Protection</font></td>";
			}
			else if (Config.RANKS_ENABLED)
			{
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Rank Points Protection</font></td>";
			}
			else if ((Config.PVP_REWARD_ENABLED || Config.RPC_REWARD_ENABLED || Config.RANK_RPC_ENABLED))
			{
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Reward Protection</font></td>";
			}
			
			long sys_time = System.currentTimeMillis();
			
			if (Config.PROTECTION_TIME_RESET > 0 && (sys_time - Config.PROTECTION_TIME_RESET < pvp2.getKillTime()))
			{
				tb += "<td width=135 height=22 align=left><font color=FFFF00>" + RankPvpSystem.calculateTimeToString(sys_time, pvp2.getKillTime()) + "</font></td></tr>";
			}
			else
			{
				tb += "<td width=135 height=22 align=left><font color=00FF00>OFF</font></td></tr>";
			}
		}
		
		tb += "</table>";
		
		if (!player.equals(playerTarget))
		{
			// span
			tb += "<table border=0 cellspacing=0 cellpadding=0><tr><td width=135 HEIGHT=1><img src=\"L2UI.Squaregray\" width=\"135\" height=\"1\"></img></td><td width=135 HEIGHT=1><img src=\"L2UI.Squaregray\" width=\"135\" height=\"1\"></img></td></tr><tr><td width=135 height=12></td><td width=135 height=12></td></tr>";
			
			if (Config.TOTAL_KILLS_ON_ME_IN_PVPINFO_ENABLED)
			{
				// legal/total kills on me:
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Legal/Total Kills on Me</font></td><td width=135 height=22 align=left><font color=FF00FF>" + pvp1.getKillsLegal() + " / " + pvp1.getKills() + "</font></td></tr>";
			}
			else
			{
				// legal kills on me:
				tb += "<tr><td width=135 height=22 align=left><font color=ae9977>Legal Kills on Me</font></td><td width=135 height=22 align=left><font color=FF00FF>" + pvp1.getKillsLegal() + "</font></td></tr>";
			}
			
			tb += "</table>";
		}
		
		if (Config.RPC_EXCHANGE_ENABLED && player.equals(playerTarget) && (Config.RANK_RPC_ENABLED || Config.RPC_REWARD_ENABLED))
		{
			// button RPC Exchanger:
			tb += "<table border=0 cellspacing=0 cellpadding=0><tr><td width=270 HEIGHT=1><img src=\"L2UI.Squaregray\" width=\"270\" height=\"1\"></img></td></tr><tr><td width=270 height=12></td></tr><tr><td width=270 align=center><button value=\"RPC Exchange\" action=\"bypass RPS.RPC:1\" width=" + Config.BUTTON_BIG_W + " height=" + Config.BUTTON_BIG_H + " back=\"" + Config.BUTTON_DOWN + "\" fore=\"" + Config.BUTTON_UP + "\"></td></tr></table>";
		}
		
		if (Config.DEATH_MANAGER_DETAILS_ENABLED && player.getRPSCookie().isDeathStatusActive() && player.isDead() && playerTarget.getObjectId() == player.getRPSCookie().getDeathStatus().getKillerObjectId())
		{ // playerTarget is not real target its handler to current killer. //getTarget() store last killer.
			// button Death Status:
			tb += "<table border=0 cellspacing=0 cellpadding=0><tr><td width=270 HEIGHT=1><img src=\"L2UI.Squaregray\" width=\"270\" height=\"1\"></img></td></tr><tr><td width=270 height=12></td></tr><tr><td width=270 align=center><button value=\"Death Status\" action=\"bypass RPS.DS\" width=" + Config.BUTTON_BIG_W + " height=" + Config.BUTTON_BIG_H + " back=\"" + Config.BUTTON_DOWN + "\" fore=\"" + Config.BUTTON_UP + "\"></td></tr></table>";
		}
		
		tb += "</center></body></html>";
		
		return tb;
	}
	
	private static String rankImgTableHtml(L2PcInstance player, PvpSummary targetPvpSummary)
	{
		String tb = "";
		
		// rank image
		tb += "<table cellpadding=0 cellspacing=0 border=0 width=292 height=60 width=292><tr><td width=60 height=60>";
		tb += ServerSideImage.getInstance().getRankIconImageHtmlTag(player, targetPvpSummary.getRankId(), 60, 60);
		// rank label
		tb += "</td><td width=232 height=60 align=left>";
		tb += ServerSideImage.getInstance().getRankNameImageHtmlTag(player, targetPvpSummary.getRankId(), 232, 60);
		tb += "</td></tr></table>";
		
		return tb;
	}
	
	private static String expBelt(L2PcInstance player, PvpSummary targetPvpSummary)
	{
		int percent = calculatePercent(targetPvpSummary);
		
		String tb = "";
		
		// percent belt
		tb += "<table border=0 cellspacing=0 cellpadding=0><tr><td width=292 height=20 align=left>";
		
		tb += ServerSideImage.getInstance().getExpImageHtmlTag(player, percent, 292, 20);
		
		tb += "</td></tr><tr><td width=292 height=18></td></tr></table>";
		
		return tb;
	}
	
	private static int calculatePercent(PvpSummary targetPvpSummary)
	{
		Rank rank = targetPvpSummary.getRank();
		
		if (rank == null)
			return 0;
		
		int rankId = rank.getId();
		long minRP = rank.getMinExp();
		long nextRP = 0;
		long currentRP = targetPvpSummary.getPvpExp();
		int percent = 0;
		
		// check if next rank exists
		if (RankTable.getInstance().getRankList().containsKey(rankId + 1))
		{
			// get minRP from next rank:
			Rank nextRank = RankTable.getInstance().getRankList().get(rankId + 1);
			
			if (nextRank != null)
				nextRP = nextRank.getMinExp();
		}
		
		if (nextRP > minRP)
		{
			double a = currentRP - minRP;
			double b = nextRP - minRP;
			double calc = (a / b) * 100;
			percent = (int) Math.floor(calc);
		}
		else
		{
			percent = 100;
		}
		
		if (percent > 100 || percent < 0)
		{
			log.log(Level.WARNING, "[RPS:ERROR] calculatePercent(): percent == " + percent + ", no exp image can be fit! ");
			log.log(Level.INFO, "[RPS:DEBUG] rankId: " + rankId + ", nextRP: " + nextRP + ", minRP: " + minRP + ", currentRP: " + currentRP);
			return 0;
		}
		
		return percent;
	}
	
}

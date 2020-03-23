package com.l2jhellas.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import Extensions.AchievmentsEngine.AchievementsManager;
import Extensions.AchievmentsEngine.base.Achievement;
import Extensions.AchievmentsEngine.base.Condition;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2AchievementsInstance extends L2Npc
{
	public L2AchievementsInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	private boolean first = true;
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player == null)
			return;
		
		if (command.startsWith("showMyAchievements"))
		{
			player.getAchievemntData();
			showMyAchievements(player);
		}
		else if (command.startsWith("achievementInfo"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			
			showAchievementInfo(id, player);
		}
		else if (command.startsWith("topList"))
		{
			showTopListWindow(player);
		}
		else if (command.startsWith("showMainWindow"))
		{
			showChatWindow(player, 0);
		}
		else if (command.startsWith("getReward"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			if (id == 10)
			{
				player.destroyItemByItemId("", 8787, 200, this, true);
				AchievementsManager.getInstance().rewardForAchievement(id, player);
			}
			else if (id == 4 || id == 19)
			{
				L2ItemInstance weapon = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				if (weapon != null)
				{
					int objid = weapon.getObjectId();
					if (AchievementsManager.getInstance().getAchievementList().get(id).meetAchievementRequirements(player))
					{
						if (!AchievementsManager.getInstance().isBinded(objid, id))
						{
							AchievementsManager.getInstance().getBinded().add(objid + "@" + id);
							player.saveAchievementData(id, objid);
							AchievementsManager.getInstance().rewardForAchievement(id, player);
						}
						else
							player.sendMessage("This item was already used to earn this achievement.");
					}
					else
					{
						player.sendMessage("Seems you don't meet the achievements requirements now.");
					}
				}
				else
					player.sendMessage("You must equip your weapon in order to get rewarded.");
			}
			else if (id == 6 || id == 18)
			{
				int clid;
				if (player.getClan() != null)
					clid = player.getClan().getClanId();
				else
					clid = -5;
				if (clid != -5)
				{
					if (!AchievementsManager.getInstance().isBinded(clid, id))
					{
						AchievementsManager.getInstance().getBinded().add(clid + "@" + id);
						player.saveAchievementData(id, clid);
						AchievementsManager.getInstance().rewardForAchievement(id, player);
					}
					else
						player.sendMessage("Current clan was already rewarded for this achievement.");
				}
			}
			else
			{
				player.saveAchievementData(id, 0);
				AchievementsManager.getInstance().rewardForAchievement(id, player);
			}
			showMyAchievements(player);
		}
		else if (command.startsWith("showMyStats"))
		{
			showMyStatsWindow(player);
		}
		else if (command.startsWith("showHelpWindow"))
		{
			showHelpWindow(player);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		if (first)
		{
			AchievementsManager.getInstance().loadUsed();
			first = false;
		}
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Hello <font color=\"LEVEL\">" + player.getName() + "</font><br>Are you looking for challenge?");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		tb.append("<button value=\"My Achievements\" action=\"bypass -h npc_%objectId%_showMyAchievements\" back=\"L2UI_ch3.bigbutton_over\" fore=\"L2UI_ch3.bigbutton\" width=95 height=21>");
		// tb.append("<button value=\"Statistics\" action=\"bypass -h npc_%objectId%_showMyStats\" back=\"L2UI_ch3.bigbutton_over\" fore=\"L2UI_ch3.bigbutton\" width=95 height=21>");
		tb.append("<button value=\"Help\" action=\"bypass -h npc_%objectId%_showHelpWindow\" back=\"L2UI_ch3.bigbutton_over\" fore=\"L2UI_ch3.bigbutton\" width=95 height=21>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		
		player.sendPacket(msg);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showMyAchievements(L2PcInstance player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><font color=\"LEVEL\">My achievements</font>:</center><br>");
		
		if (AchievementsManager.getInstance().getAchievementList().isEmpty())
		{
			tb.append("There are no Achievements created yet!");
		}
		else
		{
			int i = 0;
			
			tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
			tb.append("<tr><td width=270 align=\"left\">Name:</td><td width=60 align=\"right\">Info:</td><td width=200 align=\"center\">Status:</td></tr></table>");
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
			
			for (Achievement a : AchievementsManager.getInstance().getAchievementList().values())
			{
				tb.append(getTableColor(i));
				tb.append("<tr><td width=270 align=\"left\">" + a.getName() + "</td><td width=50 align=\"right\"><a action=\"bypass -h npc_%objectId%_achievementInfo " + a.getID() + "\">info</a></td><td width=200 align=\"center\">" + getStatusString(a.getID(), player) + "</td></tr></table>");
				i++;
			}
			
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
			tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" back=\"L2UI_ch3.bigbutton_over\" fore=\"L2UI_ch3.bigbutton\" width=95 height=21></center>");
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showAchievementInfo(int achievementID, L2PcInstance player)
	{
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">" + a.getName() + "</td></tr></table><br>");
		tb.append("<center>Status: " + getStatusString(achievementID, player));
		
		if (a.meetAchievementRequirements(player) && !player.getCompletedAchievements().contains(achievementID))
		{
			tb.append("<button value=\"Receive Reward!\" action=\"bypass -h npc_%objectId%_getReward " + a.getID() + "\" back=\"L2UI_ch3.bigbutton_over\" fore=\"L2UI_ch3.bigbutton\" width=95 height=21>");
		}
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">Description</td></tr></table><br>");
		tb.append(a.getDescription());
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"left\">Condition:</td><td width=100 align=\"left\">Value:</td><td width=200 align=\"center\">Status:</td></tr></table>");
		tb.append(getConditionsStatus(achievementID, player));
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMyAchievements\" back=\"L2UI_ch3.bigbutton_over\" fore=\"L2UI_ch3.bigbutton\" width=95 height=21></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showMyStatsWindow(L2PcInstance player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Check your <font color=\"LEVEL\">Achievements </font>statistics:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		player.getAchievemntData();
		int completedCount = player.getCompletedAchievements().size();
		
		tb.append("You have completed: " + completedCount + "/<font color=\"LEVEL\">" + AchievementsManager.getInstance().getAchievementList().size() + "</font>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" back=\"L2UI_ch3.bigbutton_over\" fore=\"L2UI_ch3.bigbutton\" width=95 height=21></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showTopListWindow(L2PcInstance player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Check your <font color=\"LEVEL\">Achievements </font>Top List:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("Not implemented yet!");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" back=\"L2UI_ch3.bigbutton_over\" fore=\"L2UI_ch3.bigbutton\" width=95 height=21></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showHelpWindow(L2PcInstance player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Achievements <font color=\"LEVEL\">Help </font>page:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("<table><tr><td>You can check the status of your achievements,</td></tr><tr><td>receive reward if every condition of the achievement is meet,</td></tr><tr><td>if not you can check which condition is still not met, by using info button</td></tr></table>");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<table><tr><td><font color=\"FF0000\">Not Completed</font> - you did not meet the achivement requirements.</td></tr>");
		tb.append("<tr><td><font color=\"LEVEL\">Get Reward</font> - you may receive reward, click info.</td></tr>");
		tb.append("<tr><td><font color=\"5EA82E\">Completed</font> - achievement completed, reward received.</td></tr></table>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" back=\"L2UI_ch3.bigbutton_over\" fore=\"L2UI_ch3.bigbutton\" width=95 height=21></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static String getStatusString(int achievementID, L2PcInstance player)
	{
		if (player.getCompletedAchievements().contains(achievementID))
			return "<font color=\"5EA82E\">Completed</font>";
		
		return (AchievementsManager.getInstance().getAchievementList().get(achievementID).meetAchievementRequirements(player)) ? "<font color=\"LEVEL\">Get Reward</font>" : "<font color=\"FF0000\">Not Completed</font>";
	}
	
	private static String getTableColor(int i)
	{
		return (i % 2 == 0) ? "<table width=270 border=0 bgcolor=\"444444\">" : "<table width=270 border=0>";
	}
	
	private static String getConditionsStatus(int achievementID, L2PcInstance player)
	{
		int i = 0;
		String s = "</center>";
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		String completed = "<font color=\"5EA82E\">Completed</font></td></tr></table>";
		String notcompleted = "<font color=\"FF0000\">Not Completed</font></td></tr></table>";
		
		for (Condition c : a.getConditions())
		{
			s += getTableColor(i);
			s += "<tr><td width=270 align=\"left\">" + c.getName() + "</td><td width=100 align=\"left\">" + c.getValue() + "</td><td width=200 align=\"center\">";
			i++;
			
			if (c.meetConditionRequirements(player))
				s += completed;
			else
				s += notcompleted;
		}
		return s;
	}
}
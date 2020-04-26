package com.l2jhellas.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2CharNoblesInstance extends L2Npc
{
	
	private final String NPC_MENU = "<html><title>Nobles Manager</title><body>" + "<center><br><br><br>" + "<button value=\"Nobles\" action=\"bypass -h npc_%objectId%_showwindow 1\" width=\"96\" height=\"19\" back=\"noico.bi2\" fore=\"noico.bi2\"><br><br>" + "</body></html>";
	
	public L2CharNoblesInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String curCommand = st.nextToken();
		
		if (curCommand.startsWith("showwindow"))
			showWindow(player, Integer.parseInt(st.nextToken()));
		else if (curCommand.startsWith("setstatus"))
			setStatus(player, Integer.parseInt(st.nextToken()));
		else if (curCommand.startsWith("info"))
			showInfoWindow(player, "info.htm");
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
		}
		else if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml(NPC_MENU);
			sendHtmlMessage(player, html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		player.sendPacket(html);
	}
	
	public void showInfoWindow(L2PcInstance player, String htm)
	{
		String html = HtmCache.getInstance().getHtm("data/html/mods/nobless/" + htm);
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(html);
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
	}
	
	private static void setStatus(L2PcInstance player, int type)
	{
		if (type == 1 && player.isNoble())
		{
			player.sendMessage("You are already nobless.");
			return;
		}
		else if (type == 1 && player.getInventory().getItemByItemId(Config.NPC_NOBLESS_ID) == null || player.getInventory().getItemByItemId(Config.NPC_NOBLESS_ID).getCount() < Config.NPC_NOBLESS_QUANTITY)
		{
			player.sendMessage("Not enough items.");
			return;
		}
		else
		{
			player.destroyItemByItemId("Consume", Config.NPC_NOBLESS_ID, Config.NPC_NOBLESS_QUANTITY, player, false);
			MagicSkillUse MSU = new MagicSkillUse(player, player, 2023, 1, 1, 0);
			player.sendPacket(MSU);
			player.broadcastPacket(MSU);
			player.setNoble(true);
			player.sendMessage("Congratulations! You got nobless status successfully!");
			player.getInventory().addItem("Noblesse Tiara", 7694, 1, player, player.getTarget());
			player.broadcastUserInfo();
		}
	}
	
	private void showWindow(L2PcInstance player, int window)
	{
		StringBuilder tb;
		NpcHtmlMessage html;
		if (window == 0)
		{
			html = new NpcHtmlMessage(1);
			html.setHtml(NPC_MENU);
			sendHtmlMessage(player, html);
		}
		else if (window == 1 && Config.NPC_NOBLES_ENABLE)
		{
			tb = new StringBuilder();
			tb.append("<html><title>Becoming a Nobles</title><body><center><br><br>");
			tb.append("Here you can get a nobleman's status.<br>");
			tb.append("<a action=\"bypass -h npc_%objectId%_setstatus 1\">Get the status of a nobleman</a>");
			tb.append("Price:<br><table>");
			tb.append("<tr><td>" + Config.NPC_NOBLESS_QUANTITY + " <font color=\"LEVEL\">" + ItemTable.getInstance().getTemplate(Config.NPC_NOBLESS_ID).getItemName() + "</font></td></tr><br>");
			tb.append("</table><br>This items can be droped by <font color=\"LEVEL\">RB's</font><br>For More info click on <a action=\"bypass -h npc_%objectId%_info\">List</a><br><button value=\"Back\" action=\"bypass -h npc_%objectId%_showwindow 0\" width=\"96\" height=\"19\" back=\"noico.bi2\" fore=\"noico.bi2\"><br>");
			
			tb.append("</center></body></html>");
			html = new NpcHtmlMessage(1);
			html.setHtml(tb.toString());
			sendHtmlMessage(player, html);
		}
		else
		{
			tb = new StringBuilder();
			tb.append("<html><title>Becoming a Nobles</title>");
			tb.append("<body><center><br><br><br><br><br><br><br><br><br><br><br><br><br><font color=\"LEVEL\"> Service is courently Disabled.</font><br><button value=\"Back\" action=\"bypass -h npc_%objectId%_showwindow 0\" width=\"96\" height=\"19\" back=\"noico.bi2\" fore=\"noico.bi2\">");
			tb.append("</center></body></html>");
			html = new NpcHtmlMessage(1);
			html.setHtml(tb.toString());
			sendHtmlMessage(player, html);
		}
	}
}
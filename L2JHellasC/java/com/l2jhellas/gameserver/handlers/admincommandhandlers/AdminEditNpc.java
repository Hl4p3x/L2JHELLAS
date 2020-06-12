package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.controllers.TradeController;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2DropCategory;
import com.l2jhellas.gameserver.model.L2DropData;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2TradeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class AdminEditNpc implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminEditChar.class.getName());
	private final static int PAGE_LIMIT = 7;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_edit_npc",
		"admin_save_npc",
		"admin_show_droplist",
		"admin_show_pagedroplist",
		"admin_edit_drop",
		"admin_add_drop",
		"admin_del_drop",
		"admin_showShop",
		"admin_showShopList",
		"admin_addShopItem",
		"admin_delShopItem",
		"admin_editShopItem",
		"admin_close_window",
		"admin_show_skilllist_npc",
		"admin_add_skill_npc",
		"admin_edit_skill_npc",
		"admin_del_skill_npc"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_showShop "))
		{
			String[] args = command.split(" ");
			if (args.length > 1)
				showShop(activeChar, Integer.parseInt(command.split(" ")[1]));
		}
		else if (command.startsWith("admin_showShopList "))
		{
			String[] args = command.split(" ");
			if (args.length > 2)
				showShopList(activeChar, Integer.parseInt(command.split(" ")[1]), Integer.parseInt(command.split(" ")[2]));
		}
		else if (command.startsWith("admin_edit_npc "))
		{
			try
			{
				String[] commandSplit = command.split(" ");
				int npcId = Integer.valueOf(commandSplit[1]);
				L2NpcTemplate npc = NpcData.getInstance().getTemplate(npcId);
				Show_Npc_Property(activeChar, npc);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Wrong usage: //edit_npc <npcId>");
			}
		}
		else if (command.startsWith("admin_show_droplist "))
		{
			int npcId = 0;
			try
			{
				npcId = Integer.parseInt(command.substring(20).trim());
			}
			catch (Exception e)
			{
			}
			
			if (npcId > 0)
				showDropList(activeChar, npcId,1);
			else
				activeChar.sendMessage("Usage: //show_droplist <npc_id>");
		}
		else if (command.startsWith("admin_show_pagedroplist "))
		{
			String[] args = command.split(" ");
			if (args.length > 2)
				showDropList(activeChar, Integer.parseInt(command.split(" ")[1]), Integer.parseInt(command.split(" ")[2]));
		}
		else if (command.startsWith("admin_addShopItem "))
		{
			String[] args = command.split(" ");
			if (args.length > 1)
				addShopItem(activeChar, args);
		}
		else if (command.startsWith("admin_delShopItem "))
		{
			String[] args = command.split(" ");
			if (args.length > 2)
				delShopItem(activeChar, args);
		}
		else if (command.startsWith("admin_editShopItem "))
		{
			String[] args = command.split(" ");
			if (args.length > 2)
				editShopItem(activeChar, args);
		}
		else if (command.startsWith("admin_save_npc "))
		{
			try
			{
				save_npc_property(activeChar, command);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_edit_drop "))
		{
			int npcId = -1, itemId = 0, category = -1000;
			try
			{
				StringTokenizer st = new StringTokenizer(command.substring(16).trim());
				if (st.countTokens() == 3)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						itemId = Integer.parseInt(st.nextToken());
						category = Integer.parseInt(st.nextToken());
						showEditDropData(activeChar, npcId, itemId, category);
					}
					catch (Exception e)
					{
					}
				}
				else if (st.countTokens() == 6)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						itemId = Integer.parseInt(st.nextToken());
						category = Integer.parseInt(st.nextToken());
						int min = Integer.parseInt(st.nextToken());
						int max = Integer.parseInt(st.nextToken());
						int chance = Integer.parseInt(st.nextToken());
						
						updateDropData(activeChar, npcId, itemId, min, max, category, chance);
					}
					catch (Exception e)
					{
						_log.warning(AdminEditNpc.class.getName() + ": admin_edit_drop parements error: " + command);
						if (Config.DEVELOPER)
							e.printStackTrace();
					}
				}
				else
					activeChar.sendMessage("Usage: //edit_drop <npc_id> <item_id> <category> [<min> <max> <chance>]");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //edit_drop <npc_id> <item_id> <category> [<min> <max> <chance>]");
			}
		}
		else if (command.startsWith("admin_add_drop "))
		{
			int npcId = -1;
			try
			{
				StringTokenizer st = new StringTokenizer(command.substring(15).trim());
				if (st.countTokens() == 1)
				{
					try
					{
						String[] input = command.substring(15).split(" ");
						if (input.length < 1)
							return true;
						npcId = Integer.parseInt(input[0]);
					}
					catch (Exception e)
					{
					}
					
					if (npcId > 0)
					{
						L2NpcTemplate npcData = NpcData.getInstance().getTemplate(npcId);
						showAddDropData(activeChar, npcData);
					}
				}
				else if (st.countTokens() == 6)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						int itemId = Integer.parseInt(st.nextToken());
						int category = Integer.parseInt(st.nextToken());
						int min = Integer.parseInt(st.nextToken());
						int max = Integer.parseInt(st.nextToken());
						int chance = Integer.parseInt(st.nextToken());
						
						addDropData(activeChar, npcId, itemId, min, max, category, chance);
					}
					catch (Exception e)
					{
						_log.warning(AdminEditNpc.class.getName() + ": admin_add_drop parements error: " + command);
						if (Config.DEVELOPER)
							e.printStackTrace();
					}
				}
				else
					activeChar.sendMessage("Usage: //add_drop <npc_id> [<item_id> <category> <min> <max> <chance>]");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //add_drop <npc_id> [<item_id> <category> <min> <max> <chance>]");
			}
		}
		else if (command.startsWith("admin_del_drop "))
		{
			int npcId = -1, itemId = -1, category = -1000;
			try
			{
				String[] input = command.substring(15).split(" ");
				if (input.length >= 3)
				{
					npcId = Integer.parseInt(input[0]);
					itemId = Integer.parseInt(input[1]);
					category = Integer.parseInt(input[2]);
				}
			}
			catch (Exception e)
			{
			}
			
			if (npcId > 0)
				deleteDropData(activeChar, npcId, itemId, category);
			else
				activeChar.sendMessage("Usage: //del_drop <npc_id> <item_id> <category>");
		}
		else if (command.startsWith("admin_show_skilllist_npc "))
		{
			int npcId = -1;
			int page = -1;
			StringTokenizer st = new StringTokenizer(command.substring(25), " ");
			if (st.countTokens() <= 2)
			{
				if (st.hasMoreTokens())
					npcId = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
					page = Integer.parseInt(st.nextToken());
			}
			
			if (npcId > 0)
				showNpcSkillList(activeChar, npcId, page);
			else
				activeChar.sendMessage("Usage: //show_skilllist_npc <npc_id> <page>");
		}
		else if (command.startsWith("admin_edit_skill_npc "))
		{
			int npcId = -1, skillId = -1;
			try
			{
				StringTokenizer st = new StringTokenizer(command.substring(21).trim(), " ");
				if (st.countTokens() == 2)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						skillId = Integer.parseInt(st.nextToken());
						showNpcSkillEdit(activeChar, npcId, skillId);
					}
					catch (Exception e)
					{
					}
				}
				else if (st.countTokens() == 3)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						skillId = Integer.parseInt(st.nextToken());
						int level = Integer.parseInt(st.nextToken());
						
						updateNpcSkillData(activeChar, npcId, skillId, level);
					}
					catch (Exception e)
					{
						_log.fine("admin_edit_skill_npc parements error: " + command);
					}
				}
				else
					activeChar.sendMessage("Usage: //edit_skill_npc <npc_id> <item_id> [<level>]");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //edit_skill_npc <npc_id> <item_id> [<level>]");
			}
		}
		else if (command.startsWith("admin_add_skill_npc "))
		{
			int npcId = -1, skillId = -1;
			try
			{
				StringTokenizer st = new StringTokenizer(command.substring(20).trim(), " ");
				if (st.countTokens() == 1)
				{
					try
					{
						String[] input = command.substring(20).split(" ");
						if (input.length < 1)
							return true;
						npcId = Integer.parseInt(input[0]);
					}
					catch (Exception e)
					{
					}
					
					if (npcId > 0)
					{
						L2NpcTemplate npcData = NpcData.getInstance().getTemplate(npcId);
						showNpcSkillAdd(activeChar, npcData);
					}
				}
				else if (st.countTokens() == 3)
				{
					try
					{
						npcId = Integer.parseInt(st.nextToken());
						skillId = Integer.parseInt(st.nextToken());
						int level = Integer.parseInt(st.nextToken());
						
						addNpcSkillData(activeChar, npcId, skillId, level);
					}
					catch (Exception e)
					{
						_log.fine("admin_add_skill_npc parements error: " + command);
					}
				}
				else
					activeChar.sendMessage("Usage: //add_skill_npc <npc_id> [<level>]");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //add_skill_npc <npc_id> [<level>]");
			}
		}
		else if (command.startsWith("admin_del_skill_npc "))
		{
			int npcId = -1, skillId = -1;
			try
			{
				String[] input = command.substring(20).split(" ");
				if (input.length >= 2)
				{
					npcId = Integer.parseInt(input[0]);
					skillId = Integer.parseInt(input[1]);
				}
			}
			catch (Exception e)
			{
			}
			
			if (npcId > 0)
				deleteNpcSkillData(activeChar, npcId, skillId);
			else
				activeChar.sendMessage("Usage: //del_skill_npc <npc_id> <skill_id>");
			
			showNpcSkillList(activeChar, npcId, -1);
		}
		
		return true;
	}
	
	private static void editShopItem(L2PcInstance activeChar, String[] args)
	{
		int tradeListID = Integer.parseInt(args[1]);
		int itemID = Integer.parseInt(args[2]);
		L2TradeList tradeList = TradeController.getInstance().getBuyList(tradeListID);
		
		L2Item item = ItemTable.getInstance().getTemplate(itemID);
		if (tradeList.getPriceForItemId(itemID) < 0)
			return;

		if (args.length > 3)
		{
			int price = Integer.parseInt(args[3]);
			int order = findOrderTradeList(itemID, tradeList.getPriceForItemId(itemID), tradeListID);
			
			tradeList.replaceItem(itemID, Integer.parseInt(args[3]));
			updateTradeList(itemID, price, tradeListID, order);
			
			activeChar.sendMessage("Updated price for " + item.getItemName() + " in Trade List " + tradeListID);
			showShopList(activeChar, tradeListID, 1);
			return;
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuilder replyMSG = new StringBuilder();
		replyMSG.append("<html><title>Merchant Shop Item Edit</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br>Edit an entry in merchantList.");
		replyMSG.append("<br>Editing Item: " + item.getItemName());
		replyMSG.append("<table>");
		replyMSG.append("<tr><td width=100>Property</td><td width=100>Edit Field</td><td width=100>Old Value</td></tr>");
		replyMSG.append("<tr><td><br></td><td></td></tr>");
		replyMSG.append("<tr><td>Price</td><td><edit var=\"price\" width=80></td><td>" + tradeList.getPriceForItemId(itemID) + "</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<center><br><br><br>");
		replyMSG.append("<button value=\"Save\" action=\"bypass admin_editShopItem " + tradeListID + " " + itemID + " $price\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br><button value=\"Back\" action=\"bypass admin_showShopList " + tradeListID + " 1\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private static void delShopItem(L2PcInstance activeChar, String[] args)
	{
		int tradeListID = Integer.parseInt(args[1]);
		int itemID = Integer.parseInt(args[2]);
		L2TradeList tradeList = TradeController.getInstance().getBuyList(tradeListID);
		
		if (tradeList.getPriceForItemId(itemID) < 0)
			return;
		
		if (args.length > 3)
		{
			int order = findOrderTradeList(itemID, tradeList.getPriceForItemId(itemID), tradeListID);
			
			tradeList.removeItem(itemID);
			deleteTradeList(tradeListID, order);
			
			activeChar.sendMessage("Deleted " + ItemTable.getInstance().getTemplate(itemID).getItemName() + " from Trade List " + tradeListID);
			showShopList(activeChar, tradeListID, 1);
			return;
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuilder replyMSG = new StringBuilder();
		replyMSG.append("<html><title>Merchant Shop Item Delete</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br>Delete entry in merchantList.");
		replyMSG.append("<br>Item to Delete: " + ItemTable.getInstance().getTemplate(itemID).getItemName());
		replyMSG.append("<table>");
		replyMSG.append("<tr><td width=100>Property</td><td width=100>Value</td></tr>");
		replyMSG.append("<tr><td><br></td><td></td></tr>");
		replyMSG.append("<tr><td>Price</td><td>" + tradeList.getPriceForItemId(itemID) + "</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<center><br><br><br>");
		replyMSG.append("<button value=\"Confirm\" action=\"bypass admin_delShopItem " + tradeListID + " " + itemID + " 1\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br><button value=\"Back\" action=\"bypass admin_showShopList " + tradeListID + " 1\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private static void addShopItem(L2PcInstance activeChar, String[] args)
	{
		int tradeListID = Integer.parseInt(args[1]);
		
		L2TradeList tradeList = TradeController.getInstance().getBuyList(tradeListID);
		if (tradeList == null)
		{
			activeChar.sendMessage("TradeList not found!");
			return;
		}
		
		if (args.length > 3)
		{
			int order = tradeList.getItems().size() + 1; // last item order + 1
			int itemID = Integer.parseInt(args[2]);
			int price = Integer.parseInt(args[3]);
			
			L2ItemInstance newItem = ItemTable.getInstance().createDummyItem(itemID);
			newItem.setPriceToSell(price);
			newItem.setCount(-1);
			tradeList.addItem(newItem);
			storeTradeList(itemID, price, tradeListID, order);
			
			activeChar.sendMessage("Added " + newItem.getItem().getItemName() + " to Trade List " + tradeList.getListId());
			showShopList(activeChar, tradeListID, 1);
			return;
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuilder replyMSG = new StringBuilder();
		replyMSG.append("<html><title>Merchant Shop Item Add</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br>Add a new entry in merchantList.");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td width=100>Property</td><td>Edit Field</td></tr>");
		replyMSG.append("<tr><td><br></td><td></td></tr>");
		replyMSG.append("<tr><td>ItemID</td><td><edit var=\"itemID\" width=80></td></tr>");
		replyMSG.append("<tr><td>Price</td><td><edit var=\"price\" width=80></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<center><br><br><br>");
		replyMSG.append("<button value=\"Save\" action=\"bypass admin_addShopItem " + tradeListID + " $itemID $price\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br><button value=\"Back\" action=\"bypass admin_showShopList " + tradeListID + " 1\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private static void showShopList(L2PcInstance activeChar, int tradeListID, int page)
	{
		L2TradeList tradeList = TradeController.getInstance().getBuyList(tradeListID);
		if (page > tradeList.getItems().size() / PAGE_LIMIT + 1 || page < 1)
			return;
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder html = itemListHtml(tradeList, page);
		
		adminReply.setHtml(html.toString());
		activeChar.sendPacket(adminReply);
		
	}
	
	private static StringBuilder itemListHtml(L2TradeList tradeList, int page)
	{
		StringBuilder replyMSG = new StringBuilder();
		
		replyMSG.append("<html><title>Merchant Shop List Page: " + page + "</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br>Edit, add or delete entries in a merchantList.");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td width=150>Item Name</td><td width=60>Price</td><td width=40>Delete</td></tr>");
		int start = ((page - 1) * PAGE_LIMIT);
		int end = Math.min(((page - 1) * PAGE_LIMIT) + (PAGE_LIMIT - 1), tradeList.getItems().size() - 1);
		for (L2ItemInstance item : tradeList.getItems(start, end + 1))
		{
			replyMSG.append("<tr><td><a action=\"bypass admin_editShopItem " + tradeList.getListId() + " " + item.getItemId() + "\">" + item.getItem().getItemName() + "</a></td>");
			replyMSG.append("<td>" + item.getPriceToSell() + "</td>");
			replyMSG.append("<td><button value=\"Del\" action=\"bypass admin_delShopItem " + tradeList.getListId() + " " + item.getItemId() + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			replyMSG.append("</tr>");
		}
		replyMSG.append("<tr>");
		int min = 1;
		int max = tradeList.getItems().size() / PAGE_LIMIT + 1;
		if (page > 1)
		{
			replyMSG.append("<td><button value=\"Page" + (page - 1) + "\" action=\"bypass admin_showShopList " + tradeList.getListId() + " " + (page - 1) + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		}
		if (page < max)
		{
			if (page <= min)
				replyMSG.append("<td></td>");
			replyMSG.append("<td><button value=\"Page" + (page + 1) + "\" action=\"bypass admin_showShopList " + tradeList.getListId() + " " + (page + 1) + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		}
		replyMSG.append("</tr><tr><td>.</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<center>");
		replyMSG.append("<button value=\"Add\" action=\"bypass admin_addShopItem " + tradeList.getListId() + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Close\" action=\"bypass admin_close_window\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center></body></html>");
		
		return replyMSG;
	}
	
	private static void showShop(L2PcInstance activeChar, int merchantID)
	{
		List<L2TradeList> tradeLists = getTradeLists(merchantID);
		if (tradeLists == null)
		{
			activeChar.sendMessage("Unknown npc template ID" + merchantID);
			return;
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuilder replyMSG = new StringBuilder("<html><title>Merchant Shop Lists</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br>Select a list to view");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Mecrchant List ID</td></tr>");
		
		for (L2TradeList tradeList : tradeLists)
		{
			if (tradeList != null)
				replyMSG.append("<tr><td><a action=\"bypass admin_showShopList " + tradeList.getListId() + " 1\">Trade List " + tradeList.getListId() + "</a></td></tr>");
		}
		
		replyMSG.append("</table>");
		replyMSG.append("<center>");
		replyMSG.append("<button value=\"Close\" action=\"bypass admin_close_window\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center></body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private static void storeTradeList(int itemID, int price, int tradeListID, int order)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("INSERT INTO merchant_buylists (`item_id`,`price`,`shop_id`,`order`) VALUES (" + itemID + "," + price + "," + tradeListID + "," + order + ")"))
		{
			stmt.execute();
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": could not store trade list");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void updateTradeList(int itemID, int price, int tradeListID, int order)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE merchant_buylists SET `price`='" + price + "' WHERE `shop_id`='" + tradeListID + "' AND `order`='" + order + "'"))
		{
			stmt.execute();
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": could not update trade list");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void deleteTradeList(int tradeListID, int order)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("DELETE FROM merchant_buylists WHERE `shop_id`='" + tradeListID + "' AND `order`='" + order + "'"))
		{
			stmt.execute();
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": could not delete trade list");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static int findOrderTradeList(int itemID, int price, int tradeListID)
	{
		int order = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM merchant_buylists WHERE `shop_id`='" + tradeListID + "' AND `item_id` ='" + itemID + "' AND `price` = '" + price + "'"))
		{
			try (ResultSet rs = stmt.executeQuery())
			{
				rs.first();
				order = rs.getInt("order");
			}
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": could not order find trade list");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		return order;
	}
	
	private static List<L2TradeList> getTradeLists(int merchantID)
	{
		String target = "npc_%objectId%_Buy";
		
		String content = HtmCache.getInstance().getHtm("data/html/merchant/" + merchantID + ".htm");
		
		content = HtmCache.getInstance().getHtm("data/html/merchant/30001.htm");
		if (content == null)
			return null;
		
		List<L2TradeList> tradeLists = new ArrayList<>();
		
		String[] lines = content.split("\n");
		int pos = 0;
		
		for (String line : lines)
		{
			pos = line.indexOf(target);
			if (pos >= 0)
			{
				int tradeListID = Integer.decode((line.substring(pos + target.length() + 1)).split("\"")[0]);
				tradeLists.add(TradeController.getInstance().getBuyList(tradeListID));
			}
		}
		return tradeLists;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void Show_Npc_Property(L2PcInstance activeChar, L2NpcTemplate npc)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		String content = HtmCache.getInstance().getHtm("data/html/admin/editnpc.htm");
		
		adminReply.setHtml(content);
		adminReply.replace("%npcId%", String.valueOf(npc.npcId));
		adminReply.replace("%templateId%", String.valueOf(npc.idTemplate));
		adminReply.replace("%name%", npc.name);
		adminReply.replace("%serverSideName%", npc.serverSideName == true ? "1" : "0");
		adminReply.replace("%title%", npc.title);
		adminReply.replace("%serverSideTitle%", npc.serverSideTitle == true ? "1" : "0");
		adminReply.replace("%collisionRadius%", String.valueOf(npc.collisionRadius));
		adminReply.replace("%collisionHeight%", String.valueOf(npc.collisionHeight));
		adminReply.replace("%level%", String.valueOf(npc.level));
		adminReply.replace("%sex%", String.valueOf(npc.sex));
		adminReply.replace("%type%", String.valueOf(npc.type));
		adminReply.replace("%attackRange%", String.valueOf(npc.baseAtkRange));
		adminReply.replace("%hp%", String.valueOf(npc.baseHpMax));
		adminReply.replace("%mp%", String.valueOf(npc.baseMpMax));
		adminReply.replace("%hpRegen%", String.valueOf(npc.baseHpReg));
		adminReply.replace("%mpRegen%", String.valueOf(npc.baseMpReg));
		adminReply.replace("%str%", String.valueOf(npc.baseSTR));
		adminReply.replace("%con%", String.valueOf(npc.baseCON));
		adminReply.replace("%dex%", String.valueOf(npc.baseDEX));
		adminReply.replace("%int%", String.valueOf(npc.baseINT));
		adminReply.replace("%wit%", String.valueOf(npc.baseWIT));
		adminReply.replace("%men%", String.valueOf(npc.baseMEN));
		adminReply.replace("%exp%", String.valueOf(npc.rewardExp));
		adminReply.replace("%sp%", String.valueOf(npc.rewardSp));
		adminReply.replace("%pAtk%", String.valueOf(npc.basePAtk));
		adminReply.replace("%pDef%", String.valueOf(npc.basePDef));
		adminReply.replace("%mAtk%", String.valueOf(npc.baseMAtk));
		adminReply.replace("%mDef%", String.valueOf(npc.baseMDef));
		adminReply.replace("%pAtkSpd%", String.valueOf(npc.basePAtkSpd));
		adminReply.replace("%aggro%", String.valueOf(npc.aggroRange));
		adminReply.replace("%mAtkSpd%", String.valueOf(npc.baseMAtkSpd));
		adminReply.replace("%rHand%", String.valueOf(npc.rhand));
		adminReply.replace("%lHand%", String.valueOf(npc.lhand));
		adminReply.replace("%armor%", String.valueOf(npc.armor));
		adminReply.replace("%walkSpd%", String.valueOf(npc.baseWalkSpd));
		adminReply.replace("%runSpd%", String.valueOf(npc.baseRunSpd));
		adminReply.replace("%factionId%", npc.factionId == null ? "" : npc.factionId);
		adminReply.replace("%factionRange%", String.valueOf(npc.factionRange));
		adminReply.replace("%isUndead%", npc.isUndead ? "1" : "0");
		adminReply.replace("%absorbLevel%", String.valueOf(npc.absorbLevel));
		activeChar.sendPacket(adminReply);
	}
	
	private static void save_npc_property(L2PcInstance activeChar, String command)
	{
		String[] commandSplit = command.split(" ");
		
		if (commandSplit.length < 4)
			return;
		
		StatsSet newNpcData = new StatsSet();
		
		try
		{
			newNpcData.set("npcId", commandSplit[1]);
			
			String statToSet = commandSplit[2];
			String value = commandSplit[3];
			
			if (commandSplit.length > 4)
			{
				for (int i = 0; i < commandSplit.length - 3; i++)
					value += " " + commandSplit[i + 4];
			}
			
			if (statToSet.equals("templateId"))
				newNpcData.set("idTemplate", Integer.valueOf(value));
			else if (statToSet.equals("name"))
				newNpcData.set("name", value);
			else if (statToSet.equals("serverSideName"))
				newNpcData.set("serverSideName", Integer.valueOf(value));
			else if (statToSet.equals("title"))
				newNpcData.set("title", value);
			else if (statToSet.equals("serverSideTitle"))
				newNpcData.set("serverSideTitle", Integer.valueOf(value) == 1 ? 1 : 0);
			else if (statToSet.equals("collisionRadius"))
				newNpcData.set("collision_radius", Integer.valueOf(value));
			else if (statToSet.equals("collisionHeight"))
				newNpcData.set("collision_height", Integer.valueOf(value));
			else if (statToSet.equals("level"))
				newNpcData.set("level", Integer.valueOf(value));
			else if (statToSet.equals("sex"))
			{
				int intValue = Integer.valueOf(value);
				newNpcData.set("sex", intValue == 0 ? "male" : intValue == 1 ? "female" : "etc");
			}
			else if (statToSet.equals("type"))
			{
				Class.forName("com.l2jhellas.gameserver.model.actor.instance." + value + "Instance");
				newNpcData.set("type", value);
			}
			else if (statToSet.equals("attackRange"))
				newNpcData.set("attackrange", Integer.valueOf(value));
			else if (statToSet.equals("hp"))
				newNpcData.set("hp", Integer.valueOf(value));
			else if (statToSet.equals("mp"))
				newNpcData.set("mp", Integer.valueOf(value));
			else if (statToSet.equals("hpRegen"))
				newNpcData.set("hpreg", Integer.valueOf(value));
			else if (statToSet.equals("mpRegen"))
				newNpcData.set("mpreg", Integer.valueOf(value));
			else if (statToSet.equals("str"))
				newNpcData.set("str", Integer.valueOf(value));
			else if (statToSet.equals("con"))
				newNpcData.set("con", Integer.valueOf(value));
			else if (statToSet.equals("dex"))
				newNpcData.set("dex", Integer.valueOf(value));
			else if (statToSet.equals("int"))
				newNpcData.set("int", Integer.valueOf(value));
			else if (statToSet.equals("wit"))
				newNpcData.set("wit", Integer.valueOf(value));
			else if (statToSet.equals("men"))
				newNpcData.set("men", Integer.valueOf(value));
			else if (statToSet.equals("exp"))
				newNpcData.set("exp", Integer.valueOf(value));
			else if (statToSet.equals("sp"))
				newNpcData.set("sp", Integer.valueOf(value));
			else if (statToSet.equals("pAtk"))
				newNpcData.set("patk", Integer.valueOf(value));
			else if (statToSet.equals("pDef"))
				newNpcData.set("pdef", Integer.valueOf(value));
			else if (statToSet.equals("mAtk"))
				newNpcData.set("matk", Integer.valueOf(value));
			else if (statToSet.equals("mDef"))
				newNpcData.set("mdef", Integer.valueOf(value));
			else if (statToSet.equals("pAtkSpd"))
				newNpcData.set("atkspd", Integer.valueOf(value));
			else if (statToSet.equals("aggro"))
				newNpcData.set("aggro", Integer.valueOf(value));
			else if (statToSet.equals("mAtkSpd"))
				newNpcData.set("matkspd", Integer.valueOf(value));
			else if (statToSet.equals("rHand"))
				newNpcData.set("rhand", Integer.valueOf(value));
			else if (statToSet.equals("lHand"))
				newNpcData.set("lhand", Integer.valueOf(value));
			else if (statToSet.equals("armor"))
				newNpcData.set("armor", Integer.valueOf(value));
			else if (statToSet.equals("runSpd"))
				newNpcData.set("runspd", Integer.valueOf(value));
			else if (statToSet.equals("factionId"))
				newNpcData.set("faction_id", value);
			else if (statToSet.equals("factionRange"))
				newNpcData.set("faction_range", Integer.valueOf(value));
			else if (statToSet.equals("isUndead"))
				newNpcData.set("isUndead", Integer.valueOf(value) == 1 ? 1 : 0);
			else if (statToSet.equals("absorbLevel"))
			{
				int intVal = Integer.valueOf(value);
				newNpcData.set("absorb_level", intVal < 0 ? 0 : intVal > 12 ? 0 : intVal);
			}
		}
		catch (Exception e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": Error saving new npc value: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		NpcData.getInstance().saveNpc(newNpcData);
		
		int npcId = newNpcData.getInteger("npcId");
		
		NpcData.getInstance().reloadNpc(npcId);
		Show_Npc_Property(activeChar, NpcData.getInstance().getTemplate(npcId));
	}

	private static void showDropList(L2PcInstance activeChar, int npcid, int page)
	{
		final L2NpcTemplate npcData = NpcData.getInstance().getTemplate(npcid);
		
		if (npcData == null)
			return;
		
		List<L2DropCategory> dropData = npcData.getDropData();
		
		if (page < 1)
			return;
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		StringBuilder html = builddropList(activeChar,npcid,page,dropData);
		
		adminReply.setHtml(html.toString());
		activeChar.sendPacket(adminReply);		
	}
	
	private static StringBuilder builddropList(L2PcInstance activeChar,int npcId, int page,List<L2DropCategory> dropData)
	{
		
		L2NpcTemplate npcData = NpcData.getInstance().getTemplate(npcId);
		
		if (npcData == null)
			return null;
				
		StringBuilder replyMSG = new StringBuilder("<html><title>NPC: " + npcData.name + "(" + npcData.npcId + ") 's drop manage</title>");
		replyMSG.append("<body><center>");
		replyMSG.append("<br1>Note: click[Item ID]to show the detail of drop data,<br1>click[Del] to delete the drop data!");
		replyMSG.append("<br1>Note: Type equals (S)weep (Q)uest and (D)rop!");
		replyMSG.append("<table width=270 border=1>");
		replyMSG.append("<tr><td>Item ID</td><td>Category</td><td>Type</td><td>Del</td></tr>");

		int start = ((page - 1) * 4);
		int end = Math.min(((page - 1) * 4) + (4 - 1), dropData.size() - 1);
		dropData = dropData.subList(start, end + 1);
		
		for (L2DropCategory data : dropData)
		{
			for (L2DropData drop : data.getAllDrops())
				replyMSG.append("<tr><td><a action=\"bypass admin_edit_drop " + npcData.npcId + " " + drop.getItemId() + " " + data.getCategoryType() + "\">" + ItemTable.getInstance().getTemplate(drop.getItemId()).getItemName() + "[" + drop.getItemId() + "]" + "</a></td><td>" + data.getCategoryType() + "</td><td>" + (drop.isQuestDrop() ? "Q" : (data.isSweep() ? "S" : "D")) + "</td><td>" + "<a action=\"bypass admin_del_drop " + npcData.npcId + " " + drop.getItemId() + " " + data.getCategoryType() + "\">Del</a></td></tr>");
		}		
		
		replyMSG.append("</table>");
		replyMSG.append("<center>");
		
		int max = dropData.size() / 4 + 1;
		if (page > 1)
			replyMSG.append("<button value=\"Page" + (page - 1) + "\" action=\"bypass admin_show_pagedroplist " + npcId + " " + (page - 1) + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		if (page < max)
			replyMSG.append("<button value=\"Page" + (page + 1) + "\" action=\"bypass admin_show_pagedroplist " + npcId + " " + (page + 1) + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		
		replyMSG.append("<button value=\"Add DropData\" action=\"bypass admin_add_drop " + npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
		replyMSG.append("<button value=\"Add Skill\" action=\"bypass admin_add_skill_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center></body></html>");
		
		return replyMSG;
	}
	
	private static void showEditDropData(L2PcInstance activeChar, int npcId, int itemId, int category)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT mobId, itemId, min, max, category, chance FROM droplist WHERE mobId=" + npcId + " AND itemId=" + itemId + " AND category=" + category))
		{
			
			try (ResultSet dropData = statement.executeQuery())
			{
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				
				StringBuilder replyMSG = new StringBuilder("<html><title>the detail of dropdata: (" + npcId + " " + itemId + " " + category + ")</title>");
				replyMSG.append("<body><center>");
				
				if (dropData.next())
				{
					replyMSG.append("<table width=270>");
					replyMSG.append("<tr><td>Editing NPC</td><td>" + NpcData.getInstance().getTemplate(dropData.getInt("mobId")).name + "</td></tr>");
					replyMSG.append("<tr><td>ItemName</td><td>" + ItemTable.getInstance().getTemplate(dropData.getInt("itemId")).getItemName() + "(" + dropData.getInt("itemId") + ")</td></tr>");
					replyMSG.append("<tr><td>Category</td><td>" + ((category == -1) ? "sweep" : Integer.toString(category)) + "</td></tr>");
					replyMSG.append("<tr><td>MIN(" + dropData.getInt("min") + ")</td><td><edit var=\"min\" width=80></td></tr>");
					replyMSG.append("<tr><td>MAX(" + dropData.getInt("max") + ")</td><td><edit var=\"max\" width=80></td></tr>");
					replyMSG.append("<tr><td>CHANCE(" + dropData.getInt("chance") + ")</td><td><edit var=\"chance\" width=80></td></tr>");
					replyMSG.append("</table><br>");
					
					replyMSG.append("<center>");
					replyMSG.append("<button value=\"Save Modify\" action=\"bypass admin_edit_drop " + npcId + " " + itemId + " " + category + " $min $max $chance\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					replyMSG.append("<br><button value=\"DropList\" action=\"bypass admin_show_droplist " + dropData.getInt("mobId") + "\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					replyMSG.append("</center>");
				}
				replyMSG.append("</body></html>");
				adminReply.setHtml(replyMSG.toString());
				
				activeChar.sendPacket(adminReply);
			}
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": could not edit drop data: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void showAddDropData(L2PcInstance activeChar, L2NpcTemplate npcData)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuilder replyMSG = new StringBuilder("<html><title>Add dropdata to " + npcData.name + "(" + npcData.npcId + ")</title>");
		replyMSG.append("<body><center>");
		
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td>Item-Id</td><td><edit var=\"itemId\" width=80></td></tr>");
		replyMSG.append("<tr><td>MIN</td><td><edit var=\"min\" width=80></td></tr>");
		replyMSG.append("<tr><td>MAX</td><td><edit var=\"max\" width=80></td></tr>");
		replyMSG.append("<tr><td>CATEGORY(sweep=-1)</td><td><edit var=\"category\" width=80></td></tr>");
		replyMSG.append("<tr><td>CHANCE(0-1000000)</td><td><edit var=\"chance\" width=80></td></tr>");
		replyMSG.append("</table>");
		
		replyMSG.append("<button value=\"SAVE\" action=\"bypass admin_add_drop " + npcData.npcId + " $itemId $category $min $max $chance\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br><button value=\"DropList\" action=\"bypass admin_show_droplist " + npcData.npcId + "\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		
		activeChar.sendPacket(adminReply);
	}
	
	private static void updateDropData(L2PcInstance activeChar, int npcId, int itemId, int min, int max, int category, int chance)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE droplist SET min=?, max=?, chance=? WHERE mobId=? AND itemId=? AND category=?"))
		{
			statement.setInt(1, min);
			statement.setInt(2, max);
			statement.setInt(3, chance);
			statement.setInt(4, npcId);
			statement.setInt(5, itemId);
			statement.setInt(6, category);
			statement.execute();
			
			showDropList(activeChar, npcId,1);
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": Could not update Npc/Mob Data ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void addDropData(L2PcInstance activeChar, int npcId, int itemId, int min, int max, int category, int chance)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO droplist(mobId, itemId, min, max, category, chance) VALUES (?,?,?,?,?,?)"))
		{
			statement.setInt(1, npcId);
			statement.setInt(2, itemId);
			statement.setInt(3, min);
			statement.setInt(4, max);
			statement.setInt(5, category);
			statement.setInt(6, chance);
			statement.execute();
			reLoadNpcDropList(npcId);
			showDropList(activeChar, npcId,1);
		}
		catch (Exception e)
		{
			updateDropData(activeChar, npcId, itemId, min, max, category, chance);
			activeChar.sendMessage("Item already existed, updated instead.");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void deleteDropData(L2PcInstance activeChar, int npcId, int itemId, int category)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement2 = con.prepareStatement("DELETE FROM droplist WHERE mobId=? AND itemId=? AND category=?"))
		{
			if (npcId > 0)
			{
				statement2.setInt(1, npcId);
				statement2.setInt(2, itemId);
				statement2.setInt(3, category);
				statement2.execute();
				reLoadNpcDropList(npcId);
				showDropList(activeChar, npcId,1);
			}
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": Could not delete Npc/Mob Data ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void reLoadNpcDropList(int npcId)
	{
		L2NpcTemplate npcData = NpcData.getInstance().getTemplate(npcId);
		if (npcData == null)
			return;
		
		// reset the drop lists
		npcData.clearAllDropData();
		npcData.getDropData().clear();
		// get the drops
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT mobId,itemId,min,max,category,chance FROM droplist WHERE mobId=?"))
		{
			statement.setInt(1, npcId);
			try (ResultSet dropDataList = statement.executeQuery())
			{
				while (dropDataList.next())
				{
					L2DropData dropData = new L2DropData();
					
					dropData.setItemId(dropDataList.getInt("itemId"));
					dropData.setMinDrop(dropDataList.getInt("min"));
					dropData.setMaxDrop(dropDataList.getInt("max"));
					dropData.setChance(dropDataList.getInt("chance"));
					
					int category = dropDataList.getInt("category");
					npcData.addDropData(dropData, category);
				}
			}
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": Could not reload Npc/Mob drop Data ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void showNpcSkillList(L2PcInstance activeChar, int npcId, int page)
	{
		L2NpcTemplate npcData = NpcData.getInstance().getTemplate(npcId);
		if (npcData == null)
		{
			activeChar.sendMessage("Template id unknown: " + npcId);
			return;
		}
		
		Map<Integer, L2Skill> skills = npcData.getSkills();
		
		int _skillsize = 0;
		
		if (skills != null)
			_skillsize = skills.size();
		else
			_skillsize = 0;
		
		int MaxSkillsPerPage = 10;
		int MaxPages = _skillsize / MaxSkillsPerPage;
		if (_skillsize > MaxSkillsPerPage * MaxPages)
			MaxPages++;
		
		if (page > MaxPages)
			page = MaxPages;
		
		int SkillsStart = MaxSkillsPerPage * page;
		int SkillsEnd = _skillsize;
		if (SkillsEnd - SkillsStart > MaxSkillsPerPage)
			SkillsEnd = SkillsStart + MaxSkillsPerPage;
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuffer replyMSG = new StringBuffer("");
		replyMSG.append("<html><title>" + npcData.getName() + " (" + npcData.getNpcId() + ") skill list</title>");
		replyMSG.append("<body>");
		replyMSG.append("Total " + Integer.valueOf(_skillsize) + ") skills.");
		String pages = "<center><table width=270><tr>";
		for (int x = 0; x < MaxPages; x++)
		{
			int pagenr = x + 1;
			if (page == x)
			{
				pages += "<td>Page " + pagenr + "</td>";
			}
			else
			{
				pages += "<td><a action=\"bypass admin_show_skilllist_npc " + npcId + " " + x + "\">Page " + pagenr + "</a></td>";
			}
		}
		pages += "</tr></table></center>";
		replyMSG.append(pages);
		
		replyMSG.append("<table width=270>");
		
		if (skills != null)
		{
			Set<Integer> skillset = skills.keySet();
			Iterator<Integer> skillite = skillset.iterator();
			Object skillobj = null;
			
			for (int i = 0; i < SkillsStart; i++)
			{
				if (skillite.hasNext())
				{
					skillobj = skillite.next();
				}
			}
			
			int cnt = SkillsStart;
			while (skillite.hasNext())
			{
				cnt++;
				if (cnt > SkillsEnd)
				{
					break;
				}
				skillobj = skillite.next();
				replyMSG.append("<tr><td><a action=\"bypass admin_edit_skill_npc " + npcId + " " + skills.get(skillobj).getId() + "\">" + skills.get(skillobj).getName() + " [" + skills.get(skillobj).getId() + "]" + "</a></td>" + "<td>" + skills.get(skillobj).getLevel() + "</td>" + "<td><a action=\"bypass admin_del_skill_npc " + npcData.getNpcId() + " " + skillobj + "\">Delete</a></td></tr>");
				
			}
		}
		replyMSG.append("</table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>");
		replyMSG.append("<button value=\"Add Skill\" action=\"bypass admin_add_skill_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Droplist\" action=\"bypass admin_show_droplist " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center></body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private static void showNpcSkillEdit(L2PcInstance activeChar, int npcId, int skillId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills WHERE npcid=" + npcId + " AND skillid=" + skillId))
		{
			try (ResultSet skillData = statement.executeQuery())
			{
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				
				StringBuffer replyMSG = new StringBuffer("<html><title>(NPC:" + npcId + " SKILL:" + skillId + ")</title>");
				replyMSG.append("<body>");
				
				if (skillData.next())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(skillData.getInt("skillid"), skillData.getInt("level"));
					
					replyMSG.append("<table>");
					replyMSG.append("<tr><td>NPC</td><td>" + NpcData.getInstance().getTemplate(skillData.getInt("npcid")).getName() + "</td></tr>");
					replyMSG.append("<tr><td>SKILL</td><td>" + skill.getName() + "(" + skillData.getInt("skillid") + ")</td></tr>");
					replyMSG.append("<tr><td>Lvl(" + skill.getLevel() + ")</td><td><edit var=\"level\" width=50></td></tr>");
					replyMSG.append("</table>");
					
					replyMSG.append("<center>");
					replyMSG.append("<button value=\"Edit Skill\" action=\"bypass admin_edit_skill_npc " + npcId + " " + skillId + " $level\"  width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					replyMSG.append("<br><button value=\"Back to Skillist\" action=\"bypass admin_show_skilllist_npc " + npcId + "\"  width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					replyMSG.append("</center>");
				}
				replyMSG.append("</body></html>");
				adminReply.setHtml(replyMSG.toString());
				
				activeChar.sendPacket(adminReply);
			}
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": Could not show npc skills");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void updateNpcSkillData(L2PcInstance activeChar, int npcId, int skillId, int level)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE npcskills SET level=? WHERE npcid=? AND skillid=?"))
		{
			L2Skill skillData = SkillTable.getInstance().getInfo(skillId, level);
			if (skillData == null)
			{
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuffer replyMSG = new StringBuffer("<html><title>Update Npc Skill Data</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"Back to Skillist\" action=\"bypass admin_show_skilllist_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
				replyMSG.append("</body></html>");
				
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
				return;
			}
			
			statement.setInt(1, level);
			statement.setInt(2, npcId);
			statement.setInt(3, skillId);
			statement.execute();
			
			if (npcId > 0)
			{
				reLoadNpcSkillList(npcId);
				
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuffer replyMSG = new StringBuffer("<html><title>Update Npc Skill Data</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"Back to Skillist\" action=\"bypass admin_show_skilllist_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
				replyMSG.append("</body></html>");
				
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
			else
			{
				activeChar.sendMessage("Unknown error");
			}
			
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": Could not update npc skill edit ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void reLoadNpcSkillList(int npcId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills WHERE npcid=? AND (skillid NOT BETWEEN 4290 AND 4302)"))
		{
			L2NpcTemplate npcData = NpcData.getInstance().getTemplate(npcId);
			if (npcData.getSkills() != null)
				npcData.getSkills().clear();
			
			// with out race
			
			statement.setInt(1, npcId);
			try (ResultSet skillDataList = statement.executeQuery())
			{
				while (skillDataList.next())
				{
					int idval = skillDataList.getInt("skillid");
					int levelval = skillDataList.getInt("level");
					L2Skill skillData = SkillTable.getInstance().getInfo(idval, levelval);
					if (skillData != null)
					{
						npcData.addSkill(skillData);
					}
				}
			}
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": Could not reload npc skill list  ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void showNpcSkillAdd(L2PcInstance activeChar, L2NpcTemplate npcData)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuffer replyMSG = new StringBuffer("<html><title>Add Skill to " + npcData.getName() + "(ID:" + npcData.getNpcId() + ")</title>");
		replyMSG.append("<body><br><center>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td width=60>SkillId </td><td><edit var=\"skillId\" width=80></td></tr>");
		replyMSG.append("<tr><td width=60>Level </td><td><edit var=\"level\" width=80></td></tr>");
		replyMSG.append("</table><br><br>");
		
		replyMSG.append("<button value=\"Add Skill\" action=\"bypass admin_add_skill_npc " + npcData.getNpcId() + " $skillId $level\"  width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br><button value=\"Back to Skillist\" action=\"bypass admin_show_skilllist_npc " + npcData.getNpcId() + "\"  width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		
		activeChar.sendPacket(adminReply);
	}
	
	private static void addNpcSkillData(L2PcInstance activeChar, int npcId, int skillId, int level)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO npcskills(npcid, skillid, level) values(?,?,?)"))
		{
			// skill check
			L2Skill skillData = SkillTable.getInstance().getInfo(skillId, level);
			if (skillData == null)
			{
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				StringBuffer replyMSG = new StringBuffer("<html><title>Add Skill to Npc(" + npcId + ")</title>");
				replyMSG.append("<body>");
				replyMSG.append("<center><button value=\"Back to Skillist\" action=\"bypass admin_show_skilllist_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
				replyMSG.append("</body></html>");
				
				adminReply.setHtml(replyMSG.toString());
				activeChar.sendPacket(adminReply);
				return;
			}
			
			statement.setInt(1, npcId);
			statement.setInt(2, skillId);
			statement.setInt(3, level);
			statement.execute();
			
			reLoadNpcSkillList(npcId);
			
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			StringBuffer replyMSG = new StringBuffer("<html><title>Add Skill to Npc (" + npcId + ", " + skillId + ", " + level + ")</title>");
			replyMSG.append("<body>");
			replyMSG.append("<center><button value=\"Add Skill\" action=\"bypass admin_add_skill_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("<br><br><button value=\"Back to Skillist\" action=\"bypass admin_show_skilllist_npc " + npcId + "\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("</center></body></html>");
			
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": Could not insert npc skill data ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void deleteNpcSkillData(L2PcInstance activeChar, int npcId, int skillId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement2 = con.prepareStatement("DELETE FROM npcskills WHERE npcid=? AND skillid=?"))
		{
			if (npcId > 0)
			{
				statement2.setInt(1, npcId);
				statement2.setInt(2, skillId);
				statement2.execute();
				
				reLoadNpcSkillList(npcId);
			}
		}
		catch (SQLException e)
		{
			_log.warning(AdminEditNpc.class.getName() + ": Could not delete npc skill data ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
}
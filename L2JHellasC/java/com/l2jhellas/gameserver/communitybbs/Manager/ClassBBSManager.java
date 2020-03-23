package com.l2jhellas.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.util.Util;

public class ClassBBSManager extends BaseBBSManager
{
	private static ClassBBSManager _Instance = null;
	
	public static ClassBBSManager getInstance()
	{
		if (_Instance == null)
		{
			_Instance = new ClassBBSManager();
		}
		return _Instance;
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		ClassId classId = activeChar.getClassId();
		int jobLevel = classId.level();
		int level = activeChar.getLevel();
		StringBuilder html = new StringBuilder("");
		html.append("<center>");
		if ((Config.ALLOW_CLASS_MASTERS_LISTCB.isEmpty()) || (!Config.ALLOW_CLASS_MASTERS_LISTCB.contains(Integer.valueOf(jobLevel))))
		{
			jobLevel = 3;
		}
		if (((level >= 20) && (jobLevel == 0)) || ((level >= 40) && (jobLevel == 1)) || ((level >= 76) && (jobLevel == 2) && (Config.ALLOW_CLASS_MASTERS_LISTCB.contains(Integer.valueOf(jobLevel)))))
		{
			html.append("<br>");
			html.append("<center>");
			html.append("<table width=600>");
			html.append("<tr><td><center>");
			L2Item item = ItemTable.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEMCB);
			html.append("You Have To Pay: <font color=\"LEVEL\">");
			html.append(Util.formatAdena(Config.CLASS_MASTERS_PRICE_LISTCB[jobLevel])).append("</font> <font color=\"LEVEL\">").append(item.getItemName()).append("</font> for proffesion.<br>");
			for (ClassId cid : ClassId.values())
			{
				if ((cid.childOf(classId)) && (cid.level() == (classId.level() + 1)))
				{
					html.append("<br><center><button value=\"").append(cid.name()).append("\" action=\"bypass -h _bbsclass;change_class;").append(cid.getId()).append(";").append(Config.CLASS_MASTERS_PRICE_LISTCB[jobLevel]).append("\" width=250 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center>");
				}
			}
			html.append("</center></td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("</center>");
		}
		else
		{
			switch (jobLevel)
			{
				case 0:
					html.append("Greetings " + activeChar.getName() + ", Your current occupation <font color=F2C202>" + activeChar.getClassId().name() + "</font>.<br>");
					html.append("In order to change your profession you must achieve: <font color=F2C202>20-th level.</font><br>");
					html.append("To activate the subclass you have to reach <font color=F2C202> 76th level. </font><br>");
					html.append("To become a Nobless, you should pump up <font color=F2C202> subclass 76-th level.</font><br>");
					break;
				case 1:
					html.append("Greetings " + activeChar.getName() + " Your current occupation <font color=F2C202>" + activeChar.getClassId().name() + "</font>.<br>");
					html.append("In order to change your profession you must achieve: <font color=F2C202>40-th level.</font><br>");
					html.append("To activate the subclass you have to reach <font color=F2C202> 76th level. </font><br>");
					html.append("To become a Nobless, you should pump up <font color=F2C202> subclass 76-th level.</font><br>");
					break;
				case 2:
					html.append("Greetings " + activeChar.getName() + " Your current occupation <font color=F2C202>" + activeChar.getClassId().name() + "</font>.<br>");
					html.append("In order to change your profession you must achieve: <font color=F2C202>76-th level.</font><br>");
					html.append("To activate the subclass you have to reach <font color=F2C202> 76th level. </font><br>");
					html.append("To become a Nobless, you should pump up <font color=F2C202> subclass 76-th level.</font><br>");
					break;
				case 3:
					html.append("Greetings " + activeChar.getName() + " Your current occupation <font color=F2C202>" + activeChar.getClassId().name() + "</font>.<br>");
					html.append("For you are no jobs available, or master class is currently not available.<br>");
			}
		}
		
		html.append("</center>");
		html.append("<br>");
		html.append("<br>");
		String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/classmaster.htm");
		content = content.replace("%classmaster%", html.toString());
		separateAndSend(content, activeChar);
		
		if (command.startsWith("_bbsclass;change_class;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			short val = Short.parseShort(st.nextToken());
			int price = Integer.parseInt(st.nextToken());
			L2Item item = ItemTable.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEMCB);
			L2ItemInstance pay = activeChar.getInventory().getItemByItemId(item.getItemId());
			if ((pay != null) && (pay.getCount() >= price))
			{
				activeChar.destroyItemByItemId("ClassMaster", Config.CLASS_MASTERS_PRICE_ITEMCB, price, activeChar, true);
				changeClass(activeChar, val);
				parsecmd("_bbsclass;", activeChar);
			}
			else if (Config.CLASS_MASTERS_PRICE_ITEMCB == 57)
			{
				activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
		}
	}
	
	private static void changeClass(L2PcInstance activeChar, short val)
	{
		int prof = activeChar.getClassId().level();
		if (activeChar.getClassId().level() == ClassId.values()[val].level())
		{
			return;
		}
		if (prof == 3)
		{
			activeChar.sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.CLASS_TRANSFER);
		}
		activeChar.setClassId(val);
		
		if (prof == 2)
		{
			activeChar.getInventory().addItem("bbsClassManager", 6622, 1, activeChar, null);
		}
		if (activeChar.isSubClassActive())
		{
			activeChar.getSubClasses().get(Integer.valueOf(activeChar.getClassIndex())).setClassId(activeChar.getActiveClass());
		}
		else
		{
			if (prof == 0)
			{
				activeChar.getInventory().addItem("bbsClassManager", 8869, 15, activeChar, null);
			}
			else if (prof == 1)
			{
				activeChar.getInventory().addItem("bbsClassManager", 8870, 15, activeChar, null);
			}
			activeChar.setBaseClass(activeChar.getActiveClass());
		}
		if (activeChar.getClassId().getId() == 97)
		{
			activeChar.getInventory().addItem("bbsClassManager", 15307, 1, activeChar, null);
		}
		else if (activeChar.getClassId().getId() == 105)
		{
			activeChar.getInventory().addItem("bbsClassManager", 15308, 1, activeChar, null);
		}
		else if (activeChar.getClassId().getId() == 112)
		{
			activeChar.getInventory().addItem("bbsClassManager", 15309, 4, activeChar, null);
		}
		activeChar.broadcastUserInfo();
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
	}
}
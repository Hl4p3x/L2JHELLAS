package com.l2jhellas.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.StringTokenizer;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.datatables.xml.AugmentationData;
import com.l2jhellas.gameserver.enums.items.L2CrystalType;
import com.l2jhellas.gameserver.model.L2Augmentation;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import com.l2jhellas.gameserver.network.serverpackets.ExVariationCancelResult;
import com.l2jhellas.gameserver.network.serverpackets.ExVariationResult;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.SkillList;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.L2Weapon;

public final class L2FastAugByAbsoInstance extends L2NpcInstance
{
	
	public L2FastAugByAbsoInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player == null)
			return;
		
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String currentcommand = st.nextToken();
		
		final String letsSliptIt = currentcommand;
		final String[] nowTheId = letsSliptIt.split("-");
		
		final String OurSplititCommand = nowTheId[0];
		final String FinallyWeHaveObjectId = nowTheId[1];
		
		switch (OurSplititCommand)
		{
			case "showremlist":
				showListWindowForRemove(player);
				player.sendPacket(new ActionFailed());
				break;
			case "showauglist":
				showListWindow(player);
				player.sendPacket(new ActionFailed());
				break;
			case "tryremove":
				
				final L2ItemInstance itemToRem = player.getInventory().getItemByObjectId(Integer.parseInt(FinallyWeHaveObjectId));
				
				if (itemToRem == null)
				{
					player.sendPacket(new ActionFailed());
					return;
				}
				
				if (itemToRem.isEquipped())
				{
					player.disarmWeapons();
					player.broadcastUserInfo();
				}
				
				itemToRem.removeAugmentation();
				
				player.sendPacket(new ExVariationCancelResult(1));
				
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(itemToRem);
				player.sendPacket(iu);
				
				player.sendPacket(new SkillList());
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1);
				sm.addItemName(itemToRem);
				player.sendPacket(sm);
				showListWindowForRemove(player);
				player.sendPacket(new ActionFailed());
				break;
			
			case "tryaug":
				if (player.getInventory().getInventoryItemCount(57, 0) < 200000)
				{
					player.sendMessage("You do not have enough adena!");
					player.sendPacket(new ActionFailed());
					return;
				}
				
				final L2ItemInstance itemToAug = player.getInventory().getItemByObjectId(Integer.parseInt(FinallyWeHaveObjectId));
				
				if (itemToAug == null)
				{
					player.sendPacket(new ActionFailed());
					return;
				}
				
				if (itemToAug.isEquipped())
				{
					player.disarmWeapons();
					player.broadcastUserInfo();
				}
				
				final L2Augmentation aug = AugmentationData.getInstance().generateRandomAugmentation(itemToAug, 2, 2);
				itemToAug.setAugmentation(aug);
				
				final int stat12 = 0x0000FFFF & aug.getAugmentationId();
				final int stat34 = aug.getAugmentationId() >> 16;
				player.sendPacket(new ExVariationResult(stat12, stat34, 1));
				
				InventoryUpdate iua = new InventoryUpdate();
				iua.addModifiedItem(itemToAug);
				player.sendPacket(iua);
				
				StatusUpdate su = new StatusUpdate(player.getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);
				
				showListWindow(player);
				
				player.getInventory().reduceAdena("FastAugh", 200000, player, null);
				
				player.sendPacket(SystemMessageId.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED);
				
				if (itemToAug.getAugmentation().getSkill() != null)
				{
					player.sendPacket(new ExShowScreenMessage("You have " + itemToAug.getAugmentation().getSkill().getName(), 5000, SMPOS.TOP_CENTER, true));
					player.sendPacket(new SkillList());
				}
				
				player.sendPacket(new ActionFailed());
				break;
			default: // Send a Server->Client packet ActionFailed to the L2PcInstance
				player.sendPacket(new ActionFailed());
				return;
		}
		
		super.onBypassFeedback(player, command);
		
		player.sendPacket(new ActionFailed());
	}
	
	public void showListWindow(L2PcInstance player)
	{
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		StringBuilder tb = new StringBuilder("");
		String Rem = "RemoveAug";
		Collection<L2ItemInstance> invitems = player.getInventory().getItems();
		
		tb.append("<html><head><title>By AbsolutePower</title></head><body>");
		tb.append("<center>");
		tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
		tb.append("<tr>");
		tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><img src=\"L2ui_ch3.menubutton4\" width=\"38\" height=\"38\"></td>");
		tb.append("<td valign=\"top\"><font color=\"FF6600\">AugmentHelper</font>");
		tb.append("<br1><font color=\"00FF00ju\">" + player.getName() + "</font>, use this menu for fast augment :)<br1></td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("</center>");
		tb.append("<center>");
		tb.append("<br>");
		
		for (L2ItemInstance item : invitems)
		{
			if (item == null)
				continue;
			
			boolean canBeShow = isValid(player, item);
			
			if (canBeShow)
				tb.append("<button value=\"" + item.getItemName() + "\" action=\"bypass -h npc_" + getObjectId() + "_tryaug-" + item.getObjectId() + "\" width=204 height=20 back=\"sek.cbui75\" fore=\"sek.cbui75\"><br>");
		}
		
		tb.append("<br>");
		tb.append("<button value=\"" + Rem + "\" action=\"bypass -h npc_" + getObjectId() + "_showremlist-1" + "\" width=204 height=20 back=\"sek.cbui75\" fore=\"sek.cbui75\"><br>");
		tb.append("</center>");
		tb.append("</body></html>");
		
		nhm.setHtml(tb.toString());
		player.sendPacket(nhm);
	}
	
	public void showListWindowForRemove(L2PcInstance player)
	{
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		StringBuilder tb = new StringBuilder("");
		String Rem = "GobackToAugList";
		Collection<L2ItemInstance> invitems = player.getInventory().getItems();
		
		tb.append("<html><head><title>By AbsolutePower</title></head><body>");
		tb.append("<center>");
		tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
		tb.append("<tr>");
		tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><img src=\"L2ui_ch3.menubutton4\" width=\"38\" height=\"38\"></td>");
		tb.append("<td valign=\"top\"><font color=\"FF6600\">AugmentHelper</font>");
		tb.append("<br1><font color=\"00FF00ju\">" + player.getName() + "</font>, use this menu for fast augment :)<br1></td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("</center>");
		tb.append("<center>");
		tb.append("<br>");
		
		for (L2ItemInstance item : invitems)
		{
			if (item == null)
				continue;
			
			boolean canBeShow = item.isAugmented();
			
			if (canBeShow)
				tb.append("<button value=\"" + item.getItemName() + "\" action=\"bypass -h npc_" + getObjectId() + "_tryremove-" + item.getObjectId() + "\" width=204 height=20 back=\"sek.cbui75\" fore=\"sek.cbui75\"><br>");
		}
		
		tb.append("<br>");
		tb.append("<button value=\"" + Rem + "\" action=\"bypass -h npc_" + getObjectId() + "_showauglist-1" + "\" width=204 height=20 back=\"sek.cbui75\" fore=\"sek.cbui75\"><br>");
		tb.append("</center>");
		tb.append("</body></html>");
		
		nhm.setHtml(tb.toString());
		player.sendPacket(nhm);
	}
	
	protected static final boolean isValid(L2PcInstance player, L2ItemInstance item)
	{
		if (!isValid(player))
			return false;
		
		if (item.getOwnerId() != player.getObjectId())
			return false;
		if (item.isAugmented())
			return false;
		if (item.isHeroItem())
			return false;
		if (item.isShadowItem())
			return false;
		if (item.getItem().getCrystalType().isLesser(L2CrystalType.C))
			return false;
		if (item.isQuestItem())
			return false;
		
		switch (item.getLocation())
		{
			case INVENTORY:
			case PAPERDOLL:
				break;
			default:
				return false;
		}
		
		if (item.getItem() instanceof L2Weapon)
		{
			switch (((L2Weapon) item.getItem()).getItemType())
			{
				case NONE:
				case ROD:
					return false;
				default:
					break;
			}
		}
		else
			return false;
		
		return true;
	}
	
	protected static final boolean isValid(L2PcInstance player)
	{
		if (player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
			return false;
		}
		if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessageId.AUGMENTED_ITEM_CANNOT_BE_DISCARDED);
			return false;
		}
		if (player.isDead())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
			return false;
		}
		if (player.isParalyzed())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
			return false;
		}
		if (player.isFishing())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
			return false;
		}
		if (player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
			return false;
		}
		if (player.isCursedWeaponEquiped())
			return false;
		if (player.isProcessingTransaction())
			return false;
		
		return true;
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
			player.setLastFolkNPC(this);
			showListWindow(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
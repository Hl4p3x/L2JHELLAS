package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.instancemanager.CoupleManager;
import com.l2jhellas.gameserver.model.Couple;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2WeddingManagerInstance extends L2Npc
{
	public L2WeddingManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			else
				showMessageWindow(player);
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showMessageWindow(L2PcInstance player)
	{
		String filename = "data/html/mods/Wedding_start.htm";
		String replace = String.valueOf(Config.MOD_WEDDING_PRICE);
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// standard msg
		String filename = "data/html/mods/Wedding_start.htm";
		String replace = "";
		
		// if player has no partner
		if (player.getPartnerId() == 0)
		{
			filename = "data/html/mods/Wedding_nopartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		L2PcInstance ptarget = L2World.getInstance().getPlayer(player.getPartnerId());
		// partner online ?
		if ((ptarget == null) || (!ptarget.isOnline()))
		{
			filename = "data/html/mods/Wedding_notfound.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		// already married ?
		if (player.isMarried())
		{
			filename = "data/html/mods/Wedding_already.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (player.isMarryAccepted())
		{
			filename = "data/html/mods/Wedding_waitforpartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (command.startsWith("AcceptWedding"))
		{
			// accept the wedding request
			player.setMarryAccepted(true);
			Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
			couple.marry();
			
			// messages to the couple
			player.sendMessage("Congratulations you are married!");
			player.setMarried(true);
			player.setMaryRequest(false);
			ptarget.sendMessage("Congratulations you are married!");
			ptarget.setMarried(true);
			ptarget.setMaryRequest(false);
			
			// wedding march
			MagicSkillUse MSU = new MagicSkillUse(player, player, 2230, 1, 1, 0);
			player.broadcastPacket(MSU);
			MSU = new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0);
			ptarget.broadcastPacket(MSU);
			if (Config.CUPID_TO_PLAYERS)
			{
				player.addItem("Cupids Bow", 9140, 1, player, true);
				ptarget.addItem("Cupids Bow", 9140, 1, player, true);
				player.getInventory().updateDatabase();
				ptarget.getInventory().updateDatabase();
			}
			// fireworks
			L2Skill skill = SkillTable.getInstance().getInfo(2025, 1);
			if (skill != null)
			{
				player.useMagic(skill, true, true);
				ptarget.useMagic(skill, true, true);
			}
			
			if (Config.MOD_WEDDING_ANNOUNCE)
			{
				Announcements.getInstance().announceToAll("Congratulations to " + player.getName() + " and " + ptarget.getName() + "! They have been married.");
			}
			
			MSU = null;
			
			filename = "data/html/mods/Wedding_accepted.htm";
			replace = ptarget.getName();
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		else if (command.startsWith("DeclineWedding"))
		{
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			player.setMarryAccepted(false);
			ptarget.setMarryAccepted(false);
			player.sendMessage("You declined!");
			ptarget.sendMessage("Your partner declined!");
			replace = ptarget.getName();
			filename = "data/html/mods/Wedding_declined.htm";
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		else if (player.isMaryRequest())
		{
			// check for formalwear
			if (Config.MOD_WEDDING_FORMALWEAR)
			{
				Inventory inv3 = player.getInventory();
				L2ItemInstance item3 = inv3.getPaperdollItem(10);
				if (null == item3)
				{
					player.setIsWearingFormalWear(false);
				}
				else
				{
					String strItem = Integer.toString(item3.getItemId());
					String frmWear = Integer.toString(6408);
					if (strItem.equals(frmWear))
					{
						player.setIsWearingFormalWear(true);
					}
					else
					{
						player.setIsWearingFormalWear(false);
					}
				}
			}
			if (Config.MOD_WEDDING_FORMALWEAR && !player.isWearingFormalWear())
			{
				filename = "data/html/mods/Wedding_noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			filename = "data/html/mods/Wedding_ask.htm";
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			replace = ptarget.getName();
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if (command.startsWith("AskWedding"))
		{
			// check for formalwear
			if (Config.MOD_WEDDING_FORMALWEAR)
			{
				Inventory inv3 = player.getInventory();
				L2ItemInstance item3 = inv3.getPaperdollItem(10);
				
				if (null == item3)
				{
					player.setIsWearingFormalWear(false);
				}
				else
				{
					String frmWear = Integer.toString(6408);
					String strItem = null;
					strItem = Integer.toString(item3.getItemId());
					
					if ((null != strItem) && strItem.equals(frmWear))
					{
						player.setIsWearingFormalWear(true);
					}
					else
					{
						player.setIsWearingFormalWear(false);
					}
				}
			}
			if (Config.MOD_WEDDING_FORMALWEAR && !player.isWearingFormalWear())
			{
				filename = "data/html/mods/Wedding_noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			else if (player.getAdena() < Config.MOD_WEDDING_PRICE)
			{
				filename = "data/html/mods/Wedding_adena.htm";
				replace = String.valueOf(Config.MOD_WEDDING_PRICE);
				sendHtmlMessage(player, filename, replace);
				return;
			}
			else
			{
				player.setMarryAccepted(true);
				ptarget.setMaryRequest(true);
				replace = ptarget.getName();
				filename = "data/html/mods/Wedding_requested.htm";
				player.getInventory().reduceAdena("Wedding", Config.MOD_WEDDING_PRICE, player, player.getLastFolkNPC());
				sendHtmlMessage(player, filename, replace);
				return;
			}
		}
		sendHtmlMessage(player, filename, replace);
	}
	
	private void sendHtmlMessage(L2PcInstance player, String filename, String replace)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}
package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.SiegeInfo;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2SiegeNpcInstance extends L2NpcInstance
{
	
	public L2SiegeNpcInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
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
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showSiegeInfoWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void showSiegeInfoWindow(L2PcInstance player)
	{
		int castleid = MapRegionTable.getAreaCastle(getX(), getY());
		Castle castle = CastleManager.getInstance().getCastleById(castleid);
		
		if (getConquerableHall() != null && validateCondition(player)) 
			getConquerableHall().showSiegeInfo(player);
		else if (castle != null)
		{
			if (validateCondition(player))
			{
				player.sendPacket(new SiegeInfo(castle));
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/siege/" + getTemplate().npcId + "-busy.htm");
				html.replace("%castlename%", castle.getName());
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	private boolean validateCondition(L2PcInstance player)
	{
		int castleid = MapRegionTable.getAreaCastle(getX(), getY());
		Castle castle = CastleManager.getInstance().getCastleById(castleid);
		
		if (castle != null && castle.getSiege().getIsInProgress())
			return false; // Busy because of siege
			
		return true;
	}
}
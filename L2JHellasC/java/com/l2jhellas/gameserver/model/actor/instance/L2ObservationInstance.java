package com.l2jhellas.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public final class L2ObservationInstance extends L2NpcInstance
{
	
	public L2ObservationInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("observeSiege"))
		{
			String val = command.substring(13);
			StringTokenizer st = new StringTokenizer(val);
			st.nextToken(); // Bypass cost
			if (player.getOlympiadGameId() > 0 || player.isInOlympiadMode())
			{
				player.sendMessage("You already participated in Olympiad!");
				return;
			}
						
			if (player.isRegisteredInFunEvent() || player.isInFunEvent())
			{
				player.sendMessage("You already participated in Event!");
				return;
			}
			
			if (player.isInCombat() || player.getPvpFlag() > 0)
			{
				player.sendMessage("You are in combat now!");
				return;
			}
			
			if (SiegeManager.getSiege(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())) != null)
			{
				doObserve(player, val);
			}
			else
				player.sendPacket(SystemMessageId.ONLY_VIEW_SIEGE);
		}
		else if (command.startsWith("observe"))
		{
			
			if (player.getOlympiadGameId() > 0 || player.isInOlympiadMode())
			{
				player.sendMessage("You already participated in Olympiad!");
				return;
			}
			
			if (player.isRegisteredInFunEvent() || player.isInFunEvent())
			{
				player.sendMessage("You already participated in Event!");
				return;
			}
			
			if (player.isInCombat() || player.getPvpFlag() > 0)
			{
				player.sendMessage("You are in combat now!");
				return;
			}
			doObserve(player, command.substring(8));
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/observation/" + pom + ".htm";
	}
	
	private void doObserve(L2PcInstance player, String val)
	{
		StringTokenizer st = new StringTokenizer(val);
		int cost = Integer.parseInt(st.nextToken());
		int x = Integer.parseInt(st.nextToken());
		int y = Integer.parseInt(st.nextToken());
		int z = Integer.parseInt(st.nextToken());
		if (player.reduceAdena("Broadcast", cost, this, true))
		{
			player.enterObserverMode(x, y, z);
			player.sendPacket(new ItemList(player, false));
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
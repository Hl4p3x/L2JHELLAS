package com.l2jhellas.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public class L2CasinoInstance extends L2Npc
{
	private final static int MIN_BET = 1;
	private final static int MAX_BET = 1000000;
	
	public L2CasinoInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player == null)
			return;
		
		if (command.startsWith("bet"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			int itemId = Integer.parseInt(st.nextToken());
			int count = Integer.parseInt(st.nextToken());
			calculateBet(player, itemId, count);
		}
		else
		   super.onBypassFeedback(player, command);
	}
		
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/casino/index.htm");
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
	
	public void calculateBet(L2PcInstance player, int itemId, int count)
	{
		if (player.getInventory().getItemByItemId(itemId) == null || player.getInventory().getItemByItemId(itemId).getCount() < count)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			return;
		}
		
		if (count < MIN_BET || count > MAX_BET)
		{
			player.sendMessage("You can bet from " + MIN_BET + " to " + MAX_BET + ".");
			return;
		}
		
		if (!player.destroyItemByItemId("Consume", itemId, count, player, true))
			return;
					
		if (Rnd.get(100) < 50)
		{
			player.sendPacket(new ExShowScreenMessage("Congratulations " + player.getName() + " you won", 3000));
			player.addItem("Consume", itemId, count * 2, player, true);
		}
		else
		    player.sendPacket(new ExShowScreenMessage("Im sorry " + player.getName() + " you lost, try again", 3000));	
	}
}
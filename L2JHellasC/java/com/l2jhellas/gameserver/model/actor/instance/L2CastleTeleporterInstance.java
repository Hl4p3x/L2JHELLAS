package com.l2jhellas.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.TeleportLocationData;
import com.l2jhellas.gameserver.model.L2TeleportLocation;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public final class L2CastleTeleporterInstance extends L2NpcInstance
{
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;
	private static final int COND_REGULAR = 3;
	
	public L2CastleTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		int condition = validateCondition(player);
		if (condition <= COND_BUSY_BECAUSE_OF_SIEGE)
			return;
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		if (actualCommand.equalsIgnoreCase("goto"))
		{
			if (st.countTokens() <= 0)
			{
				return;
			}
			int whereTo = Integer.parseInt(st.nextToken());
			if (condition == COND_REGULAR)
			{
				doTeleport(player, whereTo);
				return;
			}
			else if (condition == COND_OWNER)
			{
				int minPrivilegeLevel = 0; // NOTE: Replace 0 with highest level when privilege level is implemented
				if (st.countTokens() >= 1)
				{
					minPrivilegeLevel = Integer.parseInt(st.nextToken());
				}
				if (10 >= minPrivilegeLevel) // NOTE: Replace 10 with privilege level of player
					doTeleport(player, whereTo);
				else
					player.sendMessage("You don't have the sufficient access level to teleport there.");
				return;
			}
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
		
		return "data/html/teleporter/" + pom + ".htm";
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/teleporter/castleteleporter-no.htm";
		
		int condition = validateCondition(player);
		if (condition == COND_REGULAR)
		{
			super.showChatWindow(player);
			return;
		}
		else if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
				filename = "data/html/teleporter/castleteleporter-busy.htm"; // Busy because of siege
			else if (condition == COND_OWNER) // Clan owns castle
				filename = "data/html/teleporter/" + getNpcId() + ".htm"; // Owner message window
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationData.getInstance().getTemplate(val);
		if (list != null)
		{
			if (player.destroyItemsByList("Teleport", list.getItemsList(), this, true, 0))
			{
				if (Config.DEBUG)
					_log.fine("Teleporting player " + player.getName() + " to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ());
				
				// teleport
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
				player.stopMove(new Location(list.getLocX(), list.getLocY(), list.getLocZ(), player.getHeading()));
			}
		}
		else
		{
			_log.warning(L2CastleTeleporterInstance.class.getName() + ": No teleport destination with id:" + val);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private int validateCondition(L2PcInstance player)
	{
		if ((player.getClan() != null) && (getCastle() != null))
		{
			if (getCastle().getSiege().getIsInProgress())
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
			else if (getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
				return COND_OWNER; // Owner
		}
		
		return COND_ALL_FALSE;
	}
}
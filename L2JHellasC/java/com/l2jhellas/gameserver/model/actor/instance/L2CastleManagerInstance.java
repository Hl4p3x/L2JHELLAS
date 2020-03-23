package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.SiegeInfo;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2CastleManagerInstance extends L2Npc
{
	public String filename;
	
	public L2CastleManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player == null)
			return;
		if (!canTarget(player))
			return;
		
		else if (command.startsWith("siege_"))
		{
			int castleId = 0;
			
			if (command.startsWith("siege_gludio"))
				castleId = 1;
			else if (command.startsWith("siege_dion"))
				castleId = 2;
			else if (command.startsWith("siege_giran"))
				castleId = 3;
			else if (command.startsWith("siege_oren"))
				castleId = 4;
			else if (command.startsWith("siege_aden"))
				castleId = 5;
			else if (command.startsWith("siege_innadril"))
				castleId = 6;
			else if (command.startsWith("siege_goddard"))
				castleId = 7;
			else if (command.startsWith("siege_rune"))
				castleId = 8;
			else if (command.startsWith("siege_schuttgart"))
				castleId = 9;
			
			Castle castle = CastleManager.getInstance().getCastleById(castleId);
			if (castle != null && castleId != 0)
				player.sendPacket(new SiegeInfo(castle));
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		filename = (getHtmlPath(getNpcId(), val));
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(showWindow(player));
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
		
	}
	
	private static String showWindow(L2PcInstance player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Siege Manager</title><body>");
		tb.append("<center><font color=\"LEVEL\">Choose The Castle Manager.</font><br1>");
		tb.append("<table>");
		tb.append("<tr><td height=10></td></tr>");
		tb.append("<tr><td align=center><button action=\"bypass -h npc_%objectId%_siege_giran\" value=\"Giran Castle\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\"></td></tr>");
		tb.append("<tr><td align=center><button action=\"bypass -h npc_%objectId%_siege_aden\" value=\"Aden Castle\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\"></td></tr>");
		tb.append("<tr><td align=center><button action=\"bypass -h npc_%objectId%_siege_rune\" value=\"Rune Castle\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\"></td></tr>");
		tb.append("<tr><td align=center><button action=\"bypass -h npc_%objectId%_siege_oren\" value=\"Oren Castle\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\"></td></tr>");
		tb.append("<tr><td align=center><button action=\"bypass -h npc_%objectId%_siege_dion\" value=\"Dion Castle\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\"></td></tr>");
		tb.append("<tr><td align=center><button action=\"bypass -h npc_%objectId%_siege_gludio\" value=\"Gludio Castle\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\"></td></tr>");
		tb.append("<tr><td align=center><button action=\"bypass -h npc_%objectId%_siege_goddard\" value=\"Goddard Castle\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\"></td></tr>");
		tb.append("<tr><td align=center><button action=\"bypass -h npc_%objectId%_siege_schuttgart\" value=\"Schuttgart Castle\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\"></td></tr>");
		tb.append("<tr><td align=center><button action=\"bypass -h npc_%objectId%_siege_innadril\" value=\"Innadril Castle\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\"></td></tr>");
		tb.append("</table>");
		tb.append("<br><br>");
		tb.append("<font color=3293F3>Lineage II</font><br>");
		tb.append("<img src=\"L2UI.SquareWhite\" width=258 height=1>");
		tb.append("</center>");
		tb.append("</body></html>");
		player.sendPacket(ActionFailed.STATIC_PACKET);
		return tb.toString();
	}
}
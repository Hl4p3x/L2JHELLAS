package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.datatables.xml.MultisellData;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2BlacksmithInstance extends L2NpcInstance
{
	public L2BlacksmithInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("multisell"))
		{
			int listId = Integer.parseInt(command.substring(9).trim());
			MultisellData.getInstance().SeparateAndSend(listId, player, false, getCastle().getTaxRate());
		}
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;
		
		return "data/html/blacksmith/" + pom + ".htm";
	}
}
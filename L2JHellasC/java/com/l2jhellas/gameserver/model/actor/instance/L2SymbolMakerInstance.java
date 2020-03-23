package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.datatables.xml.HennaData;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.HennaEquipList;
import com.l2jhellas.gameserver.network.serverpackets.HennaRemoveList;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2SymbolMakerInstance extends L2NpcInstance
{
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.equals("Draw"))
			player.sendPacket(new HennaEquipList(player, HennaData.getInstance().getAvailableHenna(player.getClassId().getId())));
		else if (command.equals("RemoveList"))
		{
			boolean hasHennas = false;
			for (int i = 1; i <= 3; i++)
			{
				if (player.getHenna(i) != null)
					hasHennas = true;
			}
			
			if (hasHennas)
				player.sendPacket(new HennaRemoveList(player));
			else
				player.sendPacket(SystemMessageId.SYMBOL_NOT_FOUND);
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	public L2SymbolMakerInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/symbolmaker/SymbolMaker.htm";
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
}
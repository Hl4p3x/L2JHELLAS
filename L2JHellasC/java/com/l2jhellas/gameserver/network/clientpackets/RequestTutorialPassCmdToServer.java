package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket
{
	
	String _bypass = null;
	
	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.ALLOW_TUTORIAL)
		{
			final L2PcInstance player = getClient().getActiveChar();
			
			if (player == null)
				return;
			
			final QuestState qs = player.getQuestState("Q255_Tutorial");
			
			if (qs != null)
				qs.getQuest().notifyEvent(_bypass, null, player);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 86 RequestTutorialPassCmdToServer";
	}
}

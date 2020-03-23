package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class RequestTutorialClientEvent extends L2GameClientPacket
{
	
	private static final String _C__88_REQUESTTUTORIALCLIENTEVENT = "[C] 88 RequestTutorialClientEvent";
	int eventId = 0;
	
	@Override
	protected void readImpl()
	{
		eventId = readD();
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
				qs.getQuest().notifyEvent("CE" + eventId + "", null, player);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__88_REQUESTTUTORIALCLIENTEVENT;
	}
}

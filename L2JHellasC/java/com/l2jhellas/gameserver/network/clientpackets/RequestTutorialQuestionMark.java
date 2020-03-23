package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	
	// private static Logger _log =
	// Logger.getLogger(RequestTutorialQuestionMark.class.getName());
	int _number = 0;
	
	@Override
	protected void readImpl()
	{
		_number = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		if (Config.ALLOWFISHING && _number == 1994)
			player.showFishingHelp();
		else if (Config.ALLOW_TUTORIAL)
		{
			final QuestState qs = player.getQuestState("Q255_Tutorial");
			
			if (qs != null)
				qs.getQuest().notifyEvent("QM" + _number + "", null, player);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 87 RequestTutorialQuestionMark";
	}
}

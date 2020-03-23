package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	private static final String _C__85_REQUESTTUTORIALLINKHTML = "[C] 85 RequestTutorialLinkHtml";
	String _bypass;
	
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
		return _C__85_REQUESTTUTORIALLINKHTML;
	}
}

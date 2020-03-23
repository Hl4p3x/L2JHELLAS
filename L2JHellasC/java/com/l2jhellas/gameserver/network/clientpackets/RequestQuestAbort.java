package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.instancemanager.QuestManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.network.serverpackets.QuestList;

public final class RequestQuestAbort extends L2GameClientPacket
{
	private static final String _C__64_REQUESTQUESTABORT = "[C] 64 RequestQuestAbort";
	
	private int _questId;
	
	@Override
	protected void readImpl()
	{
		_questId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final Quest qe = QuestManager.getInstance().getQuest(_questId);
		
		if (qe == null)
			return;
		
		final QuestState qs = activeChar.getQuestState(qe.getName());
		
		if (qs != null)
		{
			qs.exitQuest(true);
			activeChar.sendPacket(new QuestList(activeChar));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__64_REQUESTQUESTABORT;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

import java.util.List;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class QuestList extends L2GameServerPacket
{
	private static final String _S__98_QUESTLIST = "[S] 80 QuestList";
	
	private final List<Quest> _quests;
	private final L2PcInstance _activeChar;
	
	public QuestList(L2PcInstance player)
	{
		_activeChar = player;
		_quests = player.getAllQuests(true);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x80);
		writeH(_quests.size());
		for (Quest q : _quests)
		{
			writeD(q.getQuestId());
			QuestState qs = _activeChar.getQuestState(q.getName());
			if (qs == null)
			{
				writeD(0);
				continue;
			}
			
			int states = qs.getInt("__compltdStateFlags");
			if (states != 0)
				writeD(states);
			else
				writeD(qs.getInt("cond"));
		}
	}
	
	@Override
	public String getType()
	{
		return _S__98_QUESTLIST;
	}
}
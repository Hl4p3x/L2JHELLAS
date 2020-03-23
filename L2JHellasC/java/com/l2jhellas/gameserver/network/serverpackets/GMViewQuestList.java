package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class GMViewQuestList extends L2GameServerPacket
{
	private static final String _S__AC_GMVIEWQUESTLIST = "[S] ac GMViewQuestList";
	
	private final L2PcInstance _activeChar;
	
	public GMViewQuestList(L2PcInstance cha)
	{
		_activeChar = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x93);
		writeS(_activeChar.getName());
		
		Quest[] questList = _activeChar.getAllActiveQuests(true);
		
		if (questList.length == 0)
		{
			writeC(0);
			writeH(0);
			writeH(0);
			return;
		}
		
		writeH(questList.length); // quest count
		
		for (Quest q : questList)
		{
			writeD(q.getQuestId());
			
			QuestState qs = _activeChar.getQuestState(q.getName());
			
			if (qs == null)
			{
				writeD(0);
				continue;
			}
			
			writeD(qs.getInt("cond")); // stage of quest progress
		}
	}
	
	@Override
	public String getType()
	{
		return _S__AC_GMVIEWQUESTLIST;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;

public class AllyInfo extends L2GameServerPacket
{
	private static final String _S__7A_FRIENDLIST = "[S] 7a AllyInfo";
	private static L2PcInstance _cha;
	
	public AllyInfo(L2PcInstance cha)
	{
		_cha = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.getAllyId() == 0)
		{
			_cha.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
			return;
		}
		
		// ======<AllyInfo>======
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_INFO_HEAD);
		_cha.sendPacket(sm);
		// ======<Ally Name>======
		sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_NAME_S1);
		sm.addString(_cha.getClan().getAllyName());
		_cha.sendPacket(sm);
		int online = 0;
		int count = 0;
		int clancount = 0;
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == _cha.getAllyId())
			{
				clancount++;
				online += clan.getOnlineMembers().length;
				count += clan.getMembers().length;
			}
		}
		// Connection
		sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
		sm.addString("" + online);
		sm.addString("" + count);
		_cha.sendPacket(sm);
		L2Clan leaderclan = ClanTable.getInstance().getClan(_cha.getAllyId());
		sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1);
		sm.addString(leaderclan.getName());
		sm.addString(leaderclan.getLeaderName());
		_cha.sendPacket(sm);
		// clan count
		sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1);
		sm.addString("" + clancount);
		_cha.sendPacket(sm);
		// clan information
		sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_HEAD);
		_cha.sendPacket(sm);
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == _cha.getAllyId())
			{
				// clan name
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_NAME_S1);
				sm.addString(clan.getName());
				_cha.sendPacket(sm);
				// clan leader name
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEADER_S1);
				sm.addString(clan.getLeaderName());
				_cha.sendPacket(sm);
				// clan level
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEVEL_S1);
				sm.addNumber(clan.getLevel());
				_cha.sendPacket(sm);
				// ---------
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_SEPARATOR);
				_cha.sendPacket(sm);
			}
		}
		sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_FOOT);
		_cha.sendPacket(sm);
	}
	
	@Override
	public String getType()
	{
		return _S__7A_FRIENDLIST;
	}
}
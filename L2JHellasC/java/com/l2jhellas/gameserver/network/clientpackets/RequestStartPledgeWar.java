package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestStartPledgeWar extends L2GameClientPacket
{
	private static final String _C__4D_REQUESTSTARTPLEDGEWAR = "[C] 4D RequestStartPledgewar";
	
	private String _pledgeName;
	private L2Clan _clan;
	private L2PcInstance player;
	
	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		player = getClient().getActiveChar();
		if (player == null)
			return;
		
		_clan = getClient().getActiveChar().getClan();
		if (_clan == null)
			return;
		
		if ((_clan.getLevel() < 3) || _clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			sm = null;
			return;
		}
		else if (!player.isClanLeader())
		{
			player.sendMessage("You can't declare war. You are not clan leader.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (clan == null)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if ((_clan.getAllyId() == clan.getAllyId()) && (_clan.getAllyId() != 0))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			sm = null;
			return;
		}
		// else if(clan.getLevel() < 3)
		else if (clan.getLevel() < 3 || clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			sm = null;
			return;
		}
		else if (_clan.isAtWarWith(clan.getClanId()))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS); // msg id 628
			sm.addString(clan.getName());
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			sm = null;
			return;
		}
		
		// _log.warning(classoura.class.getName() + ": RequestStartPledgeWar, leader: " + clan.getLeaderName() + " clan: "+ _clan.getName());
		
		// L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName());
		
		// if(leader == null)
		// return;
		
		// if(leader != null && leader.isOnline() == 0)
		// {
		// player.sendMessage("Clan leader isn't online.");
		// player.sendPacket(ActionFailed.STATIC_PACKET);
		// return;
		// }
		
		// if (leader.isProcessingRequest())
		// {
		// SystemMessage sm = SystemMessage.getSystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
		// sm.addString(leader.getName());
		// player.sendPacket(sm);
		// return;
		// }
		
		// if (leader.isTransactionInProgress())
		// {
		// SystemMessage sm = SystemMessage.getSystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
		// sm.addString(leader.getName());
		// player.sendPacket(sm);
		// return;
		// }
		
		// leader.setTransactionRequester(player);
		// player.setTransactionRequester(leader);
		// leader.sendPacket(new StartPledgeWar(_clan.getName(),player.getName()));
		
		ClanTable.getInstance().storeclanswars(player.getClanId(), clan.getClanId());
		for (L2PcInstance cha : L2World.getInstance().getAllPlayers().values())
		{
			if (cha.getClan() == player.getClan() || cha.getClan() == clan)
				cha.broadcastUserInfo();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__4D_REQUESTSTARTPLEDGEWAR;
	}
}
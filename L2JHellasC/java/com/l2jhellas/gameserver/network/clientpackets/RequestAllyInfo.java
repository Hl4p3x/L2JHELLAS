package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2ClanInfo;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AllyInfo;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;

public final class RequestAllyInfo extends L2GameClientPacket
{
	private static final String _C__8E_REQUESTALLYINFO = "[C] 8E RequestAllyInfo";
	
	@Override
	public void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		// Flood protect
		if (!FloodProtectors.performAction(getClient(),FloodAction.SERVER_BYPASS))
		{
			player.sendMessage("You are using it too fast.");
			return;
		}
		
		final int AllyId = player.getAllyId();
		if (AllyId > 0)
		{
			final AllyInfo info = new AllyInfo(AllyId);
			player.sendPacket(info);
			
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_INFO_HEAD));
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_NAME_S1).addString(info.getName()));
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1).addString(info.getLeaderC()).addString(info.getLeaderP()));		
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2).addNumber(info.getOnline()).addNumber(info.getTotal()));
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1).addNumber(info.getAllies().length));
			
			for (final L2ClanInfo aci : info.getAllies())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_HEAD));				
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_NAME_S1).addString(aci.getClan().getName()));				
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEADER_S1).addString(aci.getClan().getLeaderName()));
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEVEL_S1).addNumber(aci.getClan().getLevel()));			
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2).addNumber(aci.getOnline()).addNumber(aci.getTotal()));			
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_SEPARATOR));
			}
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_FOOT));
		}
		else
			player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
	}
	
	@Override
	public String getType()
	{
		return _C__8E_REQUESTALLYINFO;
	}
}
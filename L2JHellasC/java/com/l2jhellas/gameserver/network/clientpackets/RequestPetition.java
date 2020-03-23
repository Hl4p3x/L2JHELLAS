package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.instancemanager.PetitionManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetition extends L2GameClientPacket
{
	private static final String _C__7F_RequestPetition = "[C] 7F RequestPetition";
	
	private String _content;
	private int _type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		_content = readS();
		_type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (!AdminData.getInstance().isGmOnline(false))
		{
			activeChar.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
			return;
		}
		
		if (!PetitionManager.isPetitioningAllowed())
		{
			activeChar.sendPacket(SystemMessageId.GAME_CLIENT_UNABLE_TO_CONNECT_TO_PETITION_SERVER);
			return;
		}
		
		if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME);
			return;
		}
		
		if (PetitionManager.getInstance().getPendingPetitionCount() == Config.MAX_PETITIONS_PENDING)
		{
			activeChar.sendPacket(SystemMessageId.PETITION_SYSTEM_CURRENT_UNAVAILABLE);
			return;
		}
		
		int totalPetitions = PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar) + 1;
		
		if (totalPetitions > Config.MAX_PETITIONS_PER_PLAYER)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.WE_HAVE_RECEIVED_S1_PETITIONS_TODAY);
			sm.addNumber(totalPetitions);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (_content.length() > 255)
		{
			activeChar.sendPacket(SystemMessageId.PETITION_MAX_CHARS_255);
			return;
		}
		
		int petitionId = PetitionManager.getInstance().submitPetition(activeChar, _content, _type);
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1);
		sm.addNumber(petitionId);
		activeChar.sendPacket(sm);
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.SUBMITTED_YOU_S1_TH_PETITION_S2_LEFT);
		sm.addNumber(totalPetitions);
		sm.addNumber(Config.MAX_PETITIONS_PER_PLAYER - totalPetitions);
		activeChar.sendPacket(sm);
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PETITION_ON_WAITING_LIST);
		sm.addNumber(PetitionManager.getInstance().getPendingPetitionCount());
		activeChar.sendPacket(sm);
		sm = null;
	}
	
	@Override
	public String getType()
	{
		return _C__7F_RequestPetition;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.group.party.L2CommandChannel;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestExAcceptJoinMPCC extends L2GameClientPacket
{
	private static final String _C__D0_0E_REQUESTEXASKJOINMPCC = "[C] D0:0E RequestExAcceptJoinMPCC";
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		final L2PcInstance requestor = player.getActiveRequester();
		
		if (requestor == null)
			return;
		
		final L2Party PartyRequestor = requestor.getParty();
		
		if (PartyRequestor == null)
			return;
		
		final L2Party targetParty = player.getParty();
		
		if (targetParty == null)
			return;
		
		if (_response == 1)
		{
			L2CommandChannel channel = PartyRequestor.getCommandChannel();
			
			if (channel == null)
			{
				if (!L2CommandChannel.AuthCheck(requestor, true))
					return;
				channel = new L2CommandChannel(requestor);
			}
			else
				channel.addParty(targetParty);
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DECLINED_CHANNEL_INVITATION).addCharName(player));
		}
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__D0_0E_REQUESTEXASKJOINMPCC;
	}
}
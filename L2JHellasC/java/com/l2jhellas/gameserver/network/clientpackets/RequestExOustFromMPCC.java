package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.group.party.L2CommandChannel;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestExOustFromMPCC extends L2GameClientPacket
{
	private static final String _C__D0_0F_REQUESTEXOUSTFROMMPCC = "[C] D0:0F RequestExOustFromMPCC";
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		
		if (_name.isEmpty())
			return;
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2PcInstance target = L2World.getInstance().getPlayer(_name);
		
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			return;
		}
		
		if (activeChar.equals(target))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final L2Party reqpart = activeChar.getParty();
		final L2Party targetParty = target.getParty();
		
		if (reqpart == null || targetParty == null)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final L2CommandChannel requestorChannel = reqpart.getCommandChannel();
		
		if (requestorChannel == null || !requestorChannel.isLeader(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (!requestorChannel.removeParty(targetParty))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		targetParty.getCommandChannel().broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.DISMISSED_FROM_COMMAND_CHANNEL));
		
		if (reqpart.isInCommandChannel())
			reqpart.getCommandChannel().broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL).addCharName(targetParty.getLeader()));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_0F_REQUESTEXOUSTFROMMPCC;
	}
}
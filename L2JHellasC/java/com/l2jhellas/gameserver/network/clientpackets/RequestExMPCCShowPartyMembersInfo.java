package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ExMPCCShowPartyMemberInfo;

public final class RequestExMPCCShowPartyMembersInfo extends L2GameClientPacket
{
	private static final String _C__D0_26_REQUESTMPCCSHOWPARTYMEMBERINFO = "[C] D0:26 RequestExMPCCShowPartyMembersInfo";
	private int _pLeaderId;
	
	@Override
	protected void readImpl()
	{
		_pLeaderId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2PcInstance player = L2World.getInstance().getPlayer(_pLeaderId);
		
		if (player != null && player.isInParty())
			activeChar.sendPacket(new ExMPCCShowPartyMemberInfo(player.getParty()));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_26_REQUESTMPCCSHOWPARTYMEMBERINFO;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.PledgeReceivePowerInfo;

public final class RequestPledgeMemberPowerInfo extends L2GameClientPacket
{
	private static final String _C__D0_1B_REQUESTPLEDGEMEMBERPOWERINFO = "[C] D0:1B RequestPledgeMemberPowerInfo";
	private int _unk1;
	private String _player;
	
	@Override
	protected void readImpl()
	{
		_unk1 = readD();
		_player = readS();
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.config(RequestPledgeMemberPowerInfo.class.getName() + ": C5: RequestPledgeMemberPowerInfo d:" + _unk1);
			_log.config(RequestPledgeMemberPowerInfo.class.getName() + ": C5: RequestPledgeMemberPowerInfo S:" + _player);
		}
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		// do we need powers to do that??
		L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		L2ClanMember member = clan.getClanMember(_player);
		if (member == null)
			return;
		activeChar.sendPacket(new PledgeReceivePowerInfo(member));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_1B_REQUESTPLEDGEMEMBERPOWERINFO;
	}
}
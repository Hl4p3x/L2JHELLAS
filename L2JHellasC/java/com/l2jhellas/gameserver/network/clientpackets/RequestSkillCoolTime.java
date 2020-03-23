package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.L2GameClient;
import com.l2jhellas.gameserver.network.serverpackets.SkillCoolTime;

public class RequestSkillCoolTime extends L2GameClientPacket
{
	L2GameClient _client;
	
	@Override
	public void readImpl()
	{
		_client = getClient();
	}
	
	@Override
	public void runImpl()
	{
		L2PcInstance pl = _client.getActiveChar();
		if (pl != null)
			pl.sendPacket(new SkillCoolTime(pl));
	}
	
	@Override
	public String getType()
	{
		return "[C] 0xa6 RequestSkillCoolTime";
	}
}
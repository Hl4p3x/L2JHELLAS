package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestSkillList extends L2GameClientPacket
{
	private static final String _C__3F_REQUESTSKILLLIST = "[C] 3F RequestSkillList";
	
	@Override
	protected void readImpl()
	{
		// this is just a trigger packet. it has no content
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance cha = getClient().getActiveChar();
		
		if (cha == null)
			return;
		
		cha.sendSkillList();
	}
	
	@Override
	public String getType()
	{
		return _C__3F_REQUESTSKILLLIST;
	}
}
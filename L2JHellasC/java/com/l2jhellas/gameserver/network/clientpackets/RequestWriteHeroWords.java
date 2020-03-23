package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Hero;

public final class RequestWriteHeroWords extends L2GameClientPacket
{
	private static final String _C__FE_0C_REQUESTWRITEHEROWORDS = "[C] D0:0C RequestWriteHeroWords";
	private String _heroWords;
	
	@Override
	protected void readImpl()
	{
		_heroWords = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null || !player.isHero())
			return;
		
		if (_heroWords == null || _heroWords.length() > 300)
			return;
		
		Hero.getInstance().setHeroMessage(player, _heroWords);
	}
	
	@Override
	public String getType()
	{
		return _C__FE_0C_REQUESTWRITEHEROWORDS;
	}
}
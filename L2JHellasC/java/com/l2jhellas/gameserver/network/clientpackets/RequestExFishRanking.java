package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;

public final class RequestExFishRanking extends L2GameClientPacket
{
	private static final String _C__D0_1F_REQUESTEXFISHRANKING = "[C] D0:1F RequestExFishRanking";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		// TODO
		if (Config.DEBUG)
			_log.config(RequestExFishRanking.class.getName() + ": C5: RequestExFishRanking");
	}
	
	@Override
	public String getType()
	{
		return _C__D0_1F_REQUESTEXFISHRANKING;
	}
}
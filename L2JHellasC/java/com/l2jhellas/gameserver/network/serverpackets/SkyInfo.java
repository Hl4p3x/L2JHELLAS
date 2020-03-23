package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.SevenSigns;

public class SkyInfo extends L2GameServerPacket
{
	public static final SkyInfo REGULAR_SKY_PACKET = new SkyInfo(256);
	public static final SkyInfo DUSK_SKY_PACKET = new SkyInfo(257);
	public static final SkyInfo DAWN_SKY_PACKET = new SkyInfo(258);
	public static final SkyInfo RED_SKY_PACKET = new SkyInfo(259);
	
	private final int _state;
	
	public static SkyInfo sendSky()
	{
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			final int CW = SevenSigns.getInstance().getCabalHighestScore();
			if (CW == SevenSigns.CABAL_DAWN)
				return DAWN_SKY_PACKET;
			
			if (CW == SevenSigns.CABAL_DUSK)
				return DUSK_SKY_PACKET;
		}
		return REGULAR_SKY_PACKET;
	}
	
	private SkyInfo(int state)
	{
		_state = state;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xf8);
		writeH(_state);
	}
}
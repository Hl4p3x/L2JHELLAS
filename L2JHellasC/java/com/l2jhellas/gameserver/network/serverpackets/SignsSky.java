package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.SevenSigns;

public class SignsSky extends L2GameServerPacket
{
	private static final String _S__F8_SignsSky = "[S] F8 SignsSky";
	
	private static int _state = 0;
	
	public SignsSky()
	{
		int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		
		if (SevenSigns.getInstance().isSealValidationPeriod())
			if (compWinner == SevenSigns.CABAL_DAWN)
				_state = 2;
			else if (compWinner == SevenSigns.CABAL_DUSK)
				_state = 1;
	}
	
	public SignsSky(int state)
	{
		_state = state;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xf8);
		
		if (_state == 2) // Dawn Sky
			writeH(258);
		else if (_state == 1) // Dusk Sky
			writeH(257);
		// else
		// writeH(256);
	}
	
	@Override
	public String getType()
	{
		return _S__F8_SignsSky;
	}
}
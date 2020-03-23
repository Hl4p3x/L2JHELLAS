package com.l2jhellas.loginserver.serverpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;

public final class GGAuth extends L2LoginServerPacket
{
	static final Logger _log = Logger.getLogger(GGAuth.class.getName());
	public static final int SKIP_GG_AUTH_REQUEST = 0x0b;
	
	private final int _response;
	
	public GGAuth(int response)
	{
		_response = response;
		if (Config.DEBUG)
		{
			_log.warning(GGAuth.class.getName() + ": Reason Hex: " + (Integer.toHexString(response)));
		}
	}
	
	@Override
	protected void write()
	{
		writeC(0x0b);
		writeD(_response);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
	}
}
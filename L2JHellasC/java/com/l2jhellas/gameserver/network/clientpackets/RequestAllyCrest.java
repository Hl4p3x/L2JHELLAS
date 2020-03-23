package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.cache.CrestCache;
import com.l2jhellas.gameserver.cache.CrestCache.CrestType;
import com.l2jhellas.gameserver.network.serverpackets.AllyCrest;

public final class RequestAllyCrest extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestAllyCrest.class.getName());
	private static final String _C__88_REQUESTALLYCREST = "[C] 88 RequestAllyCrest";
	
	private int _crestId;
	
	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
			_log.fine("allycrestid " + _crestId + " requested");
		
		final byte[] data = CrestCache.getCrest(CrestType.ALLY, _crestId);
		
		if (data != null)
			sendPacket(new AllyCrest(_crestId, data));
	}
	
	@Override
	public String getType()
	{
		return _C__88_REQUESTALLYCREST;
	}
}
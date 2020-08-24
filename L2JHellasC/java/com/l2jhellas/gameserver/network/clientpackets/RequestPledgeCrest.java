package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.cache.CrestCache;
import com.l2jhellas.gameserver.cache.CrestCache.CrestType;
import com.l2jhellas.gameserver.network.serverpackets.PledgeCrest;

public final class RequestPledgeCrest extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestPledgeCrest.class.getName());
	private static final String _C__68_REQUESTPLEDGECREST = "[C] 68 RequestPledgeCrest";
	
	private int _crestId;
	
	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (_crestId == 0)
		{
			return;
		}
		if (Config.DEBUG)
		{
			_log.fine("crestid " + _crestId + " requested");
		}
		
		byte[] data = CrestCache.getCrest(CrestType.PLEDGE, _crestId);
		
		if (data != null)
		{
			PledgeCrest pc = new PledgeCrest(_crestId, data);
			sendPacket(pc);
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.warning(RequestPledgeCrest.class.getName() + ": crest is missing:" + _crestId);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__68_REQUESTPLEDGECREST;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
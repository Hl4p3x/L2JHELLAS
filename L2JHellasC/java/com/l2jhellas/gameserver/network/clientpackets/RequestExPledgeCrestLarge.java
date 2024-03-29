package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.cache.CrestCache;
import com.l2jhellas.gameserver.cache.CrestCache.CrestType;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ExPledgeCrestLarge;

public final class RequestExPledgeCrestLarge extends L2GameClientPacket
{
	private static final String _C__D0_10_REQUESTEXPLEDGECRESTLARGE = "[C] D0:10 RequestExPledgeCrestLarge";
	private int _crestId;
	
	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}
	
	@Override
	protected void runImpl()
	{
        final L2PcInstance activeChar = getClient().getActiveChar();
      
        if (activeChar == null || activeChar.getClanId() <= 0)
            return;
        
		byte[] data = CrestCache.getCrest(CrestType.PLEDGE_LARGE, _crestId);
		if (data != null)
			activeChar.broadcastPacket(new ExPledgeCrestLarge(activeChar.getClanId(),_crestId, data));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_10_REQUESTEXPLEDGECRESTLARGE;
	}
}
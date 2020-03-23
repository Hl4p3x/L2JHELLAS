package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;

public final class RequestPCCafeCouponUse extends L2GameClientPacket
{
	private static final String _C__D0_20_REQUESTPCCAFECOUPONUSE = "[C] D0:20 RequestPCCafeCouponUse";
	private String _str;
	
	@Override
	protected void readImpl()
	{
		_str = readS();
	}
	
	@Override
	protected void runImpl()
	{
		// @TODO
		if (Config.DEBUG)
			_log.config(RequestPCCafeCouponUse.class.getName() + ": C5: RequestPCCafeCouponUse: S: " + _str);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_20_REQUESTPCCAFECOUPONUSE;
	}
}
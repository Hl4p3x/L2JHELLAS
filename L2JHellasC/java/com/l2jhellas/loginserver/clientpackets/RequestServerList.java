package com.l2jhellas.loginserver.clientpackets;

import com.l2jhellas.loginserver.serverpackets.LoginFail.LoginFailReason;
import com.l2jhellas.loginserver.serverpackets.ServerList;

public class RequestServerList extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _data3;
	
	public int getSessionKey1()
	{
		return _skey1;
	}
	
	public int getSessionKey2()
	{
		return _skey2;
	}
	
	public int getData3()
	{
		return _data3;
	}
	
	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 8)
		{
			_skey1 = readD(); // loginOk 1
			_skey2 = readD(); // loginOk 2
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		if (getClient().getSessionKey().checkLoginPair(_skey1, _skey2))
			getClient().sendPacket(new ServerList(getClient()));
		else
			getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
	}
}
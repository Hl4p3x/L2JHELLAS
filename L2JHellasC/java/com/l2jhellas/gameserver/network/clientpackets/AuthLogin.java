package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.LoginServerThread;
import com.l2jhellas.gameserver.LoginServerThread.SessionKey;
import com.l2jhellas.gameserver.network.L2GameClient;

public final class AuthLogin extends L2GameClientPacket
{
	private static final String _C__08_AUTHLOGIN = "[C] 08 AuthLogin";
	
	// loginName + keys must match what the loginserver used.
	private String _loginName;
	
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	
	@Override
	protected void readImpl()
	{
		_loginName = readS().toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2GameClient client = getClient();
		
		if (client == null)
			return;
		
		if (client.getAccountName() == null)
		{
			getClient().setAccountName(_loginName);
			getClient().setSessionId(new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2));
			LoginServerThread.getInstance().addWaitingClientAndSendRequest(_loginName, client);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__08_AUTHLOGIN;
	}
}
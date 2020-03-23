package com.l2jhellas.loginserver.clientpackets;

import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import com.l2jhellas.Config;
import com.l2jhellas.loginserver.GameServerTable.GameServerInfo;
import com.l2jhellas.loginserver.L2LoginClient;
import com.l2jhellas.loginserver.L2LoginClient.LoginClientState;
import com.l2jhellas.loginserver.LoginController;
import com.l2jhellas.loginserver.LoginController.AuthLoginResult;
import com.l2jhellas.loginserver.serverpackets.AccountKicked;
import com.l2jhellas.loginserver.serverpackets.AccountKicked.AccountKickedReason;
import com.l2jhellas.loginserver.serverpackets.LoginFail.LoginFailReason;
import com.l2jhellas.loginserver.serverpackets.LoginOk;
import com.l2jhellas.loginserver.serverpackets.ServerList;

public class RequestAuthLogin extends L2LoginClientPacket
{
	private static Logger _log = Logger.getLogger(RequestAuthLogin.class.getName());
	
	private final byte[] _raw = new byte[128];
	
	private String _user;
	private String _password;
	private int _ncotp;
	
	public String getPassword()
	{
		return _password;
	}
	
	public String getUser()
	{
		return _user;
	}
	
	public int getOneTimePassword()
	{
		return _ncotp;
	}
	
	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 128)
		{
			readB(_raw);
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		byte[] decrypted = null;
		final L2LoginClient client = getClient();
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, getClient().getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80);
		}
		catch (GeneralSecurityException e)
		{
			_log.log(Level.INFO, "", e);
			return;
		}
		
		try
		{
			_user = new String(decrypted, 0x5E, 14).trim().toLowerCase();
			_password = new String(decrypted, 0x6C, 16).trim();
			_ncotp = decrypted[0x7c];
			_ncotp |= decrypted[0x7d] << 8;
			_ncotp |= decrypted[0x7e] << 16;
			_ncotp |= decrypted[0x7f] << 24;
		}
		catch (Exception e)
		{
			return;
		}
		
		final LoginController lc = LoginController.getInstance();
		AuthLoginResult result = null;
		result = lc.tryAuthLogin(_user, _password, client);
		if(result!=null)
		switch (result)
		{
			case AUTH_SUCCESS:
				client.setAccount(_user);
				client.setState(LoginClientState.AUTHED_LOGIN);
				client.setSessionKey(lc.assignSessionKeyToClient(_user, client));
				if (Config.SHOW_LICENCE)
					client.sendPacket(new LoginOk(getClient().getSessionKey()));
				else
					getClient().sendPacket(new ServerList(getClient()));
				break;
			
			case INVALID_PASSWORD:
				client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
				break;
			
			case ACCOUNT_BANNED:
				client.close(new AccountKicked(AccountKickedReason.REASON_PERMANENTLY_BANNED));
				break;
			
			case ALREADY_ON_LS:
				L2LoginClient oldClient;
				if ((oldClient = lc.getAuthedClient(_user)) != null)
				{
					// kick the other client
					oldClient.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					lc.removeAuthedLoginClient(_user);
				}
				// kick also current client
				client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
				break;
			
			case ALREADY_ON_GS:
				GameServerInfo gsi;
				if ((gsi = lc.getAccountOnGameServer(_user)) != null)
				{
					client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					// kick from there
					if (gsi.isAuthed())
						gsi.getGameServerThread().kickPlayer(_user);
				}
				break;
		}
	}
}

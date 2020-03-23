package com.l2jhellas.gameserver.network.gameserverpackets;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Logger;

import javax.crypto.Cipher;

public class BlowFishKey extends GameServerBasePacket
{
	private static Logger _log = Logger.getLogger(BlowFishKey.class.getName());
	private byte[] encrypteds;
	
	public BlowFishKey(byte[] blowfishKey, RSAPublicKey publicKey)
	{
		writeC(0x00);
		encrypteds = null;
		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			encrypteds = rsaCipher.doFinal(blowfishKey);
		}
		catch (GeneralSecurityException e)
		{
			_log.severe("Error While encrypting blowfish key for transmision (Crypt error)");
			e.printStackTrace();
		}
		writeD(encrypteds.length);
		writeB(encrypteds);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
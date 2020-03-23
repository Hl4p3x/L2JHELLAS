package com.l2jhellas.gameserver;

import java.nio.ByteBuffer;

public class Crypt
{
	private final byte[] _key = new byte[16];
	private boolean _isEnabled;
	
	public void setKey(byte[] key)
	{
		System.arraycopy(key, 0, _key, 0, key.length);
		_isEnabled = true;
	}
	
	public void decrypt(ByteBuffer buf)
	{
		if (!_isEnabled)
			return;
		
		final int sz = buf.remaining();
		int temp = 0;
		for (int i = 0; i < sz; i++)
		{
			int temp2 = buf.get(i);
			buf.put(i, (byte) (temp2 ^ _key[i & 15] ^ temp));
			temp = temp2;
		}
		
		int old = _key[8] & 0xff;
		old |= _key[9] << 8 & 0xff00;
		old |= _key[10] << 0x10 & 0xff0000;
		old |= _key[11] << 0x18 & 0xff000000;
		
		old += sz;
		
		_key[8] = (byte) (old & 0xff);
		_key[9] = (byte) (old >> 0x08 & 0xff);
		_key[10] = (byte) (old >> 0x10 & 0xff);
		_key[11] = (byte) (old >> 0x18 & 0xff);
	}
	
	public void encrypt(ByteBuffer buf)
	{
		if (!_isEnabled)
			return;
		
		int temp = 0;
		final int sz = buf.remaining();
		for (int i = 0; i < sz; i++)
		{
			int temp2 = buf.get(i);
			temp = temp2 ^ _key[i & 15] ^ temp;
			buf.put(i, (byte) temp);
		}
		
		int old = _key[8] & 0xff;
		old |= _key[9] << 8 & 0xff00;
		old |= _key[10] << 0x10 & 0xff0000;
		old |= _key[11] << 0x18 & 0xff000000;
		
		old += sz;
		
		_key[8] = (byte) (old & 0xff);
		_key[9] = (byte) (old >> 0x08 & 0xff);
		_key[10] = (byte) (old >> 0x10 & 0xff);
		_key[11] = (byte) (old >> 0x18 & 0xff);
	}
}
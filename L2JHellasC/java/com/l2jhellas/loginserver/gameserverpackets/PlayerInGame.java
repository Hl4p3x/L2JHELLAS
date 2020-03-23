package com.l2jhellas.loginserver.gameserverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.loginserver.clientpackets.ClientBasePacket;

public class PlayerInGame extends ClientBasePacket
{
	private final List<String> _accounts;
	
	public PlayerInGame(byte[] decrypt)
	{
		super(decrypt);
		_accounts = new ArrayList<>();
		int size = readH();
		for (int i = 0; i < size; i++)
		{
			_accounts.add(readS());
		}
	}
	
	public List<String> getAccounts()
	{
		return _accounts;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.util.Util;

public class RequestRecipeShopMessageSet extends L2GameClientPacket
{
	private static final String _C__B1_RequestRecipeShopMessageSet = "[C] b1 RequestRecipeShopMessageSet";
	
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		if (_name != null && _name.length() > 29)
		{
			Util.handleIllegalPlayerAction(player, player.getName() + " tried to overflow recipe shop message", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (player.getCreateList() != null)
		{
			player.getCreateList().setStoreName(_name);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__B1_RequestRecipeShopMessageSet;
	}
}
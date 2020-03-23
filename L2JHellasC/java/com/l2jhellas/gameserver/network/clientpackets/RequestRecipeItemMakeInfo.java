package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.RecipeItemMakeInfo;

public final class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
	private static final String _C__AE_REQUESTRECIPEITEMMAKEINFO = "[C] AE RequestRecipeItemMakeInfo";
	
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if(activeChar == null)
			return;
		
		activeChar.sendPacket(new RecipeItemMakeInfo(_id, activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__AE_REQUESTRECIPEITEMMAKEINFO;
	}
}
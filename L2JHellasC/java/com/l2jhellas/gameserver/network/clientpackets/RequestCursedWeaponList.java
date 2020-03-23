package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.network.serverpackets.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket
{
	private static final String _C__D0_22_REQUESTCURSEDWEAPONLIST = "[C] D0:22 RequestCursedWeaponList";
	
	@Override
	protected void readImpl()
	{
		// nothing to read it's just a trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2Character activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		activeChar.sendPacket(new ExCursedWeaponList(CursedWeaponsManager.getInstance().getCursedWeaponsIds()));
		
	}
	
	@Override
	public String getType()
	{
		return _C__D0_22_REQUESTCURSEDWEAPONLIST;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jhellas.gameserver.model.CursedWeapon;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.serverpackets.ExCursedWeaponLocation;
import com.l2jhellas.gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;

public final class RequestCursedWeaponLocation extends L2GameClientPacket
{
	private static final String _C__D0_23_REQUESTCURSEDWEAPONLOCATION = "[C] D0:23 RequestCursedWeaponLocation";
	
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
		
		final List<CursedWeaponInfo> list = new ArrayList<>();
		for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			if (!cw.isActive())
				continue;
			
			final Location pos = cw.getWorldPosition();
			
			if (pos != null)
				list.add(new CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
		}
		
		// send the ExCursedWeaponLocation
		if (!list.isEmpty())
		{
			activeChar.sendPacket(new ExCursedWeaponLocation(list));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_23_REQUESTCURSEDWEAPONLOCATION;
	}
}
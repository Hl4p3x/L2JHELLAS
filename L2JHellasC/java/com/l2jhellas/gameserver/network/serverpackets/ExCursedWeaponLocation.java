package com.l2jhellas.gameserver.network.serverpackets;

import java.util.List;

import com.l2jhellas.gameserver.model.actor.position.Location;

public class ExCursedWeaponLocation extends L2GameServerPacket
{
	private static final String _S__FE_46_EXCURSEDWEAPONLOCATION = "[S] FE:46 ExCursedWeaponLocation";
	private final List<CursedWeaponInfo> _cursedWeaponInfo;
	
	public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo)
	{
		_cursedWeaponInfo = cursedWeaponInfo;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x46);
		
		if (!_cursedWeaponInfo.isEmpty())
		{
			writeD(_cursedWeaponInfo.size());
			for (CursedWeaponInfo w : _cursedWeaponInfo)
			{
				writeD(w.id);
				writeD(w.activated);
				
				writeD(w.pos.getX());
				writeD(w.pos.getY());
				writeD(w.pos.getZ());
			}
		}
		else
		{
			writeD(0);
			writeD(0);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_46_EXCURSEDWEAPONLOCATION;
	}
	
	public static class CursedWeaponInfo
	{
		public Location pos;
		public int id;
		public int activated; // 0 - not activated ? 1 - activated
		
		public CursedWeaponInfo(Location pos2, int ID, int status)
		{
			pos = pos2;
			id = ID;
			activated = status;
		}
	}
}
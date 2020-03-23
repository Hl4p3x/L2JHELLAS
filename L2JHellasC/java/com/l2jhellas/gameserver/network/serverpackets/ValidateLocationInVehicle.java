package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class ValidateLocationInVehicle extends L2GameServerPacket
{
	private static final String _S__73_ValidateLocationInVehicle = "[S] 73 ValidateLocationInVehicle";
	private final L2Character _activeChar;
	private final int _boatId;
	
	public ValidateLocationInVehicle(L2Character player, int boatId)
	{
		_activeChar = player;
		_boatId = boatId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x73);
		writeD(_activeChar.getObjectId());
		writeD(_boatId);
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		writeD(_activeChar.getHeading());
	}
	
	@Override
	public String getType()
	{
		return _S__73_ValidateLocationInVehicle;
	}
}
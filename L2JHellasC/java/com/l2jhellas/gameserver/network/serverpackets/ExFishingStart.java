package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.position.Location;

public class ExFishingStart extends L2GameServerPacket
{
	private static final String _S__FE_13_EXFISHINGSTART = "[S] FE:13 ExFishingStart";
	private final L2Character _activeChar;
	private final Location _loc;
	private final int _fishType;
	private final boolean _isNightLure;
	
	public ExFishingStart(L2Character character, int fishType, Location loc, boolean isNightLure)
	{
		_activeChar = character;
		_fishType = fishType;
		_loc = loc;
		_isNightLure = isNightLure;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x13);
		writeD(_activeChar.getObjectId());
		writeD(_fishType); // fish type
		writeD(_loc.getX()); // x position
		writeD(_loc.getY()); // y position
		writeD(_loc.getZ()); // z position
		writeC(_isNightLure ? 0x01 : 0x00); // night lure
		writeC(Config.ALLOWFISHING ? 0x01 : 0x00); // show fish rank result button
		
	}
	
	@Override
	public String getType()
	{
		return _S__FE_13_EXFISHINGSTART;
	}
}
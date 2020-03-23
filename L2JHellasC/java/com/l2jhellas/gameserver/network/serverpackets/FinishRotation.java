package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class FinishRotation extends L2GameServerPacket
{
	private static final String _S__78_FINISHROTATION = "[S] 63 FinishRotation";
	private final int _heading;
	private final int _charObjId;
	
	public FinishRotation(L2Character cha)
	{
		_charObjId = cha.getObjectId();
		_heading = cha.getHeading();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x63);
		writeD(_charObjId);
		writeD(_heading);
	}
	
	@Override
	public String getType()
	{
		return _S__78_FINISHROTATION;
	}
}
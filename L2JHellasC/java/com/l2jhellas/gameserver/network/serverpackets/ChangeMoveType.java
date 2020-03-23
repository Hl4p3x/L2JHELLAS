package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.actor.L2Character;

public class ChangeMoveType extends L2GameServerPacket
{
	private static final String _S__3E_CHANGEMOVETYPE = "[S] 3E ChangeMoveType";
	
	private final int _charObjId;
	private final int _running;
	private final int _moveType;
	
	public ChangeMoveType(L2Character character)
	{
		_charObjId = character.getObjectId();
		_running = (character.isRunning()) ? 1 : 0;
		_moveType = (character.isInsideZone(ZoneId.WATER) ? 1 : character.isFlying() ? 2 : 0);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2e);
		writeD(_charObjId);
		writeD(_running);
		writeD(_moveType);
	}
	
	@Override
	public String getType()
	{
		return _S__3E_CHANGEMOVETYPE;
	}
}
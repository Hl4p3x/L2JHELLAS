package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ChairSit extends L2GameServerPacket
{
	private static final String _S__e1_CHAIRSIT = "[S] e1 ChairSit";
	
	private final L2PcInstance _activeChar;
	private final int _staticObjectId;
	
	public ChairSit(L2PcInstance player, int staticObjectId)
	{
		_activeChar = player;
		_staticObjectId = staticObjectId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe1);
		writeD(_activeChar.getObjectId());
		writeD(_staticObjectId);
	}
	
	@Override
	public String getType()
	{
		return _S__e1_CHAIRSIT;
	}
}
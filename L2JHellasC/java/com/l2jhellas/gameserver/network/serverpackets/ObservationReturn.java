package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ObservationReturn extends L2GameServerPacket
{
	// ddSS
	private static final String _S__E0_OBSERVRETURN = "[S] E0 ObservationReturn";
	private final L2PcInstance _activeChar;
	
	public ObservationReturn(L2PcInstance observer)
	{
		_activeChar = observer;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe0);
		writeD(_activeChar.getObsX());
		writeD(_activeChar.getObsY());
		writeD(_activeChar.getObsZ());
	}
	
	@Override
	public String getType()
	{
		return _S__E0_OBSERVRETURN;
	}
}
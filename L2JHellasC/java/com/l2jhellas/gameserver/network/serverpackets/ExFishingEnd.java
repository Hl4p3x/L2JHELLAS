package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ExFishingEnd extends L2GameServerPacket
{
	private static final String _S__FE_14_EXFISHINGEND = "[S] FE:14 ExFishingEnd";
	private final boolean _win;
	L2Character _activeChar;
	
	public ExFishingEnd(boolean win, L2PcInstance character)
	{
		_win = win;
		_activeChar = character;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x14);
		writeD(_activeChar.getObjectId());
		writeC(_win ? 1 : 0);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_14_EXFISHINGEND;
	}
}
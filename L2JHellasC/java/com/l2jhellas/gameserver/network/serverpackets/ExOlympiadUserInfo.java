package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ExOlympiadUserInfo extends L2GameServerPacket
{
	// chcdSddddd
	private static final String _S__FE_29_OLYMPIADUSERINFO = "[S] FE:2C OlympiadUserInfo";
	private final L2PcInstance _activeChar;
	private final int _side;
	
	public ExOlympiadUserInfo(L2PcInstance player, int side)
	{
		_activeChar = player;
		_side = side;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_activeChar == null)
			return;
		
		writeC(0xfe);
		writeH(0x29);
		writeC(_side);
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getName());
		writeD(_activeChar.getClassId().getId());
		writeD((int) _activeChar.getCurrentHp());
		writeD(_activeChar.getMaxHp());
		writeD((int) _activeChar.getCurrentCp());
		writeD(_activeChar.getMaxCp());
	}
	
	@Override
	public String getType()
	{
		return _S__FE_29_OLYMPIADUSERINFO;
	}
}
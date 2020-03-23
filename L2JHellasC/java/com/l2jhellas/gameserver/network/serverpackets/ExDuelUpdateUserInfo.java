package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ExDuelUpdateUserInfo extends L2GameServerPacket
{
	private static final String _S__FE_4F_EXDUELUPDATEUSERINFO = "[S] FE:4F ExDuelUpdateUserInfo";
	private final L2PcInstance _activeChar;
	
	public ExDuelUpdateUserInfo(L2PcInstance cha)
	{
		_activeChar = cha;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4f);
		writeS(_activeChar.getName());
		writeD(_activeChar.getObjectId());
		writeD(_activeChar.getClassId().getId());
		writeD(_activeChar.getLevel());
		writeD((int) _activeChar.getCurrentHp());
		writeD(_activeChar.getMaxHp());
		writeD((int) _activeChar.getCurrentMp());
		writeD(_activeChar.getMaxMp());
		writeD((int) _activeChar.getCurrentCp());
		writeD(_activeChar.getMaxCp());
	}
	
	@Override
	public String getType()
	{
		return _S__FE_4F_EXDUELUPDATEUSERINFO;
	}
}
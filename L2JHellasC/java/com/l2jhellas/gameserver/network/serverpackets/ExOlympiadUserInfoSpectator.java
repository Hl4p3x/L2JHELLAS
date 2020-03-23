package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ExOlympiadUserInfoSpectator extends L2GameServerPacket
{
	// chcdSddddd
	private static final String _S__FE_29_OLYMPIADUSERINFOSPECTATOR = "[S] FE:29 OlympiadUserInfoSpectator";
	private static int _side;
	private static L2PcInstance _player;
	
	public ExOlympiadUserInfoSpectator(L2PcInstance player, int side)
	{
		_player = player;
		_side = side;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x29);
		writeC(_side);
		writeD(_player.getObjectId());
		writeS(_player.getName());
		writeD(_player.getClassId().getId());
		writeD((int) _player.getCurrentHp());
		writeD(_player.getMaxHp());
		writeD((int) _player.getCurrentCp());
		writeD(_player.getMaxCp());
	}
	
	@Override
	public String getType()
	{
		return _S__FE_29_OLYMPIADUSERINFOSPECTATOR;
	}
}
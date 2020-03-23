package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.enums.player.PlayerCreateFailReason;

public class CharCreateFail extends L2GameServerPacket
{
	private static final String _S__26_CHARCREATEFAIL = "[S] 1a CharCreateFail";
	
	private final PlayerCreateFailReason _reason;
	
	public CharCreateFail(PlayerCreateFailReason reason)
	{
		_reason = reason;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x1a);
		writeD(_reason.getReason());
	}
	
	@Override
	public String getType()
	{
		return _S__26_CHARCREATEFAIL;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

public class CharDeleteFail extends L2GameServerPacket
{
	private static final String _S__34_CHARDELETEFAIL = "[S] 24 CharDeleteFail";
	
	public static final int REASON_DELETION_FAILED = 0x01;
	public static final int REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = 0x02;
	public static final int REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = 0x03;
	
	private final int _error;
	
	public CharDeleteFail(int errorCode)
	{
		_error = errorCode;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x24);
		writeD(_error);
	}
	
	@Override
	public String getType()
	{
		return _S__34_CHARDELETEFAIL;
	}
}
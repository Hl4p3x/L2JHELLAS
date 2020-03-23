package com.l2jhellas.gameserver.network.serverpackets;

public class ExDuelEnd extends L2GameServerPacket
{
	private static final String _S__FE_4E_EXDUELEND = "[S] FE:4E ExDuelEnd";
	private final int _isPartyDuel;
	
	public ExDuelEnd(int isPartyDuel)
	{
		_isPartyDuel = isPartyDuel;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4e);
		writeD(_isPartyDuel);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_4E_EXDUELEND;
	}
}
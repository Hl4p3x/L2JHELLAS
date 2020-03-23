package com.l2jhellas.gameserver.network.serverpackets;

public class ExDuelReady extends L2GameServerPacket
{
	private static final String _S__FE_4C_EXDUELREADY = "[S] FE:4C ExDuelReady";
	private final int _isPartyDuel;
	
	public ExDuelReady(int isPartyDuel)
	{
		_isPartyDuel = isPartyDuel;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4c);
		writeD(_isPartyDuel);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_4C_EXDUELREADY;
	}
}
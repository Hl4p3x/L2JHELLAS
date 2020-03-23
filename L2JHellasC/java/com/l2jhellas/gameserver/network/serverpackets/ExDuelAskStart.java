package com.l2jhellas.gameserver.network.serverpackets;

public class ExDuelAskStart extends L2GameServerPacket
{
	private static final String _S__FE_4B_EXDUELASKSTART = "[S] FE:4B ExDuelAskStart";
	
	private final String _requestorName;
	private final int _partyDuel;
	
	public ExDuelAskStart(String requestor, int partyDuel)
	{
		_requestorName = requestor;
		_partyDuel = partyDuel;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4b);
		
		writeS(_requestorName);
		writeD(_partyDuel);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_4B_EXDUELASKSTART;
	}
}
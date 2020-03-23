package com.l2jhellas.gameserver.network.serverpackets;

public class ExAskJoinPartyRoom extends L2GameServerPacket
{
	private static final String _S__FE_34_EXASKJOINPARTYROOM = "[S] FE:34 ExAskJoinPartyRoom";
	private final String _charName;
	
	public ExAskJoinPartyRoom(String charName)
	{
		_charName = charName;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x34);
		writeS(_charName);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_34_EXASKJOINPARTYROOM;
	}
}
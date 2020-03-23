package com.l2jhellas.gameserver.network.serverpackets;

public class SendTradeRequest extends L2GameServerPacket
{
	private static final String _S__73_SENDTRADEREQUEST = "[S] 5e SendTradeRequest";
	private final int _senderID;
	
	public SendTradeRequest(int senderID)
	{
		_senderID = senderID;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x5e);
		writeD(_senderID);
	}
	
	@Override
	public String getType()
	{
		return _S__73_SENDTRADEREQUEST;
	}
}
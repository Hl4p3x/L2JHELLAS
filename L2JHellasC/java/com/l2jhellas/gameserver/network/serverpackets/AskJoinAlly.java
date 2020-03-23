package com.l2jhellas.gameserver.network.serverpackets;

public class AskJoinAlly extends L2GameServerPacket
{
	private static final String _S__A8_ASKJOINALLY_0XA8 = "[S] a8 AskJoinAlly 0xa8";
	
	private final String _requestorName;
	private final int _requestorObjId;
	
	public AskJoinAlly(int requestorObjId, String requestorName)
	{
		_requestorName = requestorName;
		_requestorObjId = requestorObjId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xa8);
		writeD(_requestorObjId);
		writeS(_requestorName);
	}
	
	@Override
	public String getType()
	{
		return _S__A8_ASKJOINALLY_0XA8;
	}
}
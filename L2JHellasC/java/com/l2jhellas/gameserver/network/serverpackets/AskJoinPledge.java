package com.l2jhellas.gameserver.network.serverpackets;

public class AskJoinPledge extends L2GameServerPacket
{
	private static final String _S__44_ASKJOINPLEDGE = "[S] 32 AskJoinPledge";
	
	private final int _requestorObjId;
	private final String _pledgeName;
	
	public AskJoinPledge(int requestorObjId, String pledgeName)
	{
		_requestorObjId = requestorObjId;
		_pledgeName = pledgeName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x32);
		writeD(_requestorObjId);
		writeS(_pledgeName);
	}
	
	@Override
	public String getType()
	{
		return _S__44_ASKJOINPLEDGE;
	}
}
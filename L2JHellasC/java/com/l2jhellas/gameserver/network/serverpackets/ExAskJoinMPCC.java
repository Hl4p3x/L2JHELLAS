package com.l2jhellas.gameserver.network.serverpackets;

public class ExAskJoinMPCC extends L2GameServerPacket
{
	private static final String _S__FE_27_EXASKJOINMPCC = "[S] FE:27 ExAskJoinMPCC";
	
	private final String _requestorName;
	
	public ExAskJoinMPCC(String requestorName)
	{
		_requestorName = requestorName;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x27);
		writeS(_requestorName); // name of CCLeader
	}
	
	@Override
	public String getType()
	{
		return _S__FE_27_EXASKJOINMPCC;
	}
}
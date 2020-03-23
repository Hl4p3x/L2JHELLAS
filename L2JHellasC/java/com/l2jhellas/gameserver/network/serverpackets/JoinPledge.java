package com.l2jhellas.gameserver.network.serverpackets;

public class JoinPledge extends L2GameServerPacket
{
	private static final String _S__45_JOINPLEDGE = "[S] 33 JoinPledge";
	
	private final int _pledgeId;
	
	public JoinPledge(int pledgeId)
	{
		_pledgeId = pledgeId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x33);
		
		writeD(_pledgeId);
	}
	
	@Override
	public String getType()
	{
		return _S__45_JOINPLEDGE;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

public class AskJoinFriend extends L2GameServerPacket
{
	private static final String _S__7d_ASKJoinFriend_0X7d = "[S] 7d AskJoinFriend 0x7d";
	
	private final String _requestorName;
	
	// private int _itemDistribution;
	
	// public AskJoinFriend(String requestorName, int itemDistribution)
	public AskJoinFriend(String requestorName)
	{
		_requestorName = requestorName;
		// _itemDistribution = itemDistribution;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7d);
		writeS(_requestorName);
		writeD(0);
	}
	
	@Override
	public String getType()
	{
		return _S__7d_ASKJoinFriend_0X7d;
	}
}
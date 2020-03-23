package com.l2jhellas.gameserver.network.serverpackets;

public class JoinParty extends L2GameServerPacket
{
	private static final String _S__4C_JOINPARTY = "[S] 3a JoinParty";
	
	private final int _response;
	
	public JoinParty(int response)
	{
		_response = response;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x3a);
		
		writeD(_response);
	}
	
	@Override
	public String getType()
	{
		return _S__4C_JOINPARTY;
	}
}
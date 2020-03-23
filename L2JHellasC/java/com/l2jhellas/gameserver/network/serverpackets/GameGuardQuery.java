package com.l2jhellas.gameserver.network.serverpackets;

public class GameGuardQuery extends L2GameServerPacket
{
	private static final String _S__F9_GAMEGUARDQUERY = "[S] F9 GameGuardQuery";
	
	public GameGuardQuery()
	{
		
	}
	
	@Override
	public void runImpl()
	{
		// Lets make user as gg-unauthorized We will set him as ggOK after reply from client or kick
		getClient().setGameGuardOk(false);
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xf9);
	}
	
	@Override
	public String getType()
	{
		return _S__F9_GAMEGUARDQUERY;
	}
}
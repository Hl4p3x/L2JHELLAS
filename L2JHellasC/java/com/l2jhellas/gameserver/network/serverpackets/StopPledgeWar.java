package com.l2jhellas.gameserver.network.serverpackets;

public class StopPledgeWar extends L2GameServerPacket
{
	private static final String _S__7f_STOPPLEDGEWAR = "[S] 67 StopPledgeWar";
	private final String _pledgeName;
	private final String _playerName;
	
	public StopPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_playerName = charName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x67);
		writeS(_pledgeName);
		writeS(_playerName);
	}
	
	@Override
	public String getType()
	{
		return _S__7f_STOPPLEDGEWAR;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

public class StartPledgeWar extends L2GameServerPacket
{
	private static final String _S__65_STARTPLEDGEWAR = "[S] 65 StartPledgeWar";
	private final String _pledgeName;
	private final String _playerName;
	
	public StartPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_playerName = charName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x65);
		writeS(_playerName);
		writeS(_pledgeName);
	}
	
	@Override
	public String getType()
	{
		return _S__65_STARTPLEDGEWAR;
	}
}
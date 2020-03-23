package com.l2jhellas.gameserver.network.serverpackets;

public class SurrenderPledgeWar extends L2GameServerPacket
{
	private static final String _S__81_SURRENDERPLEDGEWAR = "[S] 69 SurrenderPledgeWar";
	private final String _pledgeName;
	private final String _playerName;
	
	public SurrenderPledgeWar(String pledge, String charName)
	{
		_pledgeName = pledge;
		_playerName = charName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x69);
		writeS(_pledgeName);
		writeS(_playerName);
	}
	
	@Override
	public String getType()
	{
		return _S__81_SURRENDERPLEDGEWAR;
	}
}
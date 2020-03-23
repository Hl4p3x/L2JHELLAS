package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.enums.player.PartyLootType;

public class AskJoinParty extends L2GameServerPacket
{
	private static final String _S__4B_ASKJOINPARTY_0X4B = "[S] 39 AskJoinParty 0x4b";
	
	private final String _requestorName;
	private final PartyLootType _partyLootType;
	
	public AskJoinParty(String requestorName, PartyLootType partyLootType)
	{
		_requestorName = requestorName;
		_partyLootType = partyLootType;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x39);
		writeS(_requestorName);
		writeD(_partyLootType.getId());
	}
	
	@Override
	public String getType()
	{
		return _S__4B_ASKJOINPARTY_0X4B;
	}
}
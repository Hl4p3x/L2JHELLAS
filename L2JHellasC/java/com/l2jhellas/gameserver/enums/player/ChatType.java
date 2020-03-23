package com.l2jhellas.gameserver.enums.player;

public enum ChatType
{
	GENERAL(0),
	SHOUT(1),
	WHISPER(2),
	PARTY(3),
	CLAN(4),
	GM(5),
	PETITION_PLAYER(6),
	PETITION_GM(7),
	TRADE(8),
	ALLIANCE(9),
	ANNOUNCEMENT(10),
	BOAT(11),
	FRIEND(12),
	MSNCHAT(13),
	PARTYMATCH_ROOM(14),
	PARTYROOM_COMMANDER(15),
	PARTYROOM_ALL(16),
	HERO_VOICE(17),
	CRITICAL_ANNOUNCE(18);
	
	private final int _clientId;
	
	private ChatType(int clientId)
	{
		_clientId = clientId;
	}

	public int getClientId()
	{
		return _clientId;
	}

	public static ChatType findByClientId(int clientId)
	{
		for (ChatType ChatType : values())
		{
			if (ChatType.getClientId() == clientId)
			{
				return ChatType;
			}
		}
		return null;
	}
}
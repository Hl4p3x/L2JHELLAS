package com.l2jhellas.gameserver.enums.player;

public enum PartyLootType
{
	FINDERS_KEEPERS(0),
	RANDOM(1),
	RANDOM_INCLUDING_SPOIL(2),
	BY_TURN(3),
	BY_TURN_INCLUDING_SPOIL(4);
	
	private final int _id;

	private PartyLootType(int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public static PartyLootType GetById(int id)
	{
		for (PartyLootType partyDistributionType : values())
		{
			if (partyDistributionType.getId() == id)
				return partyDistributionType;
		}
		return null;
	}
}

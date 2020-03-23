package com.l2jhellas.gameserver.enums.items;

/**
 * @author AbsolutePower
 */
public enum CrownList
{
	CROWN_Of_THE_LORD(6841,-1),
	CIRCLET_OF_INNADRIL(6834,6),
	CIRCLET_OF_DION(6835,2),
	CIRCLET_OF_GODDARD(6836,7),
	CIRCLET_OF_OREN	(6837,4),
	CIRCLET_OF_GLUDIO(6838,1),
	CIRCLET_OF_GIRAN(6839,3),
	CIRCLET_OF_ADEN(6840,5),
	CIRCLET_OF_RUNE(8182,8),
	CIRCLET_OF_SCHUTTGART(8183,9);
	
	private final int _crownId;
	private final int _castleId;

	private CrownList(int crownId,int castleId)
	{
		_crownId = crownId;
		_castleId = castleId;
	}

	public int getCrownId()
	{
		return _crownId;
	}
	
	public int getCastleId()
	{
		return _castleId;
	}
	
	public static int findCrownByItemId(int CrownitemId)
	{
		for (CrownList current : values())
		{
			if (current.getCrownId() == CrownitemId)
				return current.getCrownId();
		}		
		return 0;		
	}
	
	public static int findCrownByCastle(int castleId)
	{
		for (CrownList current : values())
		{
			if (current.getCastleId() == castleId)
				return current.getCrownId();
		}		
		return 0;		
	}
}
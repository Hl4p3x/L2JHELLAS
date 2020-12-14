package com.l2jhellas.gameserver.enums.skills;

/**
 * @author AbsolutePower
 */
public enum OneTargetAggroSkill
{
	LURE(51);
	
	private final int _Id;
	
	private OneTargetAggroSkill(int Id)
	{
		_Id = Id;
	}

	public int getId()
	{
		return _Id;
	}

	public static OneTargetAggroSkill findById(int Id)
	{
		for (OneTargetAggroSkill val : values())
		{
			if (val.getId() == Id)
				return val;
		}
		return null;
	}
}
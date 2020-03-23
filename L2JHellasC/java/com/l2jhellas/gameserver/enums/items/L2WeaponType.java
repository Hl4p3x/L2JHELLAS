package com.l2jhellas.gameserver.enums.items;

public enum L2WeaponType
{
	
	NONE(1, "Shield",40), // Shields!!!
	SWORD(2, "Sword",40),
	BLUNT(3, "Blunt",40),
	DAGGER(4, "Dagger",40),
	BOW(5, "Bow",500),
	POLE(6, "Pole",60),
	ETC(7, "Etc",40),
	FIST(8, "Fist",40),
	DUAL(9, "Dual Sword",40),
	DUALFIST(10, "Dual Fist",40),
	BIGSWORD(11, "Big Sword",40), // Two Handed Swords
	PET(12, "Pet",40),
	ROD(13, "Rod",40),
	BIGBLUNT(14, "Big Blunt",40); // Two handed blunt
	
	private final int _id;
	private final String _name;
	private final int _range;
	
	private L2WeaponType(int id, String name,int range)
	{
		_id = id;
		_name = name;
		_range = range;
	}
	
	public int mask()
	{
		return 1 << _id;
	}
	
	public int getRange()
	{
		return _range;
	}
	
	@Override
	public String toString()
	{
		return _name;
	}
}
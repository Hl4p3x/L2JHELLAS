package com.l2jhellas.gameserver.model;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class Hit
{
	private static final int HITFLAG_USESS = 0x10;
	private static final int HITFLAG_CRIT = 0x20;
	private static final int HITFLAG_SHLD = 0x40;
	private static final int HITFLAG_MISS = 0x80;
	
	private final int _targetId;
	private final int _damage;
	private int _flags = 0;
	
	public Hit(L2Object target, int damage, boolean miss, boolean crit, byte shld, boolean soulshot, int ssGrade)
	{
		_targetId = target.getObjectId();
		_damage = damage;
		
		if (soulshot)
			_flags |= HITFLAG_USESS | ssGrade;
		
		if (crit)
			_flags |= HITFLAG_CRIT;
		
		if ((target instanceof L2Character && ((L2Character) target).isInvul()) || (shld > 0))
			_flags |= HITFLAG_SHLD;
		
		if (miss)
			_flags |= HITFLAG_MISS;
	}
	
	public int getTargetId()
	{
		return _targetId;
	}
	
	public int getDamage()
	{
		return _damage;
	}
	
	public int getFlags()
	{
		return _flags;
	}
}
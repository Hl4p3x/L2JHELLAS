package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.templates.L2Weapon;

public class ConditionTargetUsesWeaponKind extends Condition
{
	private final int _weaponMask;
	
	public ConditionTargetUsesWeaponKind(int weaponMask)
	{
		_weaponMask = weaponMask;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.target == null)
			return false;
		
		L2Weapon item = env.target.getActiveWeaponItem();
		
		if (item == null)
			return false;
		
		return (item.getItemType().mask() & _weaponMask) != 0;
	}
}
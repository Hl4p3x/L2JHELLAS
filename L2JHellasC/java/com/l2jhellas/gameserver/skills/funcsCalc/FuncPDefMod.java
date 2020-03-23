package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncPDefMod extends Func
{
	static final FuncPDefMod _fmm_instance = new FuncPDefMod();
	
	public static Func getInstance()
	{
		return _fmm_instance;
	}
	
	private FuncPDefMod()
	{
		super(Stats.POWER_DEFENCE, 0x20, null);
	}
	
	@Override
	public void calc(Env env)
	{
		if (env.player instanceof L2PcInstance)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null)
				env.value -= 12;
			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) != null)
				env.value -= ((p.isMageClass()) ? 15 : 31);
			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null)
				env.value -= ((p.isMageClass()) ? 8 : 18);
			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null)
				env.value -= 8;
			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null)
				env.value -= 7;
		}
		env.value *= env.player.getLevelMod();
	}
}
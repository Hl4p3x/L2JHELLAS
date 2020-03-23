package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncMDefMod extends Func
{
	static final FuncMDefMod _fmm_instance = new FuncMDefMod();
	
	public static Func getInstance()
	{
		return _fmm_instance;
	}
	
	private FuncMDefMod()
	{
		super(Stats.MAGIC_DEFENCE, 0x20, null);
	}
	
	@Override
	public void calc(Env env)
	{
		if (env.player instanceof L2PcInstance)
		{
			L2PcInstance p = (L2PcInstance) env.player;
			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
				env.value -= 5;
			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null)
				env.value -= 5;
			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
				env.value -= 9;
			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null)
				env.value -= 9;
			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
				env.value -= 13;
		}
		env.value *= Formulas.MENbonus[env.player.getMEN()] * env.player.getLevelMod();
	}
}
package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.gameserver.templates.L2PcTemplate;

public class FuncMaxHpAdd extends Func
{
	static final FuncMaxHpAdd _fmha_instance = new FuncMaxHpAdd();
	
	public static Func getInstance()
	{
		return _fmha_instance;
	}
	
	private FuncMaxHpAdd()
	{
		super(Stats.MAX_HP, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
		int lvl = env.player.getLevel() - t.classBaseLevel;
		double hpmod = t.lvlHpMod * lvl;
		double hpmax = (t.lvlHpAdd + hpmod) * lvl;
		double hpmin = (t.lvlHpAdd * lvl) + hpmod;
		env.value += (hpmax + hpmin) / 2;
	}
}
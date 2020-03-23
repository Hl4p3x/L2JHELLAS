package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.gameserver.templates.L2PcTemplate;

public class FuncMaxCpAdd extends Func
{
	static final FuncMaxCpAdd _fmca_instance = new FuncMaxCpAdd();
	
	public static Func getInstance()
	{
		return _fmca_instance;
	}
	
	private FuncMaxCpAdd()
	{
		super(Stats.MAX_CP, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
		int lvl = env.player.getLevel() - t.classBaseLevel;
		double cpmod = t.lvlCpMod * lvl;
		double cpmax = (t.lvlCpAdd + cpmod) * lvl;
		double cpmin = (t.lvlCpAdd * lvl) + cpmod;
		env.value += (cpmax + cpmin) / 2;
	}
}
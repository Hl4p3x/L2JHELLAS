package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.gameserver.templates.L2PcTemplate;

public class FuncMaxMpAdd extends Func
{
	static final FuncMaxMpAdd _fmma_instance = new FuncMaxMpAdd();
	
	public static Func getInstance()
	{
		return _fmma_instance;
	}
	
	private FuncMaxMpAdd()
	{
		super(Stats.MAX_MP, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
		int lvl = env.player.getLevel() - t.classBaseLevel;
		double mpmod = t.lvlMpMod * lvl;
		double mpmax = (t.lvlMpAdd + mpmod) * lvl;
		double mpmin = (t.lvlMpAdd * lvl) + mpmod;
		env.value += (mpmax + mpmin) / 2;
	}
}
package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncHennaWIT extends Func
{
	static final FuncHennaWIT _fh_instance = new FuncHennaWIT();
	
	public static Func getInstance()
	{
		return _fh_instance;
	}
	
	private FuncHennaWIT()
	{
		super(Stats.STAT_WIT, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		// L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
		L2PcInstance pc = (L2PcInstance) env.player;
		if (pc != null)
			env.value += pc.getHennaStatWIT();
	}
}
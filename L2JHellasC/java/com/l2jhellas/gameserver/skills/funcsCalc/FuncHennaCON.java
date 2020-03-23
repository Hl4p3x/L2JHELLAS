package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncHennaCON extends Func
{
	static final FuncHennaCON _fh_instance = new FuncHennaCON();
	
	public static Func getInstance()
	{
		return _fh_instance;
	}
	
	private FuncHennaCON()
	{
		super(Stats.STAT_CON, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		// L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
		L2PcInstance pc = (L2PcInstance) env.player;
		if (pc != null)
			env.value += pc.getHennaStatCON();
	}
}
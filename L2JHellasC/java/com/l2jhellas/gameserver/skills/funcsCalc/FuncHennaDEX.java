package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncHennaDEX extends Func
{
	static final FuncHennaDEX _fh_instance = new FuncHennaDEX();
	
	public static Func getInstance()
	{
		return _fh_instance;
	}
	
	private FuncHennaDEX()
	{
		super(Stats.STAT_DEX, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		// L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
		L2PcInstance pc = (L2PcInstance) env.player;
		if (pc != null)
			env.value += pc.getHennaStatDEX();
	}
}
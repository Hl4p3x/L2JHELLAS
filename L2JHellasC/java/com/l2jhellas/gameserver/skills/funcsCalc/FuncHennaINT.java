package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncHennaINT extends Func
{
	static final FuncHennaINT _fh_instance = new FuncHennaINT();
	
	public static Func getInstance()
	{
		return _fh_instance;
	}
	
	private FuncHennaINT()
	{
		super(Stats.STAT_INT, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		// L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
		L2PcInstance pc = (L2PcInstance) env.player;
		if (pc != null)
			env.value += pc.getHennaStatINT();
	}
}
package com.l2jhellas.gameserver.skills.funcs;

import com.l2jhellas.gameserver.skills.Env;

public final class LambdaStats extends Lambda
{
	public enum StatsType
	{
		PLAYER_LEVEL,
		TARGET_LEVEL,
		PLAYER_MAX_HP,
		PLAYER_MAX_MP
	}
	
	private final StatsType _stat;
	
	public LambdaStats(StatsType stat)
	{
		_stat = stat;
	}
	
	@Override
	public double calc(Env env)
	{
		switch (_stat)
		{
			case PLAYER_LEVEL:
				if (env.player == null)
					return 1;
				return env.player.getLevel();
			case TARGET_LEVEL:
				if (env.target == null)
					return 1;
				return env.target.getLevel();
			case PLAYER_MAX_HP:
				if (env.player == null)
					return 1;
				return env.player.getMaxHp();
			case PLAYER_MAX_MP:
				if (env.player == null)
					return 1;
				return env.player.getMaxMp();
		}
		return 0;
	}
}
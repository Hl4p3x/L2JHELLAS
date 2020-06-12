package com.l2jhellas.shield.antiflood;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.network.L2GameClient;


public final class FloodProtectors
{
	public static enum Action
	{
		DICE_ROLL(Config.ROLL_DICE),
		HERO_VOICE(Config.HERO_VOICE),
		SUBCLASS(Config.SUBCLASS),
		DROP_ITEM(Config.DROP_ITEM),
		SERVER_BYPASS(Config.SERVER_BYPASS),
		MULTISELL(Config.MULTISELL),
		MANUFACTURE(Config.MANUFACTURE),
		MANOR(Config.MANOR),
		SENDMAIL(Config.SENDMAIL),
		CHARACTER_SELECT(Config.CHARACTER_SELECT),
		GLOBAL_CHAT(Config.GLOBAL_CHAT),
		TRADE_CHAT(Config.TRADE_CHAT),
		USE_ITEM(Config.USE_ITEM),
		SOCIAL_ACTION(Config.SOCIAL),
		ITEM_HANDLER(Config.HANDLER);
		
		private final int _Reuse;
		
		private Action(int reuse)
		{
			_Reuse = reuse;
		}		
		public int getReuse()
		{
			return _Reuse;
		}
		
		public static final int VALUES_LENGTH = Action.values().length;
	}
	
	public static boolean performAction(L2GameClient client, Action action)
	{
		final int reuseDelay = action.getReuse();	
		
		if (reuseDelay == 0)
			return true;
		
		final long currentTime = System.nanoTime();
		final long[] value = client.getFloodProtectors();
		
		synchronized (value)
		{
			if (value[action.ordinal()] > currentTime)
				return false;
			
			value[action.ordinal()] = currentTime + reuseDelay * 1000000;
			return true;
		}
	}
}
package com.l2jhellas.gameserver.model.zone.type;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;

public class L2JailZone extends L2ZoneType
{
	public L2JailZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(ZoneId.JAIL, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			character.setInsideZone(ZoneId.NO_STORE, true);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(ZoneId.JAIL, false);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
			character.setInsideZone(ZoneId.NO_STORE, false);
			
			final L2PcInstance player = ((L2PcInstance) character);
			if (player.isInJail() && !player.isInsideZone(ZoneId.JAIL))
			{
				// when a player wants to exit jail even if he is still jailed, teleport him back to jail
				ThreadPoolManager.getInstance().scheduleGeneral(new BackToJail(character), 2000);
				player.sendMessage("You cannot cheat your way out of here. You must wait until your jail time is over.");
			}
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	static class BackToJail implements Runnable
	{
		private final L2PcInstance _activeChar;
		
		BackToJail(L2Character character)
		{
			_activeChar = (L2PcInstance) character;
		}
		
		@Override
		public void run()
		{
			_activeChar.teleToLocation(-114356, -249645, -2984, false);
		}
	}
}
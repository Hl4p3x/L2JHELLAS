package com.l2jhellas.gameserver.handlers.usercommandhandlers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.handler.IUserCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.PlaySound;
import com.l2jhellas.gameserver.network.serverpackets.SetupGauge;

public class Escape implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		52
	};
	
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (activeChar.isCastingNow() || activeChar.isSitting() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.NO_UNSTUCK_PLEASE_SEND_PETITION);
		    return false;
		}
				
		// Check to see if the current player is in TvT , CTF or ViP events.
		if (activeChar.isInFunEvent())
		{
			activeChar.sendMessage("You may not escape from an Event.");
			return false;
		}
		
		// Check to see if the player is in a festival.
		if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You may not use an escape command in a festival.");
			return false;
		}
		
		// Check to see if player is in jail
		if (activeChar.isInJail())
		{
			activeChar.sendMessage("You can not escape from jail.");
			return false;
		}

		activeChar.sendPacket(PlaySound.createSound("systemmsg_e.809"));

		if(!activeChar.isGM())
			activeChar.sendPacket(SystemMessageId.STUCK_TRANSPORT_IN_FIVE_MINUTES);
		else
            activeChar.sendMessage("You are stuck. You will be transported to the nearest village.");


		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();
		
		int unstuckTimer = (activeChar.isGM() ? 5000 : Config.UNSTUCK_INTERVAL * 1000);
		
        activeChar.broadcastPacket(new MagicSkillUse(activeChar, 1050, 1, unstuckTimer, 0));
		activeChar.sendPacket(new SetupGauge(0, unstuckTimer));
		
		// End SoE Animation section	
		EscapeFinalizer ef = new EscapeFinalizer(activeChar);
		
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, unstuckTimer));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getInstance().getGameTicks() + unstuckTimer / GameTimeController.MILLIS_IN_TICK);
		return true;
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		
		EscapeFinalizer(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead())
				return;
			
			_activeChar.setIsIn7sDungeon(false);
			
			_activeChar.enableAllSkills();
			
			try
			{
				_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
			}
			catch (Throwable e)
			{
				if (Config.DEBUG)
					e.printStackTrace();
			}
		}
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
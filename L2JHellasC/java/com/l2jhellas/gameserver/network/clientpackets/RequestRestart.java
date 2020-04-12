package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.SevenSignsFestival;
import com.l2jhellas.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.L2Effect.EffectType;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.EventManager;
import com.l2jhellas.gameserver.network.L2GameClient;
import com.l2jhellas.gameserver.network.L2GameClient.GameClientState;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.CharSelectInfo;
import com.l2jhellas.gameserver.network.serverpackets.RestartResponse;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.taskmanager.AttackStanceTaskManager;

public final class RequestRestart extends L2GameClientPacket
{
	private static final String _C__46_REQUESTRESTART = "[C] 46 RequestRestart";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.getActiveEnchantItem() != null)
			return;
		
		if (player.isInBoat())
		{
			player.sendPacket(SystemMessageId.NO_RESTART_HERE);
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_RESTART))
		{
			player.sendPacket(SystemMessageId.NO_RESTART_HERE);
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		if ((player.getOlympiadGameId() > 0) || player.isInOlympiadMode())
		{
			player.sendMessage("You can't logout in olympiad mode.");
			return;
		}
		if (player.isTeleporting())
		{
			player.abortCast();
			player.setIsTeleporting(false);
		}
		
		if ((player.isInStoreMode() || (player.isInCraftMode())))
		{
			player.sendMessage("You cannot restart while you are on store mode.");
			return;
		}
		
		if (player.getActiveRequester() != null)
		{
			player.getActiveRequester().onTradeCancel(player);
			player.onTradeCancel(player.getActiveRequester());
		}
		
		if (AttackStanceTaskManager.getInstance().isInAttackStance(player) && !player.isGM())
		{
			player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.getClan() != null && player.getFirstEffect(EffectType.CLAN_GATE) != null)
		{
			player.sendMessage("You can't logout while Clan Gate is active.");
			return;
		}
		// Prevent player from restarting if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendPacket(SystemMessage.sendString("You cannot restart while you are a participant in a festival."));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			L2Party playerParty = player.getParty();
			
			if (playerParty != null)
			{
				player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
			}
		}
		if (player.isFlying())
		{
			player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
		}
		
		if (player.isRegisteredInFunEvent())
		{
			if(player.isInFunEvent())
			   EventManager.getInstance().getCurrentEvent().onLogout(player);
			else
			   EventManager.getInstance().onLogout(player);
		}
		
		player.endDuel();
				
		L2GameClient client = getClient();
		
		// Remove From Boss
		player.removeFromBossZone();
				
		player.setClient(null);	

		player.deleteMe();			

		client.setActiveChar(null);	
		client.setState(GameClientState.AUTHED);
		
		sendPacket(RestartResponse.valueOf(true));
		
		// send char list
		final CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelectSlot(cl.getCharacterSlots());
		
		RegionBBSManager.getInstance().changeCommunityBoard();
	}
	
	@Override
	public String getType()
	{
		return _C__46_REQUESTRESTART;
	}
}
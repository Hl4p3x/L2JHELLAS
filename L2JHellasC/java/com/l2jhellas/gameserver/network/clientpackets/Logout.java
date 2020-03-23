package com.l2jhellas.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.SevenSignsFestival;
import com.l2jhellas.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.FriendList;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class Logout extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(Logout.class.getName());
	private static final String _C__09_LOGOUT = "[C] 09 Logout";
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		if (player.isInBoat())
		{
			player.sendPacket(SystemMessageId.NO_LOGOUT_HERE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().isInAttackStance(player))
		{
			player.sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.atEvent)
		{
			player.sendPacket(SystemMessage.sendString("A superior power doesn't allow you to leave the event."));
			return;
		}
		
		if ((player.getOlympiadGameId() > 0) || player.isInOlympiadMode())
		{
			player.sendMessage("You can't logout while you are in olympiad.");
			return;
		}
		
		// Prevent player from logging out if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot log out while you are a participant in a festival.");
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
		
		if ((player.isInStoreMode() || (player.isInCraftMode())))
		{
			player.sendMessage("You cannot log out while you are on store mode.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.endDuel();
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		// Remove From Boss
		player.removeFromBossZone();
		
		notifyFriends(player);
		
		player.deleteMe();
		
		player.closeNetConnection(true);
		
		RegionBBSManager.getInstance().changeCommunityBoard();
	}
	
	private static void notifyFriends(L2PcInstance cha)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT friend_name FROM character_friends WHERE char_id=?");
			statement.setInt(1, cha.getObjectId());
			ResultSet rset = statement.executeQuery();
			
			L2PcInstance friend;
			String friendName;
			
			while (rset.next())
			{
				friendName = rset.getString("friend_name");
				
				friend = L2World.getInstance().getPlayer(friendName);
				
				if (friend != null) // friend logged in.
				{
					friend.sendPacket(new FriendList(friend));
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(Logout.class.getName() + ": could not restore friend data:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__09_LOGOUT;
	}
}
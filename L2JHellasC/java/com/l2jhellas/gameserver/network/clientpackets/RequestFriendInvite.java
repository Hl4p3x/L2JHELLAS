package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.BlockList;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AskJoinFriend;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendInvite extends L2GameClientPacket
{
	private static final String _C__5E_REQUESTFRIENDINVITE = "[C] 5E RequestFriendInvite";
	
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		if (_name == null || _name.isEmpty())
			return;
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2PcInstance friend = L2World.getInstance().getPlayer(_name);
		
		if (activeChar == null)
			return;
		
		// can't use friend invite for locating invisible characters
		if (friend == null || friend.isOnline() == 0 || !friend.getAppearance().isVisible())
		{
			// Target is not found in the game.
			activeChar.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
			return;
		}
		
		if (friend == activeChar)
		{
			// You cannot add yourself to your own friend list.
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
			return;
		}
		
		if (BlockList.isBlocked(activeChar, friend))
		{
			activeChar.sendMessage("You have blocked " + _name + ".");
			return;
		}
		
		if (BlockList.isBlocked(friend, activeChar))
		{
			activeChar.sendMessage("You are in " + _name + "'s block list.");
			return;
		}
		
		if (activeChar.getFriendList().contains(friend.getObjectId()))
		{
			// Player already is in your friendlist
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST).addString(_name));
			return;
		}
		
		if (!friend.isProcessingRequest())
		{
			// request to become friend
			activeChar.onTransactionRequest(friend);
			friend.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_REQUESTED_TO_BECOME_FRIENDS).addCharName(activeChar));
			friend.sendPacket(new AskJoinFriend(activeChar.getName()));
		}
		else
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(_name));
	}
	
	@Override
	public String getType()
	{
		return _C__5E_REQUESTFRIENDINVITE;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.FriendAddRequestResult;
import com.l2jhellas.gameserver.network.serverpackets.FriendPacket;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class RequestAnswerFriendInvite extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestAnswerFriendInvite.class.getName());
	private static final String _C__5F_REQUESTANSWERFRIENDINVITE = "[C] 5F RequestAnswerFriendInvite";
	
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final L2PcInstance requestor = player.getActiveRequester();
		if (requestor == null)
			return;
		
		if (player == requestor)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIENDS_LIST);
			return;
		}
		
		if (player.getFriendList().contains(requestor.getObjectId()) || requestor.getFriendList().contains(player.getObjectId()))
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST).addCharName(player));
			return;
		}
		
		if (_response == 1)
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
			
			requestor.sendPacket(FriendAddRequestResult.STATIC_ACCEPT);
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS).addCharName(player));
			requestor.getFriendList().add(player.getObjectId());
			requestor.sendPacket(new FriendPacket(player, 1));
			
			player.sendPacket(FriendAddRequestResult.STATIC_ACCEPT);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND).addCharName(requestor));
			player.getFriendList().add(requestor.getObjectId());
			player.sendPacket(new FriendPacket(requestor, 1));
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement("INSERT INTO character_friends (char_id, friend_id, friend_name) VALUES (?,?,?), (?,?,?)"))
			{
				ps.setInt(1, requestor.getObjectId());
				ps.setInt(2, player.getObjectId());
				ps.setString(3, player.getName());
				ps.setInt(4, player.getObjectId());
				ps.setInt(5, requestor.getObjectId());
				ps.setString(6, requestor.getName());
				ps.execute();
			}
			catch (Exception e)
			{
				_log.warning(RequestAnswerFriendInvite.class.getName() + ": could not add friend objectid: " + e);
			}
		}
		else
			requestor.sendPacket(FriendAddRequestResult.STATIC_FAIL);
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__5F_REQUESTANSWERFRIENDINVITE;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.FriendList;
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
		if (player != null)
		{
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
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
				sm.addCharName(player);
				requestor.sendPacket(sm);
				return;
			}
			
			if (_response == 1)
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (char_id, friend_id, friend_name) VALUES (?,?,?), (?,?,?)");
					statement.setInt(1, requestor.getObjectId());
					statement.setInt(2, player.getObjectId());
					statement.setString(3, player.getName());
					statement.setInt(4, player.getObjectId());
					statement.setInt(5, requestor.getObjectId());
					statement.setString(6, requestor.getName());
					statement.execute();
					statement.close();
					
					requestor.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
					
					requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS).addCharName(player));
					requestor.getFriendList().add(player.getObjectId());
					
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND).addCharName(requestor));
					player.getFriendList().add(requestor.getObjectId());
					
					requestor.sendPacket(new FriendList(requestor));
					player.sendPacket(new FriendList(player));
					
				}
				catch (SQLException e)
				{
					_log.warning(RequestAnswerFriendInvite.class.getName() + ": could not add friend objectid: ");
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
			}
			else
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
				requestor.sendPacket(msg);
				msg = null;
			}
			
			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__5F_REQUESTANSWERFRIENDINVITE;
	}
}
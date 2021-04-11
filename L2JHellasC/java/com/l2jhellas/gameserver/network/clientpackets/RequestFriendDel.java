package com.l2jhellas.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.datatables.sql.CharNameTable;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.FriendPacket;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class RequestFriendDel extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestFriendDel.class.getName());
	private static final String _C__61_REQUESTFRIENDDEL = "[C] 61 RequestFriendDel";
	
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
		
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final int targetId = CharNameTable.getInstance().getIdByName(_name);

		if (targetId == -1 || !player.getFriendList().contains(targetId))
		{
			player.sendPacket(SystemMessageId.THE_USER_NOT_IN_FRIENDS_LIST);
			return;
		}
		
		player.getFriendList().remove(Integer.valueOf(targetId));
		
		final L2PcInstance target = L2World.getInstance().getPlayer(_name);
		if (target != null)
		{
			player.sendPacket(new FriendPacket(target, 3));
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_name));
			
			target.getFriendList().remove(Integer.valueOf(player.getObjectId()));
			target.sendPacket(new FriendPacket(player, 3));
		}
		else
		{
			player.sendPacket(new FriendPacket(_name, 3));
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_name));
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)"))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, targetId);
			ps.setInt(3, targetId);
			ps.setInt(4, player.getObjectId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete friend objectid: ", e);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__61_REQUESTFRIENDDEL;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.datatables.sql.CharNameTable;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.FriendList;
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
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2PcInstance friend = L2World.getInstance().getPlayer(_name);
		
		if (activeChar == null || friend == null)
			return;
		
		final int friendid = CharNameTable.getInstance().getIdByName(_name);
		
		if (friendid == -1 || !activeChar.getFriendList().contains(friendid))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST).addString(_name));
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)");
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, friendid);
			statement.setInt(3, friendid);
			statement.setInt(4, activeChar.getObjectId());
			statement.execute();
			statement.close();
			
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_name));
			
			activeChar.getFriendList().remove(Integer.valueOf(friendid));
			activeChar.sendPacket(new FriendList(activeChar));
			
			friend.getFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
			friend.sendPacket(new FriendList(friend));
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
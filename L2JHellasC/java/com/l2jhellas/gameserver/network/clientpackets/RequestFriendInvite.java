package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.FriendAddRequest;
import com.l2jhellas.gameserver.network.serverpackets.FriendAddRequestResult;
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
		
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final L2PcInstance target = L2World.getInstance().getPlayer(_name);
		if (target == null || !target.isbOnline())
		{
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.sendPacket(FriendAddRequestResult.STATIC_FAIL);
			return;
		}
		
		if (target == player)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIENDS_LIST);
			player.sendPacket(FriendAddRequestResult.STATIC_FAIL);
			return;
		}
		
		if (target.getBlockList().isBlockAll())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_BLOCKED_EVERYTHING).addString(target.getName()));
			return;
		}
		
		if (target.getBlockList().isInBlockList(player))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST2).addString(target.getName()));
			return;
		}
		
		if (player.getFriendList().contains(target.getObjectId()))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST).addString(_name));
			player.sendPacket(FriendAddRequestResult.STATIC_FAIL);
			return;
		}
		
		if (target.isProcessingRequest())
		{
			player.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			player.sendPacket(FriendAddRequestResult.STATIC_FAIL);
			return;
		}
		
		player.onTransactionRequest(target);
		target.sendPacket(new FriendAddRequest(player.getName()));
	}
	
	@Override
	public String getType()
	{
		return _C__5E_REQUESTFRIENDINVITE;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.FriendRecvMsg;

public final class RequestSendFriendMsg extends L2GameClientPacket
{
	private static final String _C__CC_REQUESTSENDMSG = "[C] CC RequestSendMsg";
	
	private String _message;
	private String _reciever;
	
	@Override
	protected void readImpl()
	{
		_message = readS();
		_reciever = readS();
	}
	
	@Override
	protected void runImpl()
	{
		if ((_message == null) || _message.isEmpty() || (_message.length() > 300))
			return;
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2PcInstance targetPlayer = L2World.getInstance().getPlayer(_reciever);
		
		if ((targetPlayer == null) || !targetPlayer.getFriendList().contains(activeChar.getObjectId()))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}
		
		FriendRecvMsg frm = new FriendRecvMsg(activeChar.getName(), _reciever, _message);
		targetPlayer.sendPacket(frm);
	}
	
	@Override
	public String getType()
	{
		return _C__CC_REQUESTSENDMSG;
	}
}
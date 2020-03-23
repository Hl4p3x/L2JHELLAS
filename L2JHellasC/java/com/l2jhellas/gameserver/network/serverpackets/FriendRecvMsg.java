package com.l2jhellas.gameserver.network.serverpackets;

public class FriendRecvMsg extends L2GameServerPacket
{
	private static final String _S__FD_FRIENDRECVMSG = "[S] FD FriendRecvMsg";
	
	private final String _sender, _receiver, _message;
	
	public FriendRecvMsg(String sender, String reciever, String message)
	{
		_sender = sender;
		_receiver = reciever;
		
		_message = message;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfd);
		
		writeD(0); // ??
		writeS(_receiver);
		writeS(_sender);
		writeS(_message);
	}
	
	@Override
	public String getType()
	{
		return _S__FD_FRIENDRECVMSG;
	}
}
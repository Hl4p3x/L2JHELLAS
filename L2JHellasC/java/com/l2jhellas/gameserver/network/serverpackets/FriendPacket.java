package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class FriendPacket extends L2GameServerPacket
{
	private int _action; 
	private String _name;
	private int _objectId;
	private int _isOnline;
	
	public FriendPacket(L2PcInstance player, int action)
	{
		_action = action;
		_name = player.getName();
		_objectId = player.getObjectId();
		_isOnline = player.isOnline() ? 1 : 0;
	}
	
	public FriendPacket(String name, int action)
	{
		_action = action;
		_name = name;
		_objectId = 0;
		_isOnline = 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfb);
		writeD(_action);
		writeD(0x00); 
		writeS(_name);
		writeD(_isOnline);
		writeD(_objectId);
	}
}
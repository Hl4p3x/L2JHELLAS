package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.datatables.sql.CharNameTable;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class FriendStatus extends L2GameServerPacket
{
	private final boolean _isOnline;
	private final String _name;
	private final int _objectId;
	L2PcInstance _player;
	
	public FriendStatus(int objectId)
	{
		_player = L2World.getInstance().getPlayer(objectId);
		_isOnline = _player != null && _player.isbOnline();
		_name = _isOnline ? _player.getName() : CharNameTable.getInstance().getNameById(objectId);
		_objectId = objectId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7b);
		writeD((_isOnline) ? 1 : 0);
		writeS(_name);
		writeD(_objectId);
	}
}
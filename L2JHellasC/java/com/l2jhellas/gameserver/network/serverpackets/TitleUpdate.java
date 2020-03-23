package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class TitleUpdate extends L2GameServerPacket
{
	private static final String _S__CC_TITLE_UPDATE = "[S] cc TitleUpdate";
	private final String _title;
	private final int _objectId;
	
	public TitleUpdate(L2PcInstance cha)
	{
		_objectId = cha.getObjectId();
		_title = cha.getTitle();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xcc);
		writeD(_objectId);
		writeS(_title);
	}
	
	@Override
	public String getType()
	{
		return _S__CC_TITLE_UPDATE;
	}
}
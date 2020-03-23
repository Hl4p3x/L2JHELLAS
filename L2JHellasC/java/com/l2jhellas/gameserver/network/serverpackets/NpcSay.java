package com.l2jhellas.gameserver.network.serverpackets;

public final class NpcSay extends L2GameServerPacket
{
	// dddS
	private static final String _S__30_NPCSAY = "[S] 30 NpcSay";
	private final int _objectId;
	private final int _textType;
	private final int _npcId;
	private final String _text;
	
	public NpcSay(int objectId, int messageType, int npcId, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_text = text;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x02);
		writeD(_objectId);
		writeD(_textType);
		writeD(_npcId);
		writeS(_text);
	}
	
	@Override
	public String getType()
	{
		return _S__30_NPCSAY;
	}
}
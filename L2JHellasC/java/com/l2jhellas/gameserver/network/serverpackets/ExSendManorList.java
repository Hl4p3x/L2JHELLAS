package com.l2jhellas.gameserver.network.serverpackets;

public class ExSendManorList extends L2GameServerPacket
{
	private static final String _S__FE_1B_EXSENDMANORLIST = "[S] FE:1B ExSendManorList";
	
	public static final ExSendManorList STATIC_PACKET = new ExSendManorList();
	
	private ExSendManorList()
	{
		
	}
	
	private static final String[] _manorList =
	{
		"gludio",
		"dion",
		"giran",
		"oren",
		"aden",
		"innadril",
		"goddard",
		"rune",
		"schuttgart"
	};
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1B);
		writeD(_manorList.length);
		
		for (int i = 0; i < _manorList.length; i++)
		{
			writeD(i + 1);
			writeS(_manorList[i]);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_1B_EXSENDMANORLIST;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

public class ShowXMasSeal extends L2GameServerPacket
{
	private static final String _S__F2_SHOWXMASSEAL = "[S] F2 ShowXMasSeal";
	private final int _item;
	
	public ShowXMasSeal(int item)
	{
		_item = item;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xF2);
		
		writeD(_item);
	}
	
	@Override
	public String getType()
	{
		return _S__F2_SHOWXMASSEAL;
	}
}
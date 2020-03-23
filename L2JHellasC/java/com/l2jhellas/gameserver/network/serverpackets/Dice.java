package com.l2jhellas.gameserver.network.serverpackets;

public class Dice extends L2GameServerPacket
{
	private static final String _S__D4_Dice = "[S] D4 Dice";
	private final int _charObjId;
	private final int _itemId;
	private final int _number;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public Dice(int charObjId, int itemId, int number, int x, int y, int z)
	{
		_charObjId = charObjId;
		_itemId = itemId;
		_number = number;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xD4);
		writeD(_charObjId); // object id of player
		writeD(_itemId); // item id of dice (spade) 4625,4626,4627,4628
		writeD(_number); // number rolled
		writeD(_x); // x
		writeD(_y); // y
		writeD(_z); // z
	}
	
	@Override
	public String getType()
	{
		return _S__D4_Dice;
	}
}
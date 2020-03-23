package com.l2jhellas.gameserver.network.serverpackets;

public class ExPledgeCrestLarge extends L2GameServerPacket
{
	private static final String _S__FE_28_EXPLEDGECRESTLARGE = "[S] FE:28 ExPledgeCrestLarge";
	private final int _crestId;
	private final byte[] _data;
	
	public ExPledgeCrestLarge(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x28);
		
		writeD(0x00); // ???
		writeD(_crestId);
		writeD(_data.length);
		
		writeB(_data);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_28_EXPLEDGECRESTLARGE;
	}
}
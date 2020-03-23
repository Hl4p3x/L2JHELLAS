package com.l2jhellas.gameserver.network.serverpackets;

public class AllyCrest extends L2GameServerPacket
{
	private static final String _S__C7_ALLYCREST = "[S] ae AllyCrest";
	private final int _crestId;
	private final int _crestSize;
	private byte[] _data;
	
	public AllyCrest(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
		_crestSize = _data.length;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xae);
		writeD(_crestId);
		writeD(_crestSize);
		writeB(_data);
		_data = null;
	}
	
	@Override
	public String getType()
	{
		return _S__C7_ALLYCREST;
	}
}
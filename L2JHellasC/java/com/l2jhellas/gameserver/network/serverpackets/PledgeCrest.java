package com.l2jhellas.gameserver.network.serverpackets;

public class PledgeCrest extends L2GameServerPacket
{
	private static final String _S__84_PLEDGECREST = "[S] 6c PledgeCrest";
	private final int _crestId;
	private final int _crestSize;
	private byte[] _data;
	
	// public PledgeCrest(int crestId,byte[] data)
	public PledgeCrest(int crestId, byte[] data)
	{
		_crestId = crestId;
		// _data = data;
		_data = data;
		_crestSize = _data.length;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6c);
		writeD(_crestId);
		writeD(_crestSize);
		writeB(_data);
		_data = null;
	}
	
	@Override
	public String getType()
	{
		return _S__84_PLEDGECREST;
	}
}
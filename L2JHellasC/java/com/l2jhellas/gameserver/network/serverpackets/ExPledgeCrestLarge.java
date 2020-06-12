package com.l2jhellas.gameserver.network.serverpackets;

public class ExPledgeCrestLarge extends L2GameServerPacket
{
	private static final String _S__FE_28_EXPLEDGECRESTLARGE = "[S] FE:28 ExPledgeCrestLarge";
	private final int _clanId;
	private final int _crestId;
	private final byte[] _data;
	
	public ExPledgeCrestLarge(int clanId,int crestId, byte[] data)
	{
		_clanId = clanId;
		_crestId = crestId;
		_data = data;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x28);
		
		writeD(_clanId);
		writeD(_crestId);
		
        if (_data == null)
        	writeD(0);      	
        else
        {
            writeD(_data.length);
            writeB(_data);
        }
	}
	
	@Override
	public String getType()
	{
		return _S__FE_28_EXPLEDGECRESTLARGE;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

public class ExUseSharedGroupItem extends L2GameServerPacket
{
	private static final String _S__FE_49_EXUSESHAREDGROUPITEM = "[S] FE:49 ExUseSharedGroupItem";
	private final int _unk1, _unk2, _unk3, _unk4;
	
	public ExUseSharedGroupItem(int unk1, int unk2, int unk3, int unk4)
	{
		_unk1 = unk1;
		_unk2 = unk2;
		_unk3 = unk3;
		_unk4 = unk4;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x49);
		
		writeD(_unk1);
		writeD(_unk2);
		writeD(_unk3);
		writeD(_unk4);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_49_EXUSESHAREDGROUPITEM;
	}
}
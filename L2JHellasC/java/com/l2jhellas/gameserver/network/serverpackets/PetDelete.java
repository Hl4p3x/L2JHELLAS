package com.l2jhellas.gameserver.network.serverpackets;

public class PetDelete extends L2GameServerPacket
{
	private static final String _S__B7_PETDELETE = "[S] b7 PetDelete";
	private final int _petType;
	private final int _petObjId;
	
	public PetDelete(int petType, int petObjId)
	{
		_petType = petType;// Summon Type
		_petObjId = petObjId;// objectId
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb6);
		writeD(_petType);
		writeD(_petObjId);
	}
	
	@Override
	public String getType()
	{
		return _S__B7_PETDELETE;
	}
}
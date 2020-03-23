package com.l2jhellas.gameserver.network.serverpackets;

public class PledgeSkillListAdd extends L2GameServerPacket
{
	private static final String _S__FE_3A_PLEDGESKILLLISTADD = "[S] FE:3A PledgeSkillListAdd";
	private final int _id;
	private final int _lvl;
	
	public PledgeSkillListAdd(int id, int lvl)
	{
		_id = id;
		_lvl = lvl;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3a);
		
		writeD(_id);
		writeD(_lvl);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_3A_PLEDGESKILLLISTADD;
	}
}
package com.l2jhellas.gameserver.network.serverpackets;

public class ShowSeedSetting extends L2GameServerPacket
{
	private static final String _S__FE_1F_SHOWSEEDSETTING = "[S] FE:1F ShowSeedSetting";
	
	@Override
	public void runImpl()
	{
		// no long running
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1F);
		writeD(0); // manor id
		writeD(1); // size
		writeD(5033); // seed id
		writeD(31); // level
		writeC(1); // size (of next)?
		writeD(1871); // reward 1 id
		writeC(1); // size (of next)?
		writeD(4042); // reward 2 id
		writeD(2250); // next sale limit
		writeD(20); // count
		writeD(12); // min seed price
		writeD(200); // max seed price
		writeD(4); // today sales
		writeD(5); // today price
		writeD(6); // next sales
		writeD(7); // next price
	}
	
	@Override
	public String getType()
	{
		return _S__FE_1F_SHOWSEEDSETTING;
	}
}
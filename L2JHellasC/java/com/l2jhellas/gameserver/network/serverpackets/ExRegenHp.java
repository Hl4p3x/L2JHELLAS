package com.l2jhellas.gameserver.network.serverpackets;

public final class ExRegenHp extends L2GameServerPacket
{
	private final int _c;
	private final int _t;
	private final double _hp;
	
	public ExRegenHp(int count, int time, double hpRegen)
	{
		_c = count;
		_t = time;
		_hp = hpRegen * 0.66;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x01);
		writeD(1);
		writeD(_c);
		writeD(_t);
		writeF(_hp);
	}
}
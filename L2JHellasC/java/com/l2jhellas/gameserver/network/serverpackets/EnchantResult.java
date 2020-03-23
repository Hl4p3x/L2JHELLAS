package com.l2jhellas.gameserver.network.serverpackets;

public class EnchantResult extends L2GameServerPacket
{
	public static final EnchantResult SUCCESS = new EnchantResult(0);
	public static final EnchantResult UNK_RESULT_1 = new EnchantResult(1);
	public static final EnchantResult CANCELLED = new EnchantResult(2);
	public static final EnchantResult UNSUCCESS = new EnchantResult(3);
	public static final EnchantResult UNK_RESULT_4 = new EnchantResult(4);
	private static final String _S__81_ENCHANTRESULT = "[S] 81 EnchantResult";
	private final int _unknown;
	
	public EnchantResult(int unknown)
	{
		_unknown = unknown;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x81);
		writeD(_unknown);
	}
	
	@Override
	public String getType()
	{
		return _S__81_ENCHANTRESULT;
	}
}
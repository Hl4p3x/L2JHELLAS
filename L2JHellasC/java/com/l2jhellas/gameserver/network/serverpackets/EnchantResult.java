package com.l2jhellas.gameserver.network.serverpackets;

public class EnchantResult extends L2GameServerPacket
{
	private static final String _S__81_ENCHANTRESULT = "[S] 81 EnchantResult";
	
    public static final EnchantResult SUCCESS = new EnchantResult(0);
    public static final EnchantResult CANCELLED = new EnchantResult(2);
    public static final EnchantResult BLESSED_FAILED = new EnchantResult(3);
    public static final EnchantResult FAILED_NO_CRYSTALS = new EnchantResult(4);
    public static final EnchantResult FAILED_CRYSTALS = new EnchantResult(1);

    private final int _resultId;
	
	public EnchantResult(final int resultId)
	{
        _resultId = resultId;
	}
	
	@Override
	protected final void writeImpl()
	{
        writeC(0x81);
        writeD(_resultId);
	}
	
	@Override
	public String getType()
	{
		return _S__81_ENCHANTRESULT;
	}
}
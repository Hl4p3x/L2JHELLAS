package com.l2jhellas.gameserver.network.serverpackets;

public class AcquireSkillDone extends L2GameServerPacket
{
    private static final String _S__A7_ACQUIRESKILLDONE = "[S] 8e AcquireSkillDone";
	public static final AcquireSkillDone STATIC_PACKET = new AcquireSkillDone();

    @Override
	protected final void writeImpl()
    {
        writeC(0x8e);
    }

    @Override
    public String getType()
    {
        return _S__A7_ACQUIRESKILLDONE;
    }
}
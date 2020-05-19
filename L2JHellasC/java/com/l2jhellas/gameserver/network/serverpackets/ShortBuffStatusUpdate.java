package com.l2jhellas.gameserver.network.serverpackets;

public class ShortBuffStatusUpdate extends L2GameServerPacket
{
	private static final String _S__F4_SHORTBUFFSTATUSUPDATE = "[S] F4 ShortBuffStatusUpdate";
	
	public static final ShortBuffStatusUpdate RESET_SHORT_BUFF = new ShortBuffStatusUpdate(0, 0, 0);
	
	private final int _skillId;
	private final int _skillLvl;
	private final int _duration;
	
	public ShortBuffStatusUpdate(int skillId, int skillLvl, int duration)
	{
		_skillId = skillId;
		_skillLvl = skillLvl;
		_duration = duration;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xF4);
		writeD(_skillId);
		writeD(_skillLvl);
		writeD(_duration);
	}
	
	@Override
	public String getType()
	{
		return _S__F4_SHORTBUFFSTATUSUPDATE;
	}
}
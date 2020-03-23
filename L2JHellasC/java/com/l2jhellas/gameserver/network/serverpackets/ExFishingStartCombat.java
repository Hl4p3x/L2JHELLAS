package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class ExFishingStartCombat extends L2GameServerPacket
{
	private static final String _S__FE_15_EXFISHINGSTARTCOMBAT = "[S] FE:15 ExFishingStartCombat";
	private final L2Character _activeChar;
	private final int _time, _hp;
	private final int _lureType, _deceptiveMode, _mode;
	
	public ExFishingStartCombat(L2Character character, int time, int hp, int mode, int lureType, int deceptiveMode)
	{
		_activeChar = character;
		_time = time;
		_hp = hp;
		_mode = mode;
		_lureType = lureType;
		_deceptiveMode = deceptiveMode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x15);
		
		writeD(_activeChar.getObjectId());
		writeD(_time);
		writeD(_hp);
		writeC(_mode); // mode: 0 = resting, 1 = fighting
		writeC(_lureType); // 0 = newbie lure, 1 = normal lure, 2 = night lure
		writeC(_deceptiveMode); // Fish Deceptive Mode: 0 = no, 1 = yes
	}
	
	@Override
	public String getType()
	{
		return _S__FE_15_EXFISHINGSTARTCOMBAT;
	}
}
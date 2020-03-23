package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class MagicSkillUse extends L2GameServerPacket
{
	private static final String _S__5A_MAGICSKILLUSER = "[S] 5A MagicSkillUser";
	private final int _targetId;
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	private final int _charObjId, _x, _y, _z, _targetx, _targety, _targetz;
	private boolean _success = false;
	
	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay, boolean crit)
	{
		this(cha, target, skillId, skillLevel, hitTime, reuseDelay);
		_success = crit;
	}
	
	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_targetx = target.getX();
		_targety = target.getY();
		_targetz = target.getZ();
	}
	
	public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		_charObjId = cha.getObjectId();
		_targetId = cha.getTargetId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_targetx = cha.getX();
		_targety = cha.getY();
		_targetz = cha.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x48);
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_reuseDelay);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		
		if (_success)
		{
			writeD(0x01);
			writeH(0x00);
		}
		else
			writeD(0x00);
		
		writeD(_targetx);
		writeD(_targety);
		writeD(_targetz);
	}
	
	@Override
	public String getType()
	{
		return _S__5A_MAGICSKILLUSER;
	}
}
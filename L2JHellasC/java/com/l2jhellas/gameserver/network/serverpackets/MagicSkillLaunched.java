package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Character;

public class MagicSkillLaunched extends L2GameServerPacket
{
	private static final String _S__8E_MAGICSKILLLAUNCHED = "[S] 8E MagicSkillLaunched";
	private final int _charObjId;
	private final int _skillId;
	private final int _skillLevel;
	private final int _numberOfTargets;
	private L2Object[] _targets;
	private final int _singleTargetId;
	
	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, L2Object[] targets)
	{
		_charObjId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_numberOfTargets = targets.length;
		_targets = targets;
		_singleTargetId = 0;
	}
	
	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel)
	{
		_charObjId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_numberOfTargets = 1;
		_singleTargetId = cha.getTargetId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x76);
		writeD(_charObjId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_numberOfTargets); // also failed or not?
		if (_singleTargetId != 0 || _numberOfTargets == 0)
			writeD(_singleTargetId);
		else
			for (L2Object target : _targets)
			{
				try
				{
					writeD(target.getObjectId());
				}
				catch (NullPointerException e)
				{
					writeD(0); // untested
				}
			}
	}
	
	@Override
	public String getType()
	{
		return _S__8E_MAGICSKILLLAUNCHED;
	}
}
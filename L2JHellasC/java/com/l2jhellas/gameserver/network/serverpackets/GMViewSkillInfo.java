package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class GMViewSkillInfo extends L2GameServerPacket
{
	private static final String _S__91_GMViewSkillInfo = "[S] 91 GMViewSkillInfo";
	private final L2PcInstance _activeChar;
	private L2Skill[] _skills;
	
	public GMViewSkillInfo(L2PcInstance cha)
	{
		_activeChar = cha;
		_skills = _activeChar.getAllSkills();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x91);
		writeS(_activeChar.getName());
		writeD(_skills.length);
		
		boolean isDisabled = (_activeChar.getClan() != null) && (_activeChar.getClan().getReputationScore() < 0);
		
		for (L2Skill skill : _skills)
		{
			writeD(skill.isPassive() ? 1 : 0);
			writeD(skill.getLevel());
			writeD(skill.getId());
			writeC(_activeChar.isWearingFormalWear() || (skill.isClanSkill() && isDisabled) ? 1 : 0);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__91_GMViewSkillInfo;
	}
}
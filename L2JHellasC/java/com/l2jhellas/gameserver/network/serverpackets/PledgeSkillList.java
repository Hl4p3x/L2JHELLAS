package com.l2jhellas.gameserver.network.serverpackets;


import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2Skill;

public class PledgeSkillList extends L2GameServerPacket
{
	private static final String _S__FE_39_PLEDGESKILLLIST = "[S] FE:39 PledgeSkillList";
	private final L2Skill[] _skills;

	public PledgeSkillList(L2Clan clan)
	{
		_skills = clan.getAllSkills();
	}
	
	@Override
	protected void writeImpl()
	{	
		writeC(0xfe);
		writeH(0x39);
		writeD(_skills.length);
		for (L2Skill sk : _skills)
		{
			writeD(sk.getId());
			writeD(sk.getLevel());
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_39_PLEDGESKILLLIST;
	}
}
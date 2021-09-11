package com.l2jhellas.gameserver.network.serverpackets;

import java.util.List;

import com.l2jhellas.gameserver.holder.EnchantSkillNode;

public class ExEnchantSkillList extends L2GameServerPacket
{
	private static final String _S__FE_17_EXENCHANTSKILLLIST = "[S] FE:17 ExEnchantSkillList";
	
	private final List<EnchantSkillNode> _skills;
	
	public ExEnchantSkillList(List<EnchantSkillNode> skills)
	{
		_skills = skills;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x17);
		
		writeD(_skills.size());
		for (EnchantSkillNode esn : _skills)
		{
			writeD(esn.getId());
			writeD(esn.getValue());
			writeD(esn.getSp());
			writeQ(esn.getExp());
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_17_EXENCHANTSKILLLIST;
	}
}
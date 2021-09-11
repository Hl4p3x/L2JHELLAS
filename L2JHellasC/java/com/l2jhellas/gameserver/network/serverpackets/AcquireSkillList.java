package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.holder.ClanSkillNode;
import com.l2jhellas.gameserver.holder.FishingSkillNode;
import com.l2jhellas.gameserver.holder.GeneralSkillNode;
import com.l2jhellas.gameserver.holder.SkillNode;

public class AcquireSkillList extends L2GameServerPacket
{
	public enum skillType
	{
		Usual,
		Fishing,
		Clan
	}
	
	private static final String _S__A3_ACQUIRESKILLLIST = "[S] 8a AcquireSkillList";
	
	private List<? extends SkillNode> _skills;
	
	private final skillType _skillType;
	
	public AcquireSkillList(skillType type, List<? extends SkillNode> skills)
	{
		_skillType = type;
		_skills = new ArrayList<>(skills);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x8a);
		writeD(_skillType.ordinal());
		writeD(_skills.size());
		
		switch (_skillType)
		{
			case Usual:
				_skills.stream().map(GeneralSkillNode.class::cast).forEach(gsn ->
				{
					writeD(gsn.getId());
					writeD(gsn.getValue());
					writeD(gsn.getValue());
					writeD(gsn.getCorrectedCost());
					writeD(0);
				});
				break;
			
			case Fishing:
				_skills.stream().map(FishingSkillNode.class::cast).forEach(gsn ->
				{
					writeD(gsn.getId());
					writeD(gsn.getValue());
					writeD(gsn.getValue());
					writeD(0);
					writeD(1);
				});
				break;
			
			case Clan:
				_skills.stream().map(ClanSkillNode.class::cast).forEach(gsn ->
				{
					writeD(gsn.getId());
					writeD(gsn.getValue());
					writeD(gsn.getValue());
					writeD(gsn.getCost());
					writeD(0);
				});
				break;
		}
	}
	
	@Override
	public String getType()
	{
		return _S__A3_ACQUIRESKILLLIST;
	}
}
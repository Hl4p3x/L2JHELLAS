package com.l2jhellas.gameserver.network.serverpackets;

import java.util.LinkedHashMap;
import java.util.Map;

import com.l2jhellas.gameserver.holder.EffectHolder;
import com.l2jhellas.gameserver.model.L2Skill;

public class AbnormalStatusUpdate extends L2GameServerPacket
{
	private static final String _S__97_ABNORMALSTATUSUPDATE = "[S] 7f AbnormalStatusUpdate";
	private final Map<Integer , EffectHolder> _effects = new LinkedHashMap<>();

	public AbnormalStatusUpdate()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7f);
		
		writeH(_effects.size());

		for (EffectHolder holder : _effects.values())
		{
			writeD(holder.getId());
			writeH(holder.getValue());
			writeD((holder.getDuration() == -1) ? -1 : holder.getDuration() / 1000);
		}
	}
	
	public void addEffect(L2Skill skill, int duration)
	{
		if(skill.isDebuff())
			_effects.putIfAbsent(skill.getId(), new EffectHolder(skill, duration));
		else
			_effects.put(skill.getId(), new EffectHolder(skill, duration));
	}
	
	@Override
	public String getType()
	{
		return _S__97_ABNORMALSTATUSUPDATE;
	}
}
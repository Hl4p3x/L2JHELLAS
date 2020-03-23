package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class MagicEffectIcons extends L2GameServerPacket
{
	private static final String _S__97_MAGICEFFECTICONS = "[S] 7f MagicEffectIcons";
	private final List<Effect> _effects;
	
	private class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _duration;
		
		public Effect(int pSkillId, int pLevel, int pDuration)
		{
			_skillId = pSkillId;
			_level = pLevel;
			_duration = pDuration;
		}
	}
	
	public MagicEffectIcons()
	{
		_effects = new ArrayList<>();
	}
	
	public void addEffect(int skillId, int level, int duration)
	{
		_effects.add(new Effect(skillId, level, duration));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7f);
		
		writeH(_effects.size());
		
		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);		
			writeD(temp._duration == -1 ? -1 : temp._duration / 1000);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__97_MAGICEFFECTICONS;
	}
}
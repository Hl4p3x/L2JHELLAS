package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	// chdd(dhd)
	private static final String _S__FE_2A_OLYMPIADSPELLEDINFO = "[S] FE:2A ExOlympiadSpelledInfo";
	private final L2PcInstance _player;
	private final List<Effect> _effects;
	
	private class Effect
	{
		protected int _skillId;
		protected int _dat;
		protected int _duration;
		
		public Effect(int pSkillId, int pDat, int pDuration)
		{
			_skillId = pSkillId;
			_dat = pDat;
			_duration = pDuration;
		}
	}
	
	public ExOlympiadSpelledInfo(L2PcInstance player)
	{
		_effects = new ArrayList<>();
		_player = player;
	}
	
	public void addEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_player == null)
			return;
		writeC(0xfe);
		writeH(0x2a);
		writeD(_player.getObjectId());
		writeD(_effects.size());
		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._dat);
			writeD(temp._duration / 1000);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_2A_OLYMPIADSPELLEDINFO;
	}
}
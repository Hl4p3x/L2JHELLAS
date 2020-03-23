package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;

public class PartySpelled extends L2GameServerPacket
{
	private static final String _S__EE_PartySpelled = "[S] EE PartySpelled";
	private final List<Effect> _effects;
	private final L2Character _activeChar;
	
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
	
	public PartySpelled(L2Character cha)
	{
		_effects = new ArrayList<>();
		_activeChar = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_activeChar == null)
			return;
		writeC(0xee);
		writeD(_activeChar instanceof L2SummonInstance ? 2 : _activeChar instanceof L2PetInstance ? 1 : 0);
		writeD(_activeChar.getObjectId());
		writeD(_effects.size());
		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._dat);
			writeD(temp._duration / 1000);
		}
	}
	
	public void addPartySpelledEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}
	
	@Override
	public String getType()
	{
		return _S__EE_PartySpelled;
	}
}
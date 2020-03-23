package com.l2jhellas.gameserver.model.zone.type;

import java.util.ArrayList;

import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2WorldRegion;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;

public class L2SignetZone extends L2ZoneType
{
	private final L2WorldRegion _region;
	private final L2Character _owner;
	private final boolean _isOffensive;
	private final int _toRemoveOnOwnerExit;
	private final L2Skill _skill;
	
	public L2SignetZone(L2WorldRegion region, L2Character owner, boolean isOffensive, int toRemoveOnOwnerExit, L2Skill skill)
	{
		super(-1);
		_region = region;
		_owner = owner;
		_isOffensive = isOffensive;
		_toRemoveOnOwnerExit = toRemoveOnOwnerExit;
		_skill = skill;
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (!_isOffensive)
			_skill.getEffects(_owner, character);
		else if (character != _owner && !character.isInsidePeaceZone(_owner, character))
			_skill.getEffects(_owner, character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character == _owner && _toRemoveOnOwnerExit > 0)
		{
			_owner.stopSkillEffects(_toRemoveOnOwnerExit);
			return;
		}
		
		character.stopSkillEffects(_skill.getId());
	}
	
	public void remove()
	{
		_region.removeZone(this);
		
		for (L2Character member : _characterList.values())
			member.stopSkillEffects(_skill.getId());
		
		if (!_isOffensive)
			_owner.stopSkillEffects(_skill.getId());
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
		if (character == _owner && _toRemoveOnOwnerExit > 0)
			_owner.stopSkillEffects(_toRemoveOnOwnerExit);
		else
			character.stopSkillEffects(_skill.getId());
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		if (!_isOffensive)
			_skill.getEffects(_owner, character);
		else if (character != _owner && !character.isInsidePeaceZone(_owner, character))
			_skill.getEffects(_owner, character);
	}
	
	public L2Character[] getCharactersInZone()
	{
		ArrayList<L2Character> charsInZone = new ArrayList<>();
		for (L2Character character : _characterList.values())
		{
			if (!_isOffensive)
				charsInZone.add(character);
			else if (character != _owner && !character.isInsidePeaceZone(_owner, character))
				charsInZone.add(character);
		}
		return charsInZone.toArray(new L2Character[_characterList.values().size()]);
	}
}
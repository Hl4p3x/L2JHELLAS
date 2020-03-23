package com.l2jhellas.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.zone.type.L2OlympiadStadiumZone;

public class OlympiadStadiaManager
{
	private final List<L2OlympiadStadiumZone> _olympiadStadias = new ArrayList<>();
	
	public OlympiadStadiaManager()
	{
	}
	
	public void addStadium(L2OlympiadStadiumZone arena)
	{
		_olympiadStadias.add(arena);
	}
	
	public void clearStadium()
	{
		_olympiadStadias.clear();
	}
	
	public final L2OlympiadStadiumZone getStadium(L2Character character)
	{
		return  _olympiadStadias.stream().filter(stad -> stad != null && stad.isCharacterInZone(character)).findFirst().orElse(null);
	}
	
	public final L2OlympiadStadiumZone getOlympiadStadiumById(int olympiadStadiumId)
	{
		return  _olympiadStadias.stream().filter(stad -> stad != null && stad.getStadiumId() == olympiadStadiumId).findFirst().orElse(null);
	}

	public static OlympiadStadiaManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final OlympiadStadiaManager _instance = new OlympiadStadiaManager();
	}
}
package com.l2jhellas.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;

public class SkillHandler implements IHandler<ISkillHandler, L2SkillType>
{
	private final Map<Integer, ISkillHandler> _datatable;
	
	protected SkillHandler()
	{
		_datatable = new HashMap<>();
	}
	
	@Override
	public void registerHandler(ISkillHandler handler)
	{
		L2SkillType[] types = handler.getSkillIds();
		for (L2SkillType t : types)
		{
			_datatable.put(t.ordinal(), handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(ISkillHandler handler)
	{
		L2SkillType[] types = handler.getSkillIds();
		for (L2SkillType t : types)
		{
			_datatable.remove(t.ordinal());
		}
	}
	
	@Override
	public ISkillHandler getHandler(L2SkillType skillType)
	{
		return _datatable.get(skillType.ordinal());
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static SkillHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillHandler _instance = new SkillHandler();
	}
}
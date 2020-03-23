package com.l2jhellas.gameserver.model.zone.type;

import java.util.concurrent.Future;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2WorldRegion;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;

public class L2DynamicZone extends L2ZoneType
{
	private final L2WorldRegion _region;
	private final L2Character _owner;
	private Future<?> _task;
	private final L2Skill _skill;
	
	protected void setTask(Future<?> task)
	{
		_task = task;
	}
	
	public L2DynamicZone(L2WorldRegion region, L2Character owner, L2Skill skill)
	{
		super(-1);
		_region = region;
		_owner = owner;
		_skill = skill;
		
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				remove();
			}
		};
		setTask(ThreadPoolManager.getInstance().scheduleGeneral(r, skill.getBuffDuration()));
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		try
		{
			if (character instanceof L2PcInstance)
				((L2PcInstance) character).sendMessage("You have entered a temporary zone!");
			_skill.getEffects(_owner, character);
		}
		catch (NullPointerException e)
		{
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
			((L2PcInstance) character).sendMessage("You have left a temporary zone!");
		
		if (character == _owner)
		{
			remove();
			return;
		}
		character.stopSkillEffects(_skill.getId());
	}
	
	protected void remove()
	{
		if (_task == null)
			return;
		
		_task.cancel(false);
		_task = null;
		
		_region.removeZone(this);
		for (L2Character member : _characterList.values())
		{
			try
			{
				member.stopSkillEffects(_skill.getId());
			}
			catch (NullPointerException e)
			{
			}
		}
		_owner.stopSkillEffects(_skill.getId());
		
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
		if (character == _owner)
			remove();
		else
			character.stopSkillEffects(_skill.getId());
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		_skill.getEffects(_owner, character);
	}
}
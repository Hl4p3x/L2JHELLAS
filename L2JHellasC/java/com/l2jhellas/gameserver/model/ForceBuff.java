package com.l2jhellas.gameserver.model;

import java.util.concurrent.Future;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.skills.effects.EffectForce;
import com.l2jhellas.util.Util;

public final class ForceBuff
{
	final int _skillCastRange;
	final int _forceId;
	L2PcInstance _caster;
	L2PcInstance _target;
	Future<?> _geoCheckTask;
	
	public L2PcInstance getCaster()
	{
		return _caster;
	}
	
	public L2PcInstance getTarget()
	{
		return _target;
	}
	
	public ForceBuff(L2PcInstance caster, L2PcInstance target, L2Skill skill)
	{
		_skillCastRange = skill.getCastRange();
		_caster = caster;
		_target = target;
		_forceId = skill.getForceId();
		
		L2Effect effect = _target.getFirstEffect(_forceId);
		if (effect != null)
			((EffectForce) effect).increaseForce();
		else
			SkillTable.getInstance().getInfo(_forceId, 1).getEffects(_caster, _target);
		
		_geoCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GeoCheckTask(), 1000, 1000);
	}
	
	public void delete()
	{
		_caster.setForceBuff(null);
		L2Effect effect = _target.getFirstEffect(_forceId);
		if (effect != null)
			((EffectForce) effect).decreaseForce();
		
		_geoCheckTask.cancel(true);
	}
	
	class GeoCheckTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (!Util.checkIfInRange(_skillCastRange, _caster, _target, true))
					delete();
				
				if (!GeoEngine.canSeeTarget(_caster, _target, false))
					delete();
			}
			catch (Exception e)
			{
				// ignore
			}
		}
	}
}
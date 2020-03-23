package com.l2jhellas.gameserver.skills.effects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.conditions.Condition;
import com.l2jhellas.gameserver.skills.funcs.FuncTemplate;
import com.l2jhellas.gameserver.skills.funcs.Lambda;

public final class EffectTemplate
{
	static Logger _log = Logger.getLogger(EffectTemplate.class.getName());
	
	private final Class<?> _func;
	private final Constructor<?> _constructor;
	
	public final Condition attachCond;
	public final Condition applayCond;
	public final Lambda lambda;
	public final int counter;
	public int period; // in seconds
	public final int abnormalEffect;
	public FuncTemplate[] funcTemplates;
	
	public final String stackType;
	public final float stackOrder;
	public final boolean icon;
	
	public EffectTemplate(Condition pAttachCond, Condition pApplayCond, String func, Lambda pLambda, int pCounter, int pPeriod, int pAbnormalEffect, String pStackType, float pStackOrder, boolean showicon)
	{
		attachCond = pAttachCond;
		applayCond = pApplayCond;
		lambda = pLambda;
		counter = pCounter;
		period = pPeriod;
		abnormalEffect = pAbnormalEffect;
		stackType = pStackType;
		stackOrder = pStackOrder;
		icon = showicon;
		try
		{
			_func = Class.forName("com.l2jhellas.gameserver.skills.effects.Effect" + func);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			_constructor = _func.getConstructor(Env.class, EffectTemplate.class);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public L2Effect getEffect(Env env)
	{
		if (attachCond != null && !attachCond.test(env))
			return null;
		try
		{
			L2Effect effect = (L2Effect) _constructor.newInstance(env, this);
			// if (_applayCond != null)
			// effect.setCondition(_applayCond);
			return effect;
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (InvocationTargetException e)
		{
			_log.warning(EffectTemplate.class.getName() + ": Error creating new instance of Class " + _func + " Exception was:");
			e.getTargetException().printStackTrace();
			return null;
		}
		
	}
	
	public void attach(FuncTemplate f)
	{
		if (funcTemplates == null)
		{
			funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			int len = funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			funcTemplates = tmp;
		}
	}
}
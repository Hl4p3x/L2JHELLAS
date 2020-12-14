package com.l2jhellas.gameserver.model;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.effects.EffectTemplate;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.gameserver.skills.funcs.FuncTemplate;
import com.l2jhellas.gameserver.skills.funcs.Lambda;

public abstract class L2Effect
{
	static final Logger _log = Logger.getLogger(L2Effect.class.getName());
	
	public static enum EffectState
	{
		CREATED,
		ACTING,
		FINISHING
	}
	
	public static enum EffectType
	{
		SIGNET_EFFECT,
		SIGNET,
		SIGNET_GROUND,
		BUFF,
		CHARGE,
		DMG_OVER_TIME,
		HEAL_OVER_TIME,
		COMBAT_POINT_HEAL_OVER_TIME,
		MANA_DMG_OVER_TIME,
		MANA_HEAL_OVER_TIME,
		RELAXING,
		STUN,
		ROOT,
		SLEEP,
		HATE,
		FAKE_DEATH,
		CLAN_GATE,
		CONFUSION,
		CONFUSE_MOB_ONLY,
		MUTE,
		IMMOBILEUNTILATTACKED,
		FUSION,
		FEAR,
		SILENT_MOVE,
		SEED,
		PARALYZE,
		STUN_SELF,
		PSYCHICAL_MUTE,
		REMOVE_TARGET,
		TARGET_ME,
		SILENCE_MAGIC_PHYSICAL,
		BETRAY,
		NOBLESSE_BLESSING,
		PHOENIX_BLESSING,
		PETRIFICATION,
		BLUFF,
		CHARM_OF_LUCK,
		INVINCIBLE,
		SPOIL,
		PROTECTION_BLESSING
	}
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	
	// member _effector is the instance of L2Character that cast/used the spell/skill that is
	// causing this effect. Do not confuse with the instance of L2Character that
	// is being affected by this effect.
	private final L2Character _effector;
	
	// member _effected is the instance of L2Character that was affected
	// by this effect. Do not confuse with the instance of L2Character that
	// catsed/used this effect.
	private final L2Character _effected;
	
	// the skill that was used.
	private final L2Skill _skill;
	
	// or the items that was used.
	// private final L2Item _item;
	
	// the value of an update
	private final Lambda _lambda;
	
	// the current state
	private EffectState _state;
	
	// period, seconds
	private final int _period;
	private int _periodStartTicks;
	private int _periodfirsttime;
	
	// function templates
	private final FuncTemplate[] _funcTemplates;
	
	// initial count
	private final int _totalCount;
	// counter
	private int _count;
	
	// abnormal effect mask
	private final int _abnormalEffect;
	
	public boolean preventExitUpdate;
	
	public final class EffectTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				setPeriodfirsttime(0);
				setPeriodStartTicks(GameTimeController.getInstance().getGameTicks());
				scheduleEffect();
			}
			catch (Throwable e)
			{
				
			}
		}
	}
	
	private ScheduledFuture<?> _currentFuture;
	
	private final String _stackType;
	
	private final float _stackOrder;
	
	private boolean _inUse = false;
	private boolean _startConditionsCorrect = true;
	
	protected L2Effect(Env env, EffectTemplate template)
	{
		_state = EffectState.CREATED;
		_skill = env.skill;
		// _item = env._item == null ? null : env._item.getItem();
		_effected = env.target;
		_effector = env.player;
		_lambda = template.lambda;
		_funcTemplates = template.funcTemplates;
		_count = template.counter;
		_totalCount = _count;
		if ((_skill.getId() > 2277 && _skill.getId() < 2286) || (_skill.getId() >= 2512 && _skill.getId() <= 2514))
		{
			if (_effected instanceof L2SummonInstance || (_effected instanceof L2PcInstance && ((L2PcInstance) _effected).getPet() != null))
				template.period /= 2;
		}
		if (env.skillMastery)
			template.period *= 2;
		_period = template.period;
		_abnormalEffect = template.abnormalEffect;
		_stackType = template.stackType;
		_stackOrder = template.stackOrder;
		_periodStartTicks = GameTimeController.getInstance().getGameTicks();
		_periodfirsttime = 0;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public int getTotalCount()
	{
		return _totalCount;
	}
	
	public void setCount(int newcount)
	{
		_count = Math.min(newcount, _totalCount);
	}
	
	public void setFirstTime(int newfirsttime)
	{
		_periodfirsttime = Math.min(newfirsttime, _period);
		_periodStartTicks -= _periodfirsttime * GameTimeController.TICKS_PER_SECOND;
	}
	
	public int getPeriod()
	{
		return _period;
	}
	
	public int getTime()
	{
		return (GameTimeController.getInstance().getGameTicks() - _periodStartTicks) / GameTimeController.TICKS_PER_SECOND;
	}
	
	public int getTaskTime()
	{
		if (_count == _totalCount)
			return 0;
		return (Math.abs(_count - _totalCount + 1) * _period) + getTime() + 1;
	}
	
	public boolean getInUse()
	{
		return _inUse;
	}
	
	public boolean setInUse(boolean inUse)
	{
		_inUse = inUse;
		if (_inUse)
			_startConditionsCorrect = onStart();
		else
			onExit();
		
		return _startConditionsCorrect;
	}
	
	public String getStackType()
	{
		return _stackType;
	}
	
	public float getStackOrder()
	{
		return _stackOrder;
	}
	
	public final L2Skill getSkill()
	{
		return _skill;
	}
	
	public final L2Character getEffector()
	{
		return _effector;
	}
	
	public final L2Character getEffected()
	{
		return _effected;
	}
	
	public boolean isSelfEffect()
	{
		return _skill._effectTemplatesSelf != null;
	}
	
	public boolean isHerbEffect()
	{
		if (getSkill().getName().contains("Herb"))
			return true;
		
		return false;
	}
	
	public final double calc()
	{
		Env env = new Env();
		env.player = _effector;
		env.target = _effected;
		env.skill = _skill;
		return _lambda.calc(env);
	}
	
	private final synchronized void startEffectTask()
	{
		if (_period > 0)
		{
			stopEffectTask();
			final int initialDelay = Math.max((_period - _periodfirsttime) * 1000, 5);
			if (_count > 1)
				_currentFuture = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new EffectTask(), initialDelay, _period * 1000);
			else
				_currentFuture = ThreadPoolManager.getInstance().scheduleEffect(new EffectTask(), initialDelay);
		}
		if (_state == EffectState.ACTING)
		{
			if (isSelfEffect())
				_effector.addEffect(this);
			else
				_effected.addEffect(this);
		}
	}
	
	public final void exit()
	{
		this.exit(false);
	}
	
	public final void exit(boolean preventUpdate)
	{
		preventExitUpdate = preventUpdate;
		_state = EffectState.FINISHING;
		scheduleEffect();
	}
	
	public synchronized void stopEffectTask()
	{
		// Cancel the task
		if (_currentFuture != null)
		{
			_currentFuture.cancel(false);
			_currentFuture = null;
			// To avoid possible NPE caused by player crash
			if (isSelfEffect() && getEffector() != null)
				getEffector().removeEffect(this);
			else if (getEffected() != null)
				getEffected().removeEffect(this);
		}
	}
	
	public abstract EffectType getEffectType();
	
	public boolean onStart()
	{
		if(_abnormalEffect != 0)
		{
			AbnormalEffect ae = AbnormalEffect.FindById(_abnormalEffect);
			if (ae != null && !ae.equals(AbnormalEffect.NULL))
				getEffected().startAbnormalEffect(ae);
		}
		return true;
	}
	
	public void onExit()
	{
		if(_abnormalEffect != 0)
		{
			AbnormalEffect ae = AbnormalEffect.FindById(_abnormalEffect);
			if (ae != null && !ae.equals(AbnormalEffect.NULL))
				getEffected().stopAbnormalEffect(ae);
		}
	}
	
	public abstract boolean onActionTime();
	
	public final void rescheduleEffect()
	{
		if (_state != EffectState.ACTING)
			scheduleEffect();
		else
		{
			if (_period != 0)
			{
				startEffectTask();
				return;
			}
		}
	}
	
	public final void scheduleEffect()
	{
		switch (_state)
		{
			case CREATED:
			{
				_state = EffectState.ACTING;
				
				if (_skill.isPvpSkill() && getEffected() instanceof L2PcInstance)
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					smsg.addString(_skill.getName());
					getEffected().sendPacket(smsg);
				}
				
				if (_period != 0)
				{
					startEffectTask();
					return;
				}
				// effects not having count or period should start
				_startConditionsCorrect = onStart();
			}
			case ACTING:
			{
				if (_count > 0)
				{
					_count--;
					if (getInUse())
					{ // effect has to be in use
						if (onActionTime() && _startConditionsCorrect && _count > 0)
							return; // false causes effect to finish right away
					}
					else if (_count > 0)
					{ // do not finish it yet, in case reactivated
						return;
					}
				}
				_state = EffectState.FINISHING;
			}
			case FINISHING:
			{
				// If the time left is equal to zero, send the message
				if (_count == 0 && getEffected() instanceof L2PcInstance)
				{
					SystemMessage smsg3 = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
					smsg3.addString(_skill.getName());
					getEffected().sendPacket(smsg3);
				}
				// if task is null - stopEffectTask does not remove effect
				if (_currentFuture == null && getEffected() != null)
					getEffected().removeEffect(this);
				
				// Stop the task of the L2Effect, remove it and update client magic icon
				stopEffectTask();
				
				// Cancel the effect in the the abnormal effect map of the L2Character
				if (getInUse() || !(_count > 1 || _period > 0))
					if (_startConditionsCorrect)
						onExit();
			}
		}
	}
	
	public Func[] getStatFuncs()
	{
		if (_funcTemplates == null)
			return _emptyFunctionSet;
		ArrayList<Func> funcs = new ArrayList<>(_funcTemplates.length);
		
		Env env = new Env();
		env.player = getEffector();
		env.target = getEffected();
		env.skill = getSkill();
		Func f;
		
		for (FuncTemplate t : _funcTemplates)
		{
			f = t.getFunc(env, this); // effect is owner
			if (f != null)
				funcs.add(f);
		}
		if (funcs.isEmpty())
			return _emptyFunctionSet;
		
		return funcs.toArray(new Func[funcs.size()]);
	}

	public ScheduledFuture<?> getFuture()
	{
		return _currentFuture;
	}

	public boolean onSameEffect(L2Effect effect)
	{
		return true;
	}
	
	public int getLevel()
	{
		return getSkill().getLevel();
	}
	
	public int getPeriodfirsttime()
	{
		return _periodfirsttime;
	}
	
	public void setPeriodfirsttime(int periodfirsttime)
	{
		_periodfirsttime = periodfirsttime;
	}
	
	public int getPeriodStartTicks()
	{
		return _periodStartTicks;
	}
	
	public void setPeriodStartTicks(int periodStartTicks)
	{
		_periodStartTicks = periodStartTicks;
	}
}
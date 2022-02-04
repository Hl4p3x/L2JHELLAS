package com.l2jhellas.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.handler.SkillHandler;
import com.l2jhellas.gameserver.instancemanager.DuelManager;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jhellas.util.Rnd;

public class L2CubicInstance
{
	protected static final Logger _log = Logger.getLogger(L2CubicInstance.class.getName());
	
	public static final int STORM_CUBIC = 1;
	public static final int VAMPIRIC_CUBIC = 2;
	public static final int LIFE_CUBIC = 3;
	public static final int VIPER_CUBIC = 4;
	public static final int POLTERGEIST_CUBIC = 5;
	public static final int BINDING_CUBIC = 6;
	public static final int AQUA_CUBIC = 7;
	public static final int SPARK_CUBIC = 8;
	public static final int ATTRACT_CUBIC = 9;
	
	protected L2PcInstance _owner;
	protected L2Character _target;
	
	protected int _id;
	protected int _level = 1;
	protected long _lifetime = 1200000; // disappear in 20 mins
	
	protected List<Integer> _skills = new ArrayList<>();
	
	private Future<?> _disappearTask;
	private Future<?> _actionTask;
	
	public L2CubicInstance(L2PcInstance owner, int id, int level)
	{
		_owner = owner;
		_id = id;
		_level = level;
		
		switch (_id)
		{
			case STORM_CUBIC:
				_skills.add(4049);
				break;
			case VAMPIRIC_CUBIC:
				_skills.add(4050);
				break;
			case LIFE_CUBIC:
				_skills.add(4051);
				_lifetime = 3600000; // disappear in 60 mins
				doAction(_owner);
				break;
			case VIPER_CUBIC:
				_skills.add(4052);
				break;
			case POLTERGEIST_CUBIC:
				_skills.add(4053);
				_skills.add(4054);
				_skills.add(4055);
				break;
			case BINDING_CUBIC:
				_skills.add(4164);
				break;
			case AQUA_CUBIC:
				_skills.add(4165);
				break;
			case SPARK_CUBIC:
				_skills.add(4166);
				break;
			case ATTRACT_CUBIC:
				_skills.add(5115);
				_skills.add(5116);
				break;
		}
		
		if (_disappearTask == null)
			_disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new Disappear(), _lifetime);
	}
	
	public void doAction(L2Character target)
	{
		if (_target == target)
			return;
		
		stopAction();
		
		_target = target;
		
		switch (_id)
		{
			case STORM_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(12), 0, 10000);
				break;
			case VAMPIRIC_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(8), 0, 15000);
				break;
			case VIPER_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(30), 0, 20000);
				break;
			case POLTERGEIST_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(30), 0, 8000);
				break;
			case BINDING_CUBIC:
			case AQUA_CUBIC:
			case SPARK_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(30), 0, 8000);
				break;
			case LIFE_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(50), 0, 30000);
				break;
			case ATTRACT_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(30), 0, 8000);
				break;
		}
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public void stopAction()
	{
		_target = null;
		if (_actionTask != null)
		{
			_actionTask.cancel(true);
			_actionTask = null;
		}
	}
	
	public void cancelDisappear()
	{
		if (_disappearTask != null)
		{
			_disappearTask.cancel(true);
			_disappearTask = null;
		}
	}
	
	private class Action implements Runnable
	{
		private final int _chance;
		
		Action(int chance)
		{
			_chance = chance;
			// run task
		}
		
		@Override
		public void run()
		{
			if (_owner.isDead() || !_owner.isOnline())
			{
				if(_owner != null)
					_owner.delCubic(_id);

				if (_owner.isOnline())
					_owner.broadcastUserInfo();
				
				cancelDisappear();
				stopAction();
				return;
			}
			
			if (!AttackStanceTaskManager.getInstance().isInAttackStance(_owner))
			{
				stopAction();
				return;
			}
			
			final int range = 900;

			if (Rnd.get(1, 100) < _chance )
			{
				if (_target != null && !_target.isDead() &&_target.isInsideRadius(_owner, range,true,false))
				{
					try
					{
						final L2Skill skill = SkillTable.getInstance().getInfo(_skills.get(Rnd.get(_skills.size())), _level);
						if (skill != null)
						{
							final L2Character[] targets ={_target};
							final ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());

							if (handler != null)
								handler.useSkill(_owner, skill, targets);	
							else
								skill.useSkill(_owner, targets);

							_owner.broadcastPacket(new MagicSkillUse(_owner, _target, skill.getId(), _level, 0, 0));

						}
					}
					catch (Exception e)
					{

					}
				}
			}		
		}
	}
	
	private class Heal implements Runnable
	{
		private final int _chance;
		
		Heal(int chance)
		{
			_chance = chance;
			// run task
		}
		
		@Override
		public void run()
		{
			if (_owner.isDead())
			{
				stopAction();
				_owner.delCubic(_id);
				_owner.broadcastUserInfo();
				cancelDisappear();
				return;
			}
			try
			{
				if (Rnd.get(1, 100) < _chance)
				{
					final L2Skill skill = SkillTable.getInstance().getInfo(_skills.get(Rnd.get(_skills.size())), _level);
					if (skill != null)
					{
						L2Character target;
						target = null;
						if (_owner.isInParty())
						{
							L2PcInstance player = _owner;
							L2Party party = player.getParty();
							
							if (_owner.isInDuel() && !DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
								party = null;
							
							if (party != null && !_owner.isInOlympiadMode())
								target = party.getPartyMembers().stream().filter(member -> member != null && !member.isDead() && member.isInsideRadius(_owner, 900,true,false)).sorted(Comparator.comparingInt(L2Character::getCurrentHpPercent).reversed()).findFirst().orElse(null);
						}
						else
						{
							if (_owner.getCurrentHp() < _owner.getMaxHp())
								target = _owner;
						}
						if (target != null)
						{
							L2Character[] targets ={target};
							ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
							if (handler != null)
								handler.useSkill(_owner, skill, targets);
							else
								skill.useSkill(_owner, targets);
							
							_owner.broadcastPacket(new MagicSkillUse(_owner, target, skill.getId(), _level, 0, 0));
						}
					}
				}
			}
			catch (Exception e)
			{
			}
		}
	}
	
	private class Disappear implements Runnable
	{
		Disappear()
		{
			// run task
		}
		
		@Override
		public void run()
		{
			stopAction();
			_owner.delCubic(_id);
			_owner.broadcastUserInfo();
		}
	}
}
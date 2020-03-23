package com.l2jhellas.gameserver.model.zone.type;

import java.util.concurrent.Future;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.L2CastleZoneType;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Stats;

public class L2DamageZone extends L2CastleZoneType
{
	private int _hpDps;
	private Future<?> _task;
	
	private int _startTask;
	private int _reuseTask;
	
	private String _target = "L2Playable"; // default only playable
	
	public L2DamageZone(int id)
	{
		super(id);
		
		_hpDps = 100; // setup default damage
		
		// Setup default start / reuse time
		_startTask = 10;
		_reuseTask = 5000;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("dmgSec"))
			_hpDps = Integer.parseInt(value);
		else if (name.equalsIgnoreCase("initialDelay"))
			_startTask = Integer.parseInt(value);
		else if (name.equalsIgnoreCase("reuse"))
			_reuseTask = Integer.parseInt(value);
		else if (name.equals("targetClass"))
			_target = value;
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected boolean isAffected(L2Character character)
	{
		// check obj class
		try
		{
			if (!(Class.forName("com.l2jhellas.gameserver.model.actor." + _target).isInstance(character)))
				return false;
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (_task == null && _hpDps != 0)
		{
			// Castle traps are active only during siege, or if they're activated.
			if (getCastle() != null && (!isEnabled() || !getCastle().getSiege().getIsInProgress()))
				return;
			
			synchronized (this)
			{
				if (_task == null)
				{
					_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplyDamage(this), _startTask, _reuseTask);
					// Message for castle traps.
					if (getCastle() != null)
						getCastle().getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_TRIPPED).toString(), false);
				}
			}
		}
		
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(ZoneId.DANGER_AREA, true);
			character.sendPacket(new EtcStatusUpdate((L2PcInstance) character));
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(ZoneId.DANGER_AREA, false);
			if (!character.isInsideZone(ZoneId.DANGER_AREA))
				character.sendPacket(new EtcStatusUpdate((L2PcInstance) character));
		}
	}
	
	protected int getHpDps()
	{
		return _hpDps;
	}
	
	protected void stopTask()
	{
		if (_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
	}
	
	class ApplyDamage implements Runnable
	{
		private final L2DamageZone _dmgZone;
		
		ApplyDamage(L2DamageZone zone)
		{
			_dmgZone = zone;
		}
		
		@Override
		public void run()
		{
			// Cancels the task if config has changed, if castle isn't in siege anymore, or if zone isn't enabled.
			if (_dmgZone.getHpDps() <= 0 || (_dmgZone.getCastle() != null && (!_dmgZone.isEnabled() || !_dmgZone.getCastle().getSiege().getIsInProgress())))
			{
				_dmgZone.stopTask();
				return;
			}
			
			// Cancels the task if characters list is empty.
			if (_dmgZone.getCharactersInside().isEmpty())
			{
				_dmgZone.stopTask();
				return;
			}
			
			// Effect all people inside the zone.
			for (L2Character temp : _dmgZone.getCharactersInside())
			{
				if (temp != null && !temp.isDead())
					temp.reduceCurrentHp(_dmgZone.getHpDps() * (1 + (temp.calcStat(Stats.DAMAGE_ZONE_VULN, 0, null, null) / 100)), null);
			}
		}
	}
}
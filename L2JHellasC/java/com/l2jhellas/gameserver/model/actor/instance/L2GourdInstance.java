package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.taskmanager.DecayTaskManager;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public final class L2GourdInstance extends L2MonsterInstance
{
	private String _name;
	private byte _nectar = 0;
	private byte _good = 0;
	
	public L2GourdInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		DecayTaskManager.getInstance().addDecayTask(this, 180000);
	}
	
	public void setOwner(String name)
	{
		_name = name;
	}
	
	public String getOwner()
	{
		return _name;
	}
	
	public void addNectar()
	{
		_nectar++;
	}
	
	public byte getNectar()
	{
		return _nectar;
	}
	
	public void addGood()
	{
		_good++;
	}
	
	public byte getGood()
	{
		return _good;
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		if (attacker.getName() != getOwner())
		{
			damage = 0;
		}
		
		if (getTemplate().npcId == 12778 || getTemplate().npcId == 12779)
		{
			if (attacker.getActiveWeaponInstance().getItemId() == 4202 || attacker.getActiveWeaponInstance().getItemId() == 5133 || attacker.getActiveWeaponInstance().getItemId() == 5817 || attacker.getActiveWeaponInstance().getItemId() == 7058)
			{
				super.reduceCurrentHp(damage, attacker, awake);
			}
			else if (damage > 0)
			{
				damage = 0;
			}
		}
		super.reduceCurrentHp(damage, attacker, awake);
	}
	
}
package com.l2jhellas.gameserver.model;

import java.lang.reflect.Constructor;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public class L2GroupSpawn extends L2Spawn
{
	private final Constructor<?> _constructor;
	private final L2NpcTemplate _template;
	
	public L2GroupSpawn(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException
	{
		super(mobTemplate);
		_constructor = Class.forName("com.l2jhellas.gameserver.model.actor.instance.L2ControllableMobInstance").getConstructors()[0];
		_template = mobTemplate;
		
		setAmount(1);
	}
	
	public L2Npc doGroupSpawn()
	{
		L2Npc mob = null;
		
		try
		{
			if (_template.type.equalsIgnoreCase("L2Pet") || _template.type.equalsIgnoreCase("L2Minion"))
				return null;
			
			Object[] parameters =
			{
				IdFactory.getInstance().getNextId(),
				_template
			};
			Object tmp = _constructor.newInstance(parameters);
			
			if (!(tmp instanceof L2Npc))
				return null;
			
			mob = (L2Npc) tmp;
			
			int newlocx, newlocy, newlocz;
			
			newlocx = getLocx();
			newlocy = getLocy();
			newlocz = getLocz();
			
			mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
			
			if (getHeading() == -1)
				mob.setHeading(Rnd.nextInt(61794));
			else
				mob.setHeading(getHeading());
			
			mob.setSpawn(this);
			mob.spawnMe(newlocx, newlocy, newlocz);
			mob.onSpawn();
			
			if (Config.DEBUG)
				_log.finest("spawned Mob ID: " + _template.npcId + " ,at: " + mob.getX() + " x, " + mob.getY() + " y, " + mob.getZ() + " z");
			
			return mob;
			
		}
		catch (Exception e)
		{
			_log.warning(L2GroupSpawn.class.getName() + ": NPC class not found: " + e);
			return null;
		}
	}
}
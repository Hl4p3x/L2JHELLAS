package com.l2jhellas.gameserver.skills.l2skills;

import java.util.logging.Level;

import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.Rnd;

public class L2SkillSpawn extends L2Skill
{
	private final int _npcId;
	private final int _despawnDelay;
	private final boolean _randomOffset;
	
	public L2SkillSpawn(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId", 0);
		_despawnDelay = set.getInteger("despawnDelay", 0);
		_randomOffset = set.getBool("randomOffset", true);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead())
			return;
		
		if (_npcId == 0)
		{
			_log.warning("npc id not defined for skill id: " + getId());
			return;
		}
		
		final L2NpcTemplate template = NpcData.getInstance().getTemplate(_npcId);
		if (template == null)
		{
			_log.warning("Spawn of the nonexisting npc id: " + _npcId + ", skill id: " + getId());
			return;
		}
		
		try
		{
			final L2Spawn spawn = new L2Spawn(template);
			
			int x = caster.getX();
			int y = caster.getY();
			if (_randomOffset)
			{
				x += Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20);
				y += Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20);
			}
			spawn.setLocx(x);
			spawn.setLocy(y);
			spawn.setLocz(caster.getZ() + 20);
			
			spawn.stopRespawn();
			L2Npc npc = spawn.doSpawn();
			
			if (_despawnDelay > 0)
				npc.scheduleDespawn(_despawnDelay);
			
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception while spawning NPC ID: " + _npcId + ", skill ID: " + getId() + ", exception: " + e.getMessage(), e);
		}
	}
}
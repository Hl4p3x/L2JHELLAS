package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public final class L2ChestInstance extends L2MonsterInstance
{
	private volatile boolean _isInteracted;
	private volatile boolean _specialDrop;
	
	public L2ChestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_isInteracted = false;
		_specialDrop = false;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_isInteracted = false;
		_specialDrop = false;
		setMustRewardExpSp(true);
	}
	
	public synchronized boolean isInteracted()
	{
		return _isInteracted;
	}
	
	public synchronized void setInteracted()
	{
		_isInteracted = true;
	}
	
	public synchronized boolean isSpecialDrop()
	{
		return _specialDrop;
	}
	
	public synchronized void setSpecialDrop()
	{
		_specialDrop = true;
	}
	
	@Override
	public void doItemDrop(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		int id = getTemplate().npcId;
		
		if (!_specialDrop)
		{
			if (id >= 18265 && id <= 18286)
				id += 3536;
			else if (id == 18287 || id == 18288)
				id = 21671;
			else if (id == 18289 || id == 18290)
				id = 21694;
			else if (id == 18291 || id == 18292)
				id = 21717;
			else if (id == 18293 || id == 18294)
				id = 21740;
			else if (id == 18295 || id == 18296)
				id = 21763;
			else if (id == 18297 || id == 18298)
				id = 21786;
		}
		
		super.doItemDrop(NpcData.getInstance().getTemplate(id), lastAttacker);
	}
	
	// cast - trap chest
	public void chestTrap(L2Character player)
	{
		int trapSkillId = 0;
		int rnd = Rnd.get(120);
		
		if (getTemplate().level >= 61)
		{
			if (rnd >= 90)
				trapSkillId = 4139;// explosion
			else if (rnd >= 50)
				trapSkillId = 4118;// area paralysis
			else if (rnd >= 20)
				trapSkillId = 1167;// poison cloud
			else
				trapSkillId = 223;// sting
		}
		else if (getTemplate().level >= 41)
		{
			if (rnd >= 90)
				trapSkillId = 4139;// explosion
			else if (rnd >= 60)
				trapSkillId = 96;// bleed
			else if (rnd >= 20)
				trapSkillId = 1167;// poison cloud
			else
				trapSkillId = 4118;// area paralysis
		}
		else if (getTemplate().level >= 21)
		{
			if (rnd >= 80)
				trapSkillId = 4139;// explosion
			else if (rnd >= 50)
				trapSkillId = 96;// bleed
			else if (rnd >= 20)
				trapSkillId = 1167;// poison cloud
			else
				trapSkillId = 129;// poison
		}
		else
		{
			if (rnd >= 80)
				trapSkillId = 4139;// explosion
			else if (rnd >= 50)
				trapSkillId = 96;// bleed
			else
				trapSkillId = 129;// poison
		}
		
		player.sendPacket(SystemMessage.sendString("There was a trap!"));
		handleCast(player, trapSkillId);
	}
	
	// <--
	// cast case
	// <--
	private boolean handleCast(L2Character player, int skillId)
	{
		int skillLevel = 1;
		byte lvl = getTemplate().level;
		if (lvl > 20 && lvl <= 40)
			skillLevel = 3;
		else if (lvl > 40 && lvl <= 60)
			skillLevel = 5;
		else if (lvl > 60)
			skillLevel = 6;
		
		if (player.isDead() || !player.isVisible() || !player.isInsideRadius(this, getDistanceToWatchObject(player), false, false))
			return false;
		
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		
		if (player.getFirstEffect(skill) == null)
		{
			skill.getEffects(this, player);
			broadcastPacket(new MagicSkillUse(this, player, skill.getId(), skillLevel, skill.getHitTime(), 0));
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		if (super.isMovementDisabled())
			return true;
		if (isInteracted())
			return false;
		return true;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}
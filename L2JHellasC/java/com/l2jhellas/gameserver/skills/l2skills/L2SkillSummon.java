package com.l2jhellas.gameserver.skills.l2skills;

import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jhellas.gameserver.model.base.Experience;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;

public class L2SkillSummon extends L2Skill
{
	private final int _npcId;
	private final float _expPenalty;
	private final boolean _isCubic;
	
	public L2SkillSummon(StatsSet set)
	{
		super(set);
		
		_npcId = set.getInteger("npcId", 0); // default for undescribed skills
		_expPenalty = set.getFloat("expPenalty", 0.f);
		_isCubic = set.getBool("isCubic", false);
	}
	
	public boolean checkCondition(L2Character activeChar)
	{
		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) activeChar;
			if (_isCubic)
			{
				if (getTargetType() != L2SkillTargetType.TARGET_SELF)
					return true; // Player is always able to cast mass cubic skill
				int mastery = player.getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY);
				if (mastery < 0)
				{
					mastery = 0;
				}
				int count = player.getCubics().size();
				if (count > mastery)
				{
					player.sendPacket(SystemMessageId.CUBIC_SUMMONING_FAILED);
					return false;
				}
			}
			else
			{
				if (player.inObserverMode())
					return false;
				if (player.getPet() != null)
				{
					player.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
					return false;
				}
			}
		}
		return super.checkCondition(activeChar, null, false);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead() || !(caster instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) caster;
		
		// Skill 2046 only used for animation
		if (getId() == 2046)
			return;
		
		if (_npcId == 0)
		{
			activeChar.sendMessage("Summon skill " + getId() + " not described yet");
			return;
		}
		
		if (_isCubic)
		{
			if (targets.length > 1) // Mass cubic skill
			{
				for (L2Object obj : targets)
				{
					if (!(obj instanceof L2PcInstance))
					{
						continue;
					}
					L2PcInstance player = ((L2PcInstance) obj);
					int mastery = player.getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY);
					if (mastery < 0)
					{
						mastery = 0;
					}
					if (mastery == 0 && player.getCubics().size() > 0)
					{
						// Player can have only 1 cubic - we shuld replace old cubic with new one
						for (L2CubicInstance c : player.getCubics().values())
						{
							c.stopAction();
							c = null;
						}
						player.getCubics().clear();
					}
					if (player.getCubics().size() > mastery)
					{
						continue;
					}
					if (player.getCubics().containsKey(_npcId))
					{
						player.sendMessage("You already have such cubic");
					}
					else
					{						
						player.addCubic(_npcId, getLevel());
						player.broadcastUserInfo();
					}
				}
				return;
			}
			
			int mastery = activeChar.getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY);
			if (mastery < 0)
			{
				mastery = 0;
			}
			if (activeChar.getCubics().size() > mastery)
			{
				activeChar.sendPacket(SystemMessageId.CUBIC_SUMMONING_FAILED);
				return;
			}
			if (activeChar.getCubics().containsKey(_npcId))
			{
				activeChar.sendMessage("You already have such cubic");
				return;
			}
			activeChar.addCubic(_npcId, getLevel());
			activeChar.broadcastUserInfo();
			return;
		}
		
		if (activeChar.getPet() != null || activeChar.isMounted())
			return;
		
		L2SummonInstance summon;
		L2NpcTemplate summonTemplate = NpcData.getInstance().getTemplate(_npcId);
		if (summonTemplate.type.equalsIgnoreCase("L2SiegeSummon"))
		{
			summon = new L2SiegeSummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
		}
		else
		{
			summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
		}
		
		summon.setName(summonTemplate.name);
		summon.setTitle(activeChar.getName());
		summon.setExpPenalty(_expPenalty);
		if (summon.getLevel() >= Experience.MAX_LEVEL)
		{
			summon.getStat().setExp(Experience.LEVEL[Experience.MAX_LEVEL - 1]);
			_log.warning(L2SkillSummon.class.getName() + ": Summon (" + summon.getName() + ") NpcID: " + summon.getNpcId() + " has a level above 75. Please rectify.");
		}
		else
		{
			summon.getStat().setExp(Experience.LEVEL[summon.getLevel() % Experience.MAX_LEVEL]);
		}
		summon.setCurrentHp(summon.getMaxHp());
		summon.setCurrentMp(summon.getMaxMp());
		summon.setHeading(activeChar.getHeading());
		summon.setRunning();
		activeChar.setPet(summon);
		
		summon.spawnMe(activeChar.getX() + 30, activeChar.getY() + 30, activeChar.getZ());

		summon.setFollowStatus(true);
	}
	
	public final boolean isCubic()
	{
		return _isCubic;
	}
}
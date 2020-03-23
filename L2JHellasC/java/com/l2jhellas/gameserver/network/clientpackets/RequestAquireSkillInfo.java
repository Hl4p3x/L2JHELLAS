package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.SkillSpellbookData;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.model.L2PledgeSkillLearn;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2SkillLearn;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.AcquireSkillInfo;
import com.l2jhellas.gameserver.skills.SkillTable;

public class RequestAquireSkillInfo extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestAquireSkillInfo.class.getName());
	private static final String _C__6B_REQUESTAQUIRESKILLINFO = "[C] 6B RequestAquireSkillInfo";
	
	private int _id;
	private int _level;
	private int _skillType;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_level = readD();
		_skillType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2NpcInstance trainer = activeChar.getLastFolkNPC();
		
		if (((trainer == null) || !activeChar.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false)) && !activeChar.isGM())
			return;
		
		final L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		
		boolean canteach = false;
		
		if (skill == null)
		{
			if (Config.DEBUG)
				_log.warning(RequestAquireSkillInfo.class.getName() + ": skill id " + _id + " level " + _level + " is undefined. aquireSkillInfo failed.");
			return;
		}
		
		if (_skillType == 0)
		{
			if(trainer==null)
				return;
			
			if (!trainer.getTemplate().canTeach(activeChar.getSkillLearningClassId()))
				return; // cheater
				
			final L2SkillLearn[] skills = SkillTreeData.getInstance().getAvailableSkills(activeChar, activeChar.getSkillLearningClassId());
			
			for (L2SkillLearn s : skills)
			{
				if (s.getId() == _id && s.getLevel() == _level)
				{
					canteach = true;
					break;
				}
			}
			
			if (!canteach)
				return; // cheater
				
			final int requiredSp = SkillTreeData.getInstance().getSkillCost(activeChar, skill);
			final AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp, 0);
			
			if (Config.SP_BOOK_NEEDED)
			{
				final int spbId = SkillSpellbookData.getInstance().getBookForSkill(skill);
				
				if (skill.getLevel() == 1 && spbId > -1)
					asi.addRequirement(99, spbId, 1, 50);
			}
			
			sendPacket(asi);
		}
		else if (_skillType == 2)
		{
			int requiredRep = 0;
			int itemId = 0;
			final L2PledgeSkillLearn[] skills = SkillTreeData.getInstance().getAvailablePledgeSkills(activeChar);
			
			for (L2PledgeSkillLearn s : skills)
			{
				if (s.getId() == _id && s.getLevel() == _level)
				{
					canteach = true;
					requiredRep = s.getRepCost();
					itemId = s.getItemId();
					break;
				}
			}
			
			if (!canteach)
				return; // cheater
				
			final AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), requiredRep, 2);
			
			if (Config.LIFE_CRYSTAL_NEEDED)
			{
				asi.addRequirement(1, itemId, 1, 0);
			}
			
			sendPacket(asi);
		}
		else
		// Common Skills
		{
			int costid = 0;
			int costcount = 0;
			int spcost = 0;
			
			final L2SkillLearn[] skillsc = SkillTreeData.getInstance().getAvailableSkills(activeChar);
			
			for (L2SkillLearn s : skillsc)
			{
				final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				
				if (sk == null || sk != skill)
					continue;
				
				canteach = true;
				costid = s.getIdCost();
				costcount = s.getCostCount();
				spcost = s.getSpCost();
			}
			
			final AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), spcost, 1);
			asi.addRequirement(4, costid, costcount, 0);
			sendPacket(asi);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__6B_REQUESTAQUIRESKILLINFO;
	}
}
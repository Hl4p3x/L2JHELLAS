package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.SkillSpellbookData;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.holder.ClanSkillNode;
import com.l2jhellas.gameserver.holder.FishingSkillNode;
import com.l2jhellas.gameserver.holder.GeneralSkillNode;
import com.l2jhellas.gameserver.model.L2Skill;
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
		
		
		if (skill == null)
		{
			if (Config.DEBUG)
				_log.warning(RequestAquireSkillInfo.class.getName() + ": skill id " + _id + " level " + _level + " is undefined. aquireSkillInfo failed.");
			return;
		}
		
		final AcquireSkillInfo asi;

		if (_skillType == 0)
		{
			if(trainer==null)
				return;
			
			// Player already has such skill with same or higher level.
			int skillLvl = activeChar.getSkillLevel(_id);
			if (skillLvl >= _level)
				return;
			
			// Requested skill must be 1 level higher than existing skill.
			if (skillLvl != _level - 1)
				return;
			
			if (!trainer.getTemplate().canTeach(activeChar.getSkillLearningClassId()))
				return; // cheater
				
			// Search if the asked skill exists on player template.
			final GeneralSkillNode gsn = activeChar.getTemplate().findSkill(_id, _level);
			if (gsn != null)
			{
				asi = new AcquireSkillInfo(_id, _level, gsn.getCorrectedCost(), 0);
				final int bookId = SkillSpellbookData.getInstance().getBookForSkill(_id, _level);
				if (bookId != 0)
					asi.addRequirement(99, bookId, 1, 50);
				sendPacket(asi);
			}
		}
		else if (_skillType == 2)
		{
			if (!activeChar.isClanLeader())
				return;
			
			final ClanSkillNode csn = SkillTreeData.getInstance().getClanSkillFor(activeChar, _id, _level);
			if (csn != null)
			{
				asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), csn.getCost(), 2);
				if (Config.LIFE_CRYSTAL_NEEDED && csn.getItemId() != 0)
					asi.addRequirement(1, csn.getItemId(), 1, 0);
				sendPacket(asi);
			}
		}
		else if (_skillType == 1)
		{
			// Player already has such skill with same or higher level.
			int skillLvl = activeChar.getSkillLevel(_id);
			if (skillLvl >= _level)
				return;
			
			// Requested skill must be 1 level higher than existing skill.
			if (skillLvl != _level - 1)
				return;
			
			final FishingSkillNode fsn = SkillTreeData.getInstance().getFishingSkillFor(activeChar,_id, _level);
			if (fsn != null)
			{
				asi = new AcquireSkillInfo(_id, _level, 0, 1);
				asi.addRequirement(4, fsn.getItemId(), fsn.getItemCount(), 0);
				sendPacket(asi);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__6B_REQUESTAQUIRESKILLINFO;
	}
}
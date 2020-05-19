package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.model.L2EnchantSkillLearn;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ExEnchantSkillInfo;
import com.l2jhellas.gameserver.skills.SkillTable;

public final class RequestExEnchantSkillInfo extends L2GameClientPacket
{
	private static final String _C__D0_06_REQUESTEXENCHANTSKILLINFO = "[C] D0:06 RequestExEnchantSkillInfo";
	private int _skillId;
	private int _skillLvl;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (_skillId <= 0 || _skillLvl <= 0)
			return;
		
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (activeChar.getLevel() < 76)
			return;
		
		final L2NpcInstance trainer = activeChar.getLastFolkNPC();
		
		if (((trainer == null) || !activeChar.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false)) && !activeChar.isGM())
			return;
		
		if (activeChar.getSkillLevel(_skillId) >= _skillLvl)
			return;
		
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		
		boolean canteach = false;
		
		if ((skill == null) || (skill.getId() != _skillId))
			return;
		
		if(trainer==null)
			return;
		
		if (!trainer.getTemplate().canTeach(activeChar.getClassId()))
			return; // cheater
			
		L2EnchantSkillLearn[] skills = SkillTreeData.getInstance().getAvailableEnchantSkills(activeChar);
		
		for (L2EnchantSkillLearn s : skills)
		{
			if (s.getId() == _skillId && s.getLevel() == _skillLvl)
			{
				canteach = true;
				break;
			}
		}
		
		if (!canteach)
			return; // cheater
			
		int requiredSp = SkillTreeData.getInstance().getSkillSpCost(activeChar, skill);
		int requiredExp = SkillTreeData.getInstance().getSkillExpCost(activeChar, skill);
		byte rate = SkillTreeData.getInstance().getSkillRate(activeChar, skill);
		ExEnchantSkillInfo asi = new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), requiredSp, requiredExp, rate);
		
		if (Config.ES_SP_BOOK_NEEDED && (skill.getLevel() == 101 || skill.getLevel() == 141)) // only first lvl requires book
		{
			int spbId = 6622;
			asi.addRequirement(4, spbId, 1, 0);
		}
		sendPacket(asi);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_06_REQUESTEXENCHANTSKILLINFO;
	}
}
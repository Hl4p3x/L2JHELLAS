package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.holder.EnchantSkillNode;
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
				
		if ((skill == null) || (skill.getId() != _skillId))
			return;
		
		if(trainer==null)
			return;
		
		if (!trainer.getTemplate().canTeach(activeChar.getClassId()))
			return; // cheater
			
		final EnchantSkillNode esn = SkillTreeData.getInstance().getEnchantSkillFor(activeChar, _skillId, _skillLvl);
		if (esn == null)
			return;
		
		final ExEnchantSkillInfo esi = new ExEnchantSkillInfo(_skillId, _skillLvl, esn.getSp(), esn.getExp(), esn.getEnchantRate(activeChar.getLevel()));
		if (Config.ES_SP_BOOK_NEEDED && esn.getItem() != null)
			esi.addRequirement(4, esn.getItem().getId(), esn.getItem().getValue(), 0);
		
		sendPacket(esi);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_06_REQUESTEXENCHANTSKILLINFO;
	}
}
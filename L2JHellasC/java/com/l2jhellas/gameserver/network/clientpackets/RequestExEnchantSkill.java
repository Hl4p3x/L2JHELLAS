package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.holder.EnchantSkillNode;
import com.l2jhellas.gameserver.model.L2ShortCut;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.Rnd;

public final class RequestExEnchantSkill extends L2GameClientPacket
{
	private static final String _C__D0_07_REQUESTEXENCHANTSKILL = "[C] D0:07 RequestExEnchantSkill";
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
		
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		L2NpcInstance trainer = player.getLastFolkNPC();
		if (trainer == null)
			return;
				
		if (!player.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false) && !player.isGM())
			return;
		
		if (player.getSkillLevel(_skillId) >= _skillLvl)// already knows the skill with this level
			return;
		
		if (player.getClassId().getId() < 88) // requires to have 3rd class quest completed
			return;
		
		if (player.getLevel() < 76)
			return;
		
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		
		if (skill == null)
			return;
		
		final EnchantSkillNode esn = SkillTreeData.getInstance().getEnchantSkillFor(player, _skillId, _skillLvl);
		if (esn == null)
			return;

		if (player.getSp() < esn.getSp())
		{
			player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		if (player.getExp() < esn.getExp())
		{
			player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL);
			return;
		}

		// Check item restriction, and try to consume item.
		if (Config.ES_SP_BOOK_NEEDED && esn.getItem() != null && !player.destroyItemByItemId("SkillEnchant", esn.getItem().getId(), esn.getItem().getValue(), trainer, true))
		{
			player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		if (Rnd.get(100) <= esn.getEnchantRate(player.getLevel()))
		{
			player.addSkill(skill, true);
			player.getStat().removeExpAndSp(esn.getExp(), esn.getSp());
			
			StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.SP, player.getSp());
			player.sendPacket(su);
			
			SystemMessage ep = SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
			ep.addNumber(esn.getExp());
			sendPacket(ep);
			
			SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
			sp.addNumber(esn.getSp());
			sendPacket(sp);
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		else
		{
			if (skill.getLevel() > 100)
			{
				_skillLvl = SkillTable.getInstance().getMaxLevel(_skillId,0);
				player.addSkill(SkillTable.getInstance().getInfo(_skillId, _skillLvl), true);
				player.sendSkillList();
			}
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		
		if (trainer != null && player != null && player.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false))
		    trainer.showEnchantSkillList(player, player.getClassId());
		
		// update all the shortcuts to this skill
		L2ShortCut[] allShortCuts = player.getAllShortCuts();
		
		for (L2ShortCut sc : allShortCuts)
		{
			if (sc.getId() == _skillId && sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _skillLvl, 1);
				player.sendPacket(new ShortCutRegister(player, newsc));
				player.registerShortCut(newsc);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_07_REQUESTEXENCHANTSKILL;
	}
}
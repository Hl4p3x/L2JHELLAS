package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.model.L2EnchantSkillLearn;
import com.l2jhellas.gameserver.model.L2ShortCut;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.IllegalPlayerAction;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public final class RequestExEnchantSkill extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestAquireSkill.class.getName());
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
		
		int npcid = trainer.getNpcId();
		
		if (!player.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false) && !player.isGM())
			return;
		
		if (player.getSkillLevel(_skillId) >= _skillLvl)// already knows the skill with this level
			return;
		
		if (player.getClassId().getId() < 88) // requires to have 3rd class quest completed
			return;
		
		if (player.getLevel() < 76)
			return;
		
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		
		int counts = 0;
		int _requiredSp = 10000000;
		int _requiredExp = 100000;
		byte _rate = 0;
		int _baseLvl = 1;
		
		L2EnchantSkillLearn[] skills = SkillTreeData.getInstance().getAvailableEnchantSkills(player);
		
		for (L2EnchantSkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null || sk != skill || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcid))
				continue;
			counts++;
			_requiredSp = s.getSpCost();
			_requiredExp = s.getExp();
			_rate = s.getRate(player);
			_baseLvl = s.getBaseLevel();
		}
		
		if (counts == 0 && !Config.ALT_GAME_SKILL_LEARN)
		{
			player.sendMessage("You are trying to learn skill that u can't..");
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		if (player.getSp() >= _requiredSp)
		{
			if (player.getExp() >= _requiredExp)
			{
				if (Config.ES_SP_BOOK_NEEDED && (_skillLvl == 101 || _skillLvl == 141)) // only first lvl requires book
				{
					int spbId = 6622;
					
					L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);
					
					if (spb == null)// Haven't spellbook
					{
						player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
						return;
					}
					// ok
					player.destroyItem("Consume", spb, trainer, true);
				}
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL);
				player.sendPacket(sm);
				return;
			}
		}
		else
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			player.sendPacket(sm);
			return;
		}
		if (Rnd.get(100) <= _rate)
		{
			player.addSkill(skill, true);
			
			if (Config.DEBUG)
				_log.fine("Learned skill " + _skillId + " for " + _requiredSp + " SP.");
			
			player.getStat().removeExpAndSp(_requiredExp, _requiredSp);
			
			StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.SP, player.getSp());
			player.sendPacket(su);
			
			SystemMessage ep = SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
			ep.addNumber(_requiredExp);
			sendPacket(ep);
			
			SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
			sp.addNumber(_requiredSp);
			sendPacket(sp);
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		else
		{
			if (skill.getLevel() > 100)
			{
				_skillLvl = _baseLvl;
				player.addSkill(SkillTable.getInstance().getInfo(_skillId, _skillLvl), true);
				player.sendSkillList();
			}
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
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
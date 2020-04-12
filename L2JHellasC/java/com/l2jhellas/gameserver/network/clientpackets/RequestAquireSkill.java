package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.SkillSpellbookData;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.model.L2PledgeSkillLearn;
import com.l2jhellas.gameserver.model.L2ShortCut;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2SkillLearn;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2VillageMasterInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AcquireSkillDone;
import com.l2jhellas.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jhellas.gameserver.network.serverpackets.PledgeSkillList;
import com.l2jhellas.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.IllegalPlayerAction;
import com.l2jhellas.util.Util;

public class RequestAquireSkill extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestAquireSkill.class.getName());
	private static final String _C__6C_REQUESTAQUIRESKILL = "[C] 6C RequestAquireSkill";
	
	private int _id;
	private int _level;
	private int _skillType;
	
	@Override
	protected void readImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if(canRead(player))
		{
		  _id = readD();
		  _level = readD();
		  _skillType = readD();
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_id <= 0 || _level <= 0)
			return;
		
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final L2NpcInstance trainer = player.getLastFolkNPC();
		if (trainer == null)
			return;
		
		final int npcid = trainer.getNpcId();
		
		if (!player.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false) && !player.isGM())
			return;
		
		if (!Config.ALT_GAME_SKILL_LEARN)
			player.setSkillLearningClassId(player.getClassId());
		
		final L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		
		if (skill == null)
			return;
		
		if (!canLearn(player, skill))
		{
			return;
		}
		
		int counts = 0;
		int _requiredSp = 10000000;
		
		if (_skillType == 0)
		{
			final L2SkillLearn[] skills = SkillTreeData.getInstance().getAvailableSkills(player, player.getSkillLearningClassId());
			
			for (L2SkillLearn s : skills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if (sk == null || sk != skill || !sk.getCanLearn(player.getSkillLearningClassId()) || !sk.canTeachBy(npcid))
					continue;
				counts++;
				_requiredSp = SkillTreeData.getInstance().getSkillCost(player, skill);
			}
			
			if (counts == 0 && !Config.ALT_GAME_SKILL_LEARN)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
			
			if (player.getSp() >= _requiredSp)
			{
				if (Config.SP_BOOK_NEEDED)
				{
					final int spbId = SkillSpellbookData.getInstance().getBookForSkill(skill);
					
					if (skill.getLevel() == 1 && spbId > -1)
					{
						final L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);
						
						if (spb == null)
						{
							// Haven't spellbook
							player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
							return;
						}
						// ok
						player.destroyItem("Consume", spb, trainer, true);
					}
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
				return;
			}
		}
		else if (_skillType == 1)
		{
			int costid = 0;
			int costcount = 0;
			// Skill Learn bug Fix
			final L2SkillLearn[] skillsc = SkillTreeData.getInstance().getAvailableSkills(player);
			
			for (L2SkillLearn s : skillsc)
			{
				final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				
				if (sk == null || sk != skill)
					continue;
				
				counts++;
				costid = s.getIdCost();
				costcount = s.getCostCount();
				_requiredSp = s.getSpCost();
			}
			
			if (counts == 0)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
			
			if (player.getSp() >= _requiredSp)
			{
				if (!player.destroyItemByItemId("Consume", costid, costcount, trainer, false))
				{
					// Haven't spellbook
					player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
					return;
				}
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addNumber(costcount);
				sm.addItemName(costid);
				sendPacket(sm);
				sm = null;
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
				player.sendPacket(sm);
				sm = null;
				return;
			}
		}
		else if (_skillType == 2) // pledge skills TODO: Find appropriate system messages.
		{
			if (!player.isClanLeader())
			{
				// TODO: Find and add system msg
				player.sendMessage("This feature is available only for the clan leader");
				return;
			}
			
			int itemId = 0;
			int repCost = 100000000;
			// Skill Learn bug Fix
			final L2PledgeSkillLearn[] skills = SkillTreeData.getInstance().getAvailablePledgeSkills(player);
			
			for (L2PledgeSkillLearn s : skills)
			{
				final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				
				if (sk == null || sk != skill)
					continue;
				
				counts++;
				itemId = s.getItemId();
				repCost = s.getRepCost();
			}
			
			if (counts == 0)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
			
			if (player.getClan().getReputationScore() >= repCost)
			{
				if (Config.LIFE_CRYSTAL_NEEDED)
				{
					if (!player.destroyItemByItemId("Consume", itemId, 1, trainer, false))
					{
						// Haven't spell book
						player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
						return;
					}
					
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
					sm.addItemName(itemId);
					sm.addNumber(1);
					sendPacket(sm);
					sm = null;
				}
			}
			else
			{
				player.sendPacket(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
				return;
			}
			
			player.getClan().setReputationScore(player.getClan().getReputationScore() - repCost, true);
			player.getClan().addNewSkill(skill);
			
			if (Config.DEBUG)
				_log.fine("Learned pledge skill " + _id + " for " + _requiredSp + " SP.");
			
			SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
			cr.addNumber(repCost);
			player.sendPacket(cr);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
			sm.addSkillName(_id);
			player.sendPacket(sm);
			sm = null;
			
			player.getClan().broadcastToOnlineMembers(new PledgeSkillList(player.getClan()));
			
			player.sendPacket(AcquireSkillDone.STATIC_PACKET);

			for (L2PcInstance member : player.getClan().getOnlineMembers())
			{
				member.sendSkillList();
			}
			if (trainer != null && player != null)
				((L2VillageMasterInstance) trainer).showPledgeSkillList(player); // Maybe we shoud add a check here...
				
			return;
		}
		
		else
		{
			_log.warning(RequestAquireSkill.class.getName() + ": Recived Wrong Packet Data in Aquired Skill - unk1:" + _skillType);
			return;
		}
		
		player.sendPacket(AcquireSkillDone.STATIC_PACKET);

		player.addSkill(skill, true);
		
		if (Config.DEBUG)
			_log.fine("Learned skill " + _id + " for " + _requiredSp + " SP.");
		
		player.setSp(player.getSp() - _requiredSp);
		
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.SP, player.getSp());
		player.sendPacket(su);
		
		SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
		sp.addNumber(_requiredSp);
		sendPacket(sp);
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1);
		sm.addSkillName(_id);
		player.sendPacket(sm);
		sm = null;
		
		// update all the shortcuts to this skill
		if (_level > 1)
		{
			for (L2ShortCut sc : player.getAllShortCuts())
			{
				if (sc.getId() == _id && sc.getType() == L2ShortCut.TYPE_SKILL)
				{
					L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _level, 1);
					player.sendPacket(new ShortCutRegister(player, newsc));
					player.registerShortCut(newsc);
				}
			}
		}
		
		if (trainer instanceof L2FishermanInstance)
			((L2FishermanInstance) trainer).showSkillList(player);
		else
			trainer.showSkillList(player, player.getSkillLearningClassId());
		
		if (_id >= 1368 && _id <= 1372) // if skill is expand sendpacket :)
		{
			ExStorageMaxCount esmc = new ExStorageMaxCount(player);
			player.sendPacket(esmc);
		}
	}
	
	boolean canRead(L2PcInstance player)
	{		
		if (player == null || !player.isbOnline())
			return false;
		
		final L2NpcInstance trainer = player.getLastFolkNPC();
		
		if (trainer == null)
			return false;
				
		if (!player.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false) && !player.isGM())
			return false;
		
		return true;
	}
	
	private boolean canLearn(L2PcInstance player, L2Skill skill)
	{
		final int prevSkillLevel = player.getSkillLevel(_id);
		
		if (prevSkillLevel >= _level)
		{
			return false;
		}
		
		if ((_level != 1) && (prevSkillLevel != (_level - 1)))
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " is requesting skill Id: " + _id + " level " + _level + " without knowing it's previous level!", Config.DEFAULT_PUNISH);
			return false;
		}
		
		return true;
	}
	
	@Override
	public String getType()
	{
		return _C__6C_REQUESTAQUIRESKILL;
	}
}
package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.SkillSpellbookData;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.holder.ClanSkillNode;
import com.l2jhellas.gameserver.holder.FishingSkillNode;
import com.l2jhellas.gameserver.holder.GeneralSkillNode;
import com.l2jhellas.gameserver.model.L2ShortCut;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2VillageMasterInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AcquireSkillDone;
import com.l2jhellas.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jhellas.gameserver.network.serverpackets.PledgeSkillList;
import com.l2jhellas.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;

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
				
		if (!player.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false) && !player.isGM())
			return;
		
		if (!Config.ALT_GAME_SKILL_LEARN)
			player.setSkillLearningClassId(player.getClassId());
		
		final L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		
		if (skill == null)
			return;
		
		if (_skillType == 0)
		{
			// Player already has such skill with same or higher level.
			int skillLvl = player.getSkillLevel(_id);
			if (skillLvl >= _level)
				return;
			
			// Requested skill must be 1 level higher than existing skill.
			if (skillLvl != _level - 1)
				return;
			
			final GeneralSkillNode gsn = player.getTemplate().findSkill(_id, _level);
			if (gsn == null)
				return;
			
			if (player.getSp() < gsn.getCorrectedCost())
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
				return;
			}
			
			final int bookId = SkillSpellbookData.getInstance().getBookForSkill(_id, _level);
			if (bookId > 0 && !player.destroyItemByItemId("SkillLearn", bookId, 1, trainer, true))
			{
				player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
				return;
			}
			
			// Consume SP.
			player.removeExpAndSp(0, gsn.getCorrectedCost());
			
			// Add skill new skill.
			player.addSkill(skill,true);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1).addSkillName(skill));
			
			// Update player and return.
			player.sendSkillList();
			
			player.sendPacket(AcquireSkillDone.STATIC_PACKET);

			if (trainer != null && player != null && player.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false))
			    trainer.showSkillList(player, player.getSkillLearningClassId());
		}
		else if (_skillType == 1)
		{
			// Player already has such skill with same or higher level.
			int skillLvl = player.getSkillLevel(_id);
			if (skillLvl >= _level)
				return;
			
			// Requested skill must be 1 level higher than existing skill.
			if (skillLvl != _level - 1)
				return;
			
			final FishingSkillNode fsn = SkillTreeData.getInstance().getFishingSkillFor(player, _id, _level);
			if (fsn == null)
				return;
			
			if (!player.destroyItemByItemId("Consume", fsn.getItemId(), fsn.getItemCount(), trainer, true))
			{
				player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
				L2FishermanInstance.showSkillList(player);
				return;
			}
			
			player.addSkill(skill, true);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1).addSkillName(skill));
			
			if (_id >= 1368 && _id <= 1372)
				player.sendPacket(new ExStorageMaxCount(player));
			
			player.sendSkillList();
			
			player.sendPacket(AcquireSkillDone.STATIC_PACKET);

			if (trainer != null && player != null && player.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false))
			   L2FishermanInstance.showSkillList(player);		
		}
		else if (_skillType == 2)
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			final ClanSkillNode csn = SkillTreeData.getInstance().getClanSkillFor(player, _id, _level);
			if (csn == null)
				return;
			
			if (player.getClan().getReputationScore() < csn.getCost())
			{
				player.sendPacket(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
				return;
			}
			
			if (Config.LIFE_CRYSTAL_NEEDED && !player.destroyItemByItemId("Consume", csn.getItemId(), 1, trainer, true))
			{
				player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
				return;
			}
			
			player.getClan().setReputationScore(player.getClan().getReputationScore() - csn.getCost(), true);
			player.getClan().addNewSkill(skill);
			
			if (Config.DEBUG)
				_log.fine("Learned pledge skill " + _id + " for " + csn.getCost() + " SP.");
			
			SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
			cr.addNumber(csn.getCost());
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

			if (trainer != null && player != null && player.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false))
				((L2VillageMasterInstance) trainer).showPledgeSkillList(player); // Maybe we shoud add a check here...
				
			return;
		}

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
	}
	
	boolean canRead(L2PcInstance player)
	{		
		if (player == null || !player.isOnline())
			return false;
		
		final L2NpcInstance trainer = player.getLastFolkNPC();
		
		if (trainer == null)
			return false;
				
		if (!player.isInsideRadius(trainer, L2Npc.INTERACTION_DISTANCE, false, false) && !player.isGM())
			return false;
		
		return true;
	}
	
	@Override
	public String getType()
	{
		return _C__6C_REQUESTAQUIRESKILL;
	}
}
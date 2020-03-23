package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.enums.sound.Sound;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Manor;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.PlaySound;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Rnd;

public class Sow implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SOW
	};
	
	private L2PcInstance _activeChar;
	private L2MonsterInstance _target;
	private int _seedId;
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		_activeChar = (L2PcInstance) activeChar;
		
		L2Object[] targetList = skill.getTargetList(activeChar);
		
		if (targetList == null)
		{
			return;
		}
		
		for (int index = 0; index < targetList.length; index++)
		{
			if (!(targetList[0] instanceof L2MonsterInstance))
				continue;
			
			_target = (L2MonsterInstance) targetList[0];
			
			if (_target.isSeeded())
			{
				_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			if (_target.isDead())
			{
				_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			if (_target.getSeederId() != _activeChar.getObjectId())
			{
				_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			_seedId = _target.getSeedType();
			if (_seedId == 0)
			{
				_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			L2ItemInstance item = _activeChar.getInventory().getItemByItemId(_seedId);
			
			// Consuming used seed
			if (!_activeChar.destroyItemByItemId("Consume", item.getObjectId(), 1, _target, false))
				return;
			
			SystemMessage sm = null;
			if (calcSuccess())
			{
				PlaySound ps = Sound.ITEMSOUND_QUEST_ITEMGET.getPacket();
				_activeChar.sendPacket(ps);
				
				_target.setSeeded(_activeChar);
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
			}
			else
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);

			if (_activeChar.getParty() == null)
				_activeChar.sendPacket(sm);
			else
				_activeChar.getParty().broadcastToPartyMembers(sm);

			_target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
	}
	
	private boolean calcSuccess()
	{
		int basicSuccess = (L2Manor.getInstance().isAlternative(_seedId) ? 20 : 90);
		int minlevelSeed = 0;
		int maxlevelSeed = 0;
		minlevelSeed = L2Manor.getInstance().getSeedMinLevel(_seedId);
		maxlevelSeed = L2Manor.getInstance().getSeedMaxLevel(_seedId);
		
		int levelPlayer = _activeChar.getLevel(); // Attacker Level
		int levelTarget = _target.getLevel(); // taret Level
		
		// 5% decrease in chance if player level
		// is more then +/- 5 levels to _seed's_ level
		if (levelTarget < minlevelSeed)
			basicSuccess -= 5;
		if (levelTarget > maxlevelSeed)
			basicSuccess -= 5;
		
		// 5% decrease in chance if player level
		// is more than +/- 5 levels to _target's_ level
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
			diff = -diff;
		if (diff > 5)
			basicSuccess -= 5 * (diff - 5);
		
		// chance can't be less than 1%
		if (basicSuccess < 1)
			basicSuccess = 1;
		
		int rate = Rnd.nextInt(99);
		
		return (rate < basicSuccess);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
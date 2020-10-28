package com.l2jhellas.gameserver.model.actor.stat;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2ClassMasterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.base.Experience;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;
import com.l2jhellas.gameserver.skills.Stats;

import Extensions.Balancer.BalanceLoad;

public class PcStat extends PlayableStat
{	
	private int _oldMaxHp; // stats watch
	private int _oldMaxMp; // stats watch
	private int _oldMaxCp; // stats watch
	
	public PcStat(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addExp(long value)
	{
		L2PcInstance activeChar = getActiveChar();
		
		// Allowed to gain exp?
		if (!getActiveChar().getAccessLevel().canGainExp())
			return false;
		
		if (!super.addExp(value))
			return false;
		
		// Player is Gm and access level is below or equal to GM_DONT_TAKE_EXPSP and is in party, don't give Xp
		if (getActiveChar().isGM() && !getActiveChar().getAccessLevel().canGainExp() && getActiveChar().isInParty())
			return false;
		
		activeChar.sendPacket(new UserInfo(activeChar));
		return true;
	}
	
	@Override
	public boolean addExpAndSp(long addToExp, int addToSp)
	{
		float ratioTakenByPet = 0;
		// Player is Gm and access level is below or equal to GM_DONT_TAKE_EXPSP and is in party, don't give Xp/Sp
		L2PcInstance activeChar = getActiveChar();
		if (activeChar.isGM() && !activeChar.getAccessLevel().canGainExp() && activeChar.isInParty())
			return false;
		
		// if this player has a pet that takes from the owner's Exp, give the pet Exp now
		
		if (activeChar.getPet() instanceof L2PetInstance)
		{
			L2PetInstance pet = (L2PetInstance) activeChar.getPet();
			ratioTakenByPet = pet.getPetData().getOwnerExpTaken();
			
			// only give exp/sp to the pet by taking from the owner if the pet has a non-zero, positive ratio
			// allow possible customizations that would have the pet earning more than 100% of the owner's exp/sp
			if (ratioTakenByPet > 0 && !pet.isDead())
			{
				pet.addExpAndSp((long) (addToExp * ratioTakenByPet), (int) (addToSp * ratioTakenByPet));
			}
			// now adjust the max ratio to avoid the owner earning negative exp/sp
			if (ratioTakenByPet > 1)
			{
				ratioTakenByPet = 1;
			}
			addToExp = (long) (addToExp * (1 - ratioTakenByPet));
			addToSp = (int) (addToSp * (1 - ratioTakenByPet));
		}
		
		if (!super.addExpAndSp(addToExp, addToSp))
			return false;
		
		SystemMessage sm;
		
		if (addToExp == 0 && addToSp > 0)
			sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP).addNumber(addToSp);
		else if (addToExp > 0 && addToSp == 0)
			sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE).addNumber((int) addToExp);
		else
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP).addNumber((int) addToExp).addNumber(addToSp);
		
		getActiveChar().sendPacket(sm);
		
		return true;
	}
	
	@Override
	public boolean removeExpAndSp(long addToExp, int addToSp)
	{
		int level = getLevel();
		if (!super.removeExpAndSp(addToExp, addToSp))
			return false;
		
		if (addToExp > 0)
			getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1).addNumber((int) addToExp));
		
		if (addToSp > 0)
			getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1).addNumber(addToSp));
		
		if (getLevel() < level)
			getActiveChar().broadcastStatusUpdate();
		
		return true;
	}
	
	@Override
	public final boolean addLevel(byte value)
	{
		if (getLevel() + value > Experience.MAX_LEVEL - 1)
			return false;
		
		boolean levelIncreased = super.addLevel(value);
		
		if (Config.ALLOW_REMOTE_CLASS_MASTER)
		{
			int lvlnow = getActiveChar().getClassId().level();
			if (getLevel() >= 20 && lvlnow == 0)
			{
				L2ClassMasterInstance.ClassMaster.onAction(getActiveChar());
			}
			else if (getLevel() >= 40 && lvlnow == 1)
			{
				L2ClassMasterInstance.ClassMaster.onAction(getActiveChar());
			}
			else if (getLevel() >= 76 && lvlnow == 2)
			{
				L2ClassMasterInstance.ClassMaster.onAction(getActiveChar());
			}
		}
		
		if (levelIncreased)
		{
			if(Config.ALLOW_TUTORIAL)
			{
				QuestState qs = getActiveChar().getQuestState("255_Tutorial");
				if (qs != null)
					qs.getQuest().notifyEvent("CE40", null, getActiveChar());
			}
			if (!Config.ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE)
			{
				if (getActiveChar().getLevel() >= 25 && getActiveChar().isNewbie())
					getActiveChar().setNewbie(false);
			}
			getActiveChar().setCurrentCp(getMaxCp());
			getActiveChar().broadcastSocialActionInRadius(15);
			getActiveChar().sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL);
		}
		
		// Give AutoGet skills and all normal skills if Auto-Learn is activated.
		getActiveChar().rewardSkills();
		
		if (getActiveChar().getClan() != null)
		{
			getActiveChar().getClan().updateClanMember(getActiveChar());
			getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
		}
		if (getActiveChar().isInParty())
		{
			getActiveChar().getParty().recalculatePartyLevel(); // Recalculate the party level
		}
		
		StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getActiveChar().sendPacket(su);
		
		// Update the overloaded status of the L2PcInstance
		getActiveChar().refreshOverloaded();
		// Update the expertise status of the L2PcInstance
		getActiveChar().refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to the L2PcInstance
		getActiveChar().sendPacket(new UserInfo(getActiveChar()));
		
		return levelIncreased;
	}
	
	@Override
	public boolean addSp(int value)
	{
		if (!super.addSp(value))
			return false;
		
		StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
		su.addAttribute(StatusUpdate.SP, getSp());
		getActiveChar().sendPacket(su);
		
		return true;
	}
	
	@Override
	public final long getExpForLevel(int level)
	{
		return Experience.LEVEL[level];
	}
	
	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
	
	@Override
	public final long getExp()
	{
		if (getActiveChar().isSubClassActive())
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getExp();
		
		return super.getExp();
	}
	
	@Override
	public final void setExp(long value)
	{
		if (getActiveChar().isSubClassActive())
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setExp(value);
		else
			super.setExp(value);
	}
	
	@Override
	public final byte getLevel()
	{
		if (getActiveChar().isSubClassActive())
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();
		
		return super.getLevel();
	}
	
	@Override
	public final void setLevel(byte value)
	{
		if (value > Experience.MAX_LEVEL - 1)
			value = Experience.MAX_LEVEL - 1;
		
		if (getActiveChar().isSubClassActive())
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setLevel(value);
		else
			super.setLevel(value);
	}
	
	@Override
	public final int getMaxCp()
	{
		int val = (int) calcStat(Stats.MAX_CP, getActiveChar().getTemplate().baseCpMax, null, null);
		
		if (getActiveChar().getClassId().getId() >= 88)
			val += BalanceLoad.CP[getActiveChar().getClassId().getId() - 88];
		
		if (val != _oldMaxCp)
		{
			_oldMaxCp = val;
			
			if (getActiveChar().getStatus().getCurrentCp() != val)
				getActiveChar().getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp());
		}
		return val;
	}

	@Override
	public final int getMaxHp()
	{
		int val = super.getMaxHp();
		
		if (getActiveChar().getClassId().getId() >= 88)
			val += BalanceLoad.HP[getActiveChar().getClassId().getId() - 88];
		
		if (val != _oldMaxHp)
		{
			_oldMaxHp = val;
			
			if (getActiveChar().getStatus().getCurrentHp() != val)
				getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp()); // trigger start of regeneration
		}
		return val;
	}

	@Override
	public final int getMaxMp()
	{
		// Get the Max MP (base+modifier) of the L2PcInstance
		int val = super.getMaxMp();
		
		if (getActiveChar().getClassId().getId() >= 88)
			val += BalanceLoad.MP[getActiveChar().getClassId().getId() - 88];
		
		if (val != _oldMaxMp)
		{
			_oldMaxMp = val;
			
			if (getActiveChar().getStatus().getCurrentMp() != val)
				getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp());
		}
		return val;
	}
	
	@Override
	public final int getSp()
	{
		if (getActiveChar().isSubClassActive())
			return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();
		
		return super.getSp();
	}
	
	@Override
	public final int getSTR()
	{
		int str = (int) calcStat(Stats.STAT_STR, getActiveChar().getTemplate().baseSTR, null, null);
		if (getActiveChar().getClassId().getId() >= 88)
			str += BalanceLoad.STR[(getActiveChar()).getClassId().getId() - 88];
		return str;
	}
	
	@Override
	public final int getDEX()
	{
		int DEX = (int) calcStat(Stats.STAT_DEX, getActiveChar().getTemplate().baseDEX, null, null);
		if (getActiveChar().getClassId().getId() >= 88)
			DEX += BalanceLoad.DEX[(getActiveChar()).getClassId().getId() - 88];
		return DEX;
	}
	
	public final int getCON()
	{
		int CON = (int) calcStat(Stats.STAT_CON, getActiveChar().getTemplate().baseCON, null, null);
		if (getActiveChar().getClassId().getId() >= 88)
			CON += BalanceLoad.CON[(getActiveChar()).getClassId().getId() - 88];
		return CON;
	}
	
	@Override
	public int getINT()
	{
		int INT = (int) calcStat(Stats.STAT_INT, getActiveChar().getTemplate().baseINT, null, null);
		if (getActiveChar().getClassId().getId() >= 88)
			INT += BalanceLoad.INT[(getActiveChar()).getClassId().getId() - 88];
		return INT;
	}
	
	@Override
	public final int getMEN()
	{
		int MEN = (int) calcStat(Stats.STAT_MEN,getActiveChar().getTemplate().baseMEN, null, null);
		if (getActiveChar().getClassId().getId() >= 88)
			MEN += BalanceLoad.MEN[(getActiveChar()).getClassId().getId() - 88];
		return MEN;
	}
	
	@Override
	public final int getWIT()
	{
		int WIT = (int) calcStat(Stats.STAT_WIT, getActiveChar().getTemplate().baseWIT, null, null);
		if (getActiveChar().getClassId().getId() >= 88)
			WIT += BalanceLoad.WIT[(getActiveChar()).getClassId().getId() - 88];
		return WIT;
	}
	
	@Override
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		int val = super.getCriticalHit(target,skill);

		if (getActiveChar().getClassId().getId() >= 88)
			val += BalanceLoad.Critical[getActiveChar().getClassId().getId() - 88];

		return Math.min(val, Config.MAX_PCRIT_RATE);
	}
	
	@Override
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		int val = super.getMCriticalHit(target,skill);

		if (getActiveChar().getClassId().getId() >= 88)
			val += BalanceLoad.MagicCritical[getActiveChar().getClassId().getId() - 88];
		
		return Math.min(val, Config.MAX_MCRIT_RATE);
	}

	@Override
	public int getEvasionRate(L2Character target)
	{
		int val = super.getEvasionRate(target);
		if (getActiveChar().getClassId().getId() >= 88)
			val += BalanceLoad.Evasion[getActiveChar().getClassId().getId() - 88];
		return Math.min(val, Config.ALT_MAX_EVASION);
	}
	
	@Override
	public int getAccuracy()
	{
		int val = super.getAccuracy();
		if (getActiveChar().getClassId().getId() >= 88)
			val += BalanceLoad.Accuracy[getActiveChar().getClassId().getId() - 88];
		return val;
	}
	
	@Override
	public int getPhysicalAttackRange()
	{
		return getActiveChar().getAttackType().equals(L2WeaponType.BOW) ? (int) calcStat(Stats.POWER_ATTACK_RANGE, getActiveChar().getTemplate().baseAtkRange, null, null) : getActiveChar().getAttackType().getRange();
	}
	
	@Override
	public final void setSp(int value)
	{
		if (getActiveChar().isSubClassActive())
			getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setSp(value);
		else
			super.setSp(value);
		
		StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
		su.addAttribute(StatusUpdate.SP, getSp());
		getActiveChar().sendPacket(su);
	}
}
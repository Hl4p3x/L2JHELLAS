package com.l2jhellas.gameserver.model.actor;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.stat.PlayableStat;
import com.l2jhellas.gameserver.model.actor.status.PlayableStatus;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.templates.L2CharTemplate;

public abstract class L2Playable extends L2Character
{
	
	private boolean _isNoblesseBlessed = false; // for Noblesse Blessing skill, restores buffs after death
	private boolean _getCharmOfLuck = false; // Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	private boolean _isPhoenixBlessed = false; // for Soul of The PPhoenix or Salvation buffs
	private boolean _ProtectionBlessing = false;
	private String _lastTownName = null;
	
	public L2Playable(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		getStat(); // init stats
		getStatus(); // init status
	}
	
	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player.getTarget() != this)
		{
			player.setTarget(this);
		}
		else
		{
			if (isAutoAttackable(player) && player.isInsideRadius(this, player.getPhysicalAttackRange(), false, false) && ((Config.GEODATA) ? GeoEngine.canSeeTarget(player, this, isFlying()) : GeoEngine.canSeeTarget(player, this)))
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			else
				player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public PlayableStat getStat()
	{
		if ((super.getStat() == null) || !(super.getStat() instanceof PlayableStat))
			setStat(new PlayableStat(this));
		return (PlayableStat) super.getStat();
	}
	
	@Override
	public PlayableStatus getStatus()
	{
		if ((super.getStatus() == null) || !(super.getStatus() instanceof PlayableStatus))
			setStatus(new PlayableStatus(this));
		return (PlayableStatus) super.getStatus();
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (killer != null)
		{
			L2PcInstance player = null;
			if (killer instanceof L2PcInstance)
				player = (L2PcInstance) killer;
			else if (killer instanceof L2Summon)
				player = ((L2Summon) killer).getOwner();
			
			if (player != null)
				player.onKillUpdatePvPKarma(this);
		}
		return true;
	}
	
	public boolean checkIfPvP(L2Character target)
	{
		if (target == null) // Target is null
			return false;
		if (target == this) // Target is self
			return false;
		if (!(target instanceof L2Playable)) // Target is not a
			// L2PlayableInstance
			return false;
		
		L2PcInstance player = null;
		if (this instanceof L2PcInstance)
			player = (L2PcInstance) this;
		else if (this instanceof L2Summon)
			player = ((L2Summon) this).getOwner();
		
		if (player == null) // Active player is null
			return false;
		if (player.getKarma() != 0) // Active player has karma
			return false;
		
		L2PcInstance targetPlayer = null;
		if (target instanceof L2PcInstance)
			targetPlayer = (L2PcInstance) target;
		else if (target instanceof L2Summon)
			targetPlayer = ((L2Summon) target).getOwner();
		
		if (targetPlayer == null) // Target player is null
			return false;
		if (targetPlayer == this) // Target player is self
			return false;
		if (targetPlayer.getKarma() != 0) // Target player has karma
			return false;
		if (targetPlayer.getPvpFlag() == 0)
			return false;
		
		return true;
		
	}
	
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	// Support for Noblesse Blessing skill, where buffs are retained
	// after resurrect
	public final boolean isNoblesseBlessed()
	{
		return _isNoblesseBlessed;
	}
	
	public final void setIsNoblesseBlessed(boolean value)
	{
		_isNoblesseBlessed = value;
	}
	
	public final void startNoblesseBlessing()
	{
		setIsNoblesseBlessed(true);
		updateAbnormalEffect();
	}
	
	public final void stopNoblesseBlessing(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.NOBLESSE_BLESSING);
		else
		{
			removeEffect(effect);
		}
		
		setIsNoblesseBlessed(false);
		updateAbnormalEffect();
	}
	
	// Support for Soul of the Phoenix and Salvation skills
	public final boolean isPhoenixBlessed()
	{
		return _isPhoenixBlessed;
	}
	
	public final void setIsPhoenixBlessed(boolean value)
	{
		_isPhoenixBlessed = value;
	}
	
	public final void startPhoenixBlessing()
	{
		setIsPhoenixBlessed(true);
		updateAbnormalEffect();
	}
	
	public final void stopPhoenixBlessing(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.PHOENIX_BLESSING);
		else
			removeEffect(effect);
		
		setIsPhoenixBlessed(false);
		updateAbnormalEffect();
	}
	
	// for Newbie Protection Blessing skill, keeps you safe from an attack by a
	// chaotic character >= 10 levels apart from you
	public final boolean getProtectionBlessing()
	{
		return _ProtectionBlessing;
	}
	
	public final void setProtectionBlessing(boolean value)
	{
		_ProtectionBlessing = value;
	}
	
	public void startProtectionBlessing()
	{
		setProtectionBlessing(true);
		updateAbnormalEffect();
	}
	
	public void stopProtectionBlessing(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.PROTECTION_BLESSING);
		else
			removeEffect(effect);
		
		setProtectionBlessing(false);
		updateAbnormalEffect();
	}
	
	public boolean _donator = false;
	
	public void setDonator(boolean value)
	{
		_donator = value;
	}
	
	public boolean isDonator()
	{
		return _donator;
	}
	
	public abstract boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage);
	
	public abstract boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage);
	
	// Charm of Luck - During a Raid/Boss war, decreased chance for death
	// penalty
	public final boolean getCharmOfLuck()
	{
		return _getCharmOfLuck;
	}
	
	public final void setCharmOfLuck(boolean value)
	{
		_getCharmOfLuck = value;
	}
	
	public final void startCharmOfLuck()
	{
		setCharmOfLuck(true);
		updateAbnormalEffect();
	}
	
	public final void stopCharmOfLuck(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.CHARM_OF_LUCK);
		else
			removeEffect(effect);
		
		setCharmOfLuck(false);
		updateAbnormalEffect();
	}
	
	public void setLastTownName(String lastTownName)
	{
		_lastTownName = lastTownName;
	}
	
	public String getLastTownName()
	{
		return _lastTownName;
	}
	
	public abstract int getKarma();
	
	public abstract byte getPvpFlag();
}
package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.instancemanager.RaidBossPointsManager;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.entity.Hero;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.PlaySound;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public final class L2GrandBossInstance extends L2MonsterInstance
{
	private boolean _teleportedToNest;
	
	protected boolean _isInSocialAction = false;
	
	public boolean IsInSocialAction()
	{
		return _isInSocialAction;
	}
	
	public void setIsInSocialAction(boolean value)
	{
		_isInSocialAction = value;
	}
	
	public L2GrandBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public void setTeleported(boolean flag)
	{
		_teleportedToNest = flag;
	}
	
	public boolean getTeleported()
	{
		return _teleportedToNest;
	}
	
	@Override
	public void onSpawn()
	{
		if (getNpcId() == 29028) // baium and valakas are all the time in passive mode, theirs attack AI handled in AI scripts
			super.disableCoreAI(true);
		super.onSpawn();
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		switch (getTemplate().npcId)
		{
			case 29014: // Orfen
				if ((getCurrentHp() - damage) < getMaxHp() / 2 && !getTeleported())
				{
					clearAggroList();
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					teleToLocation(43577, 15985, -4396, false);
					setTeleported(true);
					setCanReturnToSpawnPoint(false);
				}
				break;
			default:
		}
		if (IsInSocialAction() || isInvul())
			return;
		super.reduceCurrentHp(damage, attacker, awake);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		L2PcInstance player = null;
		
		if (killer instanceof L2PcInstance)
			player = (L2PcInstance) killer;
		else if (killer instanceof L2Summon)
			player = ((L2Summon) killer).getOwner();
		
		if (player != null)
		{
			if (player.isInParty())
			{
				for (L2PcInstance member : player.getParty().getPartyMembers())
				{
					RaidBossPointsManager.getInstance().addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));				
					if (member.isNoble())
					    Hero.getInstance().setRBkilled(member.getObjectId(), getNpcId());
				}
			}
			else
			{
				RaidBossPointsManager.getInstance().addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
				if (player.isNoble())
				    Hero.getInstance().setRBkilled(player.getObjectId(), getNpcId());
			}

			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
			broadcastPacket(PlaySound.createSound("systemmsg_e.1209"));
		}
		return true;
	}
	
	@Override
	public void doAttack(L2Character target,boolean stopMove)
	{
		if (_isInSocialAction)
			return;
		
		super.doAttack(target,true);
	}
	
	@Override
	public void doCast(L2Skill skill)
	{
		if (_isInSocialAction)
			return;
		
		super.doCast(skill);
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	@Override
	public boolean isBoss()
	{
		return true;
	}
}
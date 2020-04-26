package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.instancemanager.RaidBossPointsManager;
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
	public L2GrandBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
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
		if (isInvul())
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